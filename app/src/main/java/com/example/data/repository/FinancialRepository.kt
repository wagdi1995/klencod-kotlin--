package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CurrencyEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.VoucherEntity
import com.example.domain.model.AccountModel
import com.example.domain.model.BalanceType
import com.example.domain.model.CurrencyModel
import com.example.domain.model.FinancialSummary
import com.example.domain.model.MonthlyStat
import com.example.domain.model.TransactionModel
import com.example.domain.model.TransactionType
import com.example.domain.model.VoucherModel
import com.example.domain.model.VoucherType
import com.example.domain.util.DateFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.UUID

class FinancialRepository(private val database: AppDatabase) {

    private val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()
    private val voucherDao = database.voucherDao()
    private val currencyDao = database.currencyDao()
    private val categoryDao = database.categoryDao()
    private val settingsDao = database.settingsDao()

    // --- ACCOUNTS ---

    fun getAccounts(): Flow<List<AccountModel>> {
        return combine(
            accountDao.getAllAccounts(),
            transactionDao.getAllTransactions(),
            currencyDao.getAllCurrencies()
        ) { accounts, transactions, currencies ->
            val transByAccount = transactions.groupBy { it.accountId }
            accounts.map { entity ->
                val acctTrans = transByAccount[entity.id] ?: emptyList()
                calculateAccountModel(entity, acctTrans)
            }
        }
    }

    fun getAccountById(id: Long): Flow<AccountModel?> {
        return combine(
            accountDao.getAccountById(id),
            transactionDao.getTransactionsForAccount(id)
        ) { entity, transactions ->
            if (entity == null) null
            else calculateAccountModel(entity, transactions)
        }
    }

    suspend fun insertAccount(account: AccountModel): Long {
        val entity = AccountEntity(
            name = account.name,
            phone = account.phone,
            category = account.category,
            openingBalance = account.openingBalance.toPlainString(),
            openingBalanceType = account.openingBalanceType.name,
            currencyCode = account.currencyCode,
            notes = account.notes,
            accountNumber = account.accountNumber.ifBlank { (1000..9999).random().toString() },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return accountDao.insertAccount(entity)
    }

    suspend fun updateAccount(account: AccountModel) {
        val existing = accountDao.getAccountByIdDirect(account.id) ?: return
        val entity = existing.copy(
            name = account.name,
            phone = account.phone,
            category = account.category,
            openingBalance = account.openingBalance.toPlainString(),
            openingBalanceType = account.openingBalanceType.name,
            currencyCode = account.currencyCode,
            notes = account.notes,
            accountNumber = account.accountNumber.ifBlank { existing.accountNumber },
            updatedAt = System.currentTimeMillis()
        )
        accountDao.updateAccount(entity)
    }

    suspend fun deleteAccount(accountId: Long, force: Boolean = false): Boolean {
        val transCount = transactionDao.countTransactionsForAccount(accountId)
        if (transCount > 0 && !force) {
            return false // Safe deletion policy: prevent accidental deletion with transactions
        }
        database.withTransaction {
            if (transCount > 0) {
                // Delete all transactions for this account first
                val trans = transactionDao.getTransactionsForAccountAsc(accountId)
                trans.forEach {
                    if (it.voucherNumber != null) {
                        voucherDao.deleteVoucherByTransactionId(it.id)
                    }
                    transactionDao.deleteTransaction(it)
                }
            }
            accountDao.deleteAccountById(accountId)
        }
        return true
    }

    // --- TRANSACTIONS ---

    fun getAllTransactions(): Flow<List<TransactionModel>> {
        return combine(
            transactionDao.getAllTransactions(),
            accountDao.getAllAccounts()
        ) { transactions, accounts ->
            val accountMap = accounts.associateBy { it.id }
            transactions.map { entity ->
                val acct = accountMap[entity.accountId]
                val type = TransactionType.fromString(entity.type)
                val amount = BigDecimal(entity.amount)
                TransactionModel(
                    id = entity.id,
                    accountId = entity.accountId,
                    accountName = acct?.name ?: "حساب محذوف",
                    accountCategory = acct?.category ?: "",
                    type = type,
                    amount = amount,
                    currencyCode = entity.currencyCode,
                    date = entity.date,
                    title = entity.title,
                    description = entity.description,
                    notes = entity.notes,
                    transferRefId = entity.transferRefId,
                    voucherNumber = entity.voucherNumber,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionModel>> {
        return combine(
            transactionDao.getRecentTransactions(limit),
            accountDao.getAllAccounts()
        ) { transactions, accounts ->
            val accountMap = accounts.associateBy { it.id }
            transactions.map { entity ->
                val acct = accountMap[entity.accountId]
                TransactionModel(
                    id = entity.id,
                    accountId = entity.accountId,
                    accountName = acct?.name ?: "حساب",
                    accountCategory = acct?.category ?: "",
                    type = TransactionType.fromString(entity.type),
                    amount = BigDecimal(entity.amount),
                    currencyCode = entity.currencyCode,
                    date = entity.date,
                    title = entity.title,
                    description = entity.description,
                    notes = entity.notes,
                    transferRefId = entity.transferRefId,
                    voucherNumber = entity.voucherNumber,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionModel>> {
        return combine(
            transactionDao.getTransactionsForAccount(accountId),
            accountDao.getAccountById(accountId)
        ) { transactions, accountEntity ->
            if (accountEntity == null) emptyList()
            else {
                // Calculate chronological running balance from oldest to newest
                val initialOpening = BigDecimal(accountEntity.openingBalance).let {
                    if (accountEntity.openingBalanceType == BalanceType.FOR_ME.name) it else it.negate()
                }

                // Sort ascending for running balance calculation
                val chronological = transactions.sortedWith(compareBy({ it.date }, { it.id }))
                var currentRunning = initialOpening

                val calculated = chronological.map { trans ->
                    val type = TransactionType.fromString(trans.type)
                    val amount = BigDecimal(trans.amount)
                    currentRunning = if (type.isIncoming) {
                        currentRunning.add(amount)
                    } else {
                        currentRunning.subtract(amount)
                    }
                    TransactionModel(
                        id = trans.id,
                        accountId = trans.accountId,
                        accountName = accountEntity.name,
                        accountCategory = accountEntity.category,
                        type = type,
                        amount = amount,
                        currencyCode = trans.currencyCode,
                        date = trans.date,
                        title = trans.title,
                        description = trans.description,
                        notes = trans.notes,
                        transferRefId = trans.transferRefId,
                        voucherNumber = trans.voucherNumber,
                        runningBalance = currentRunning,
                        createdAt = trans.createdAt
                    )
                }
                // Return descending for UI display
                calculated.reversed()
            }
        }
    }

    suspend fun insertTransaction(transaction: TransactionModel): Long {
        val entity = TransactionEntity(
            accountId = transaction.accountId,
            type = transaction.type.name,
            amount = transaction.amount.toPlainString(),
            currencyCode = transaction.currencyCode,
            date = transaction.date,
            title = transaction.title,
            description = transaction.description,
            notes = transaction.notes,
            transferRefId = transaction.transferRefId,
            voucherNumber = transaction.voucherNumber,
            createdAt = System.currentTimeMillis()
        )
        return transactionDao.insertTransaction(entity)
    }

    suspend fun updateTransaction(transaction: TransactionModel) {
        val existing = transactionDao.getTransactionById(transaction.id) ?: return
        val updated = existing.copy(
            accountId = transaction.accountId,
            type = transaction.type.name,
            amount = transaction.amount.toPlainString(),
            currencyCode = transaction.currencyCode,
            date = transaction.date,
            title = transaction.title,
            description = transaction.description,
            notes = transaction.notes
        )
        transactionDao.updateTransaction(updated)
    }

    suspend fun deleteTransaction(transactionId: Long) {
        val trans = transactionDao.getTransactionById(transactionId) ?: return
        database.withTransaction {
            if (trans.transferRefId != null) {
                // Delete both legs of transfer
                transactionDao.deleteTransactionsByTransferRef(trans.transferRefId)
            } else {
                if (trans.voucherNumber != null) {
                    voucherDao.deleteVoucherByTransactionId(trans.id)
                }
                transactionDao.deleteTransaction(trans)
            }
        }
    }

    // --- TRANSFERS ---

    suspend fun performTransfer(
        sourceAccountId: Long,
        destinationAccountId: Long,
        amount: BigDecimal,
        currencyCode: String,
        exchangeRate: Double = 1.0,
        date: Long = System.currentTimeMillis(),
        description: String = "",
        notes: String = ""
    ): Boolean {
        if (sourceAccountId == destinationAccountId || amount <= BigDecimal.ZERO) return false

        val sourceAccount = accountDao.getAccountByIdDirect(sourceAccountId) ?: return false
        val destAccount = accountDao.getAccountByIdDirect(destinationAccountId) ?: return false

        val transferRefId = UUID.randomUUID().toString()
        val destAmount = if (exchangeRate != 1.0 && exchangeRate > 0) {
            amount.multiply(BigDecimal.valueOf(exchangeRate)).setScale(2, RoundingMode.HALF_UP)
        } else {
            amount
        }

        database.withTransaction {
            // Source account: Outgoing transfer
            transactionDao.insertTransaction(
                TransactionEntity(
                    accountId = sourceAccountId,
                    type = TransactionType.TRANSFER_OUT.name,
                    amount = amount.toPlainString(),
                    currencyCode = currencyCode,
                    date = date,
                    title = "تحويل صادر إلى ${destAccount.name}",
                    description = description.ifBlank { "تحويل إلى حساب ${destAccount.name}" },
                    notes = notes,
                    transferRefId = transferRefId
                )
            )

            // Destination account: Incoming transfer
            transactionDao.insertTransaction(
                TransactionEntity(
                    accountId = destinationAccountId,
                    type = TransactionType.TRANSFER_IN.name,
                    amount = destAmount.toPlainString(),
                    currencyCode = destAccount.currencyCode,
                    date = date,
                    title = "تحويل وارد من ${sourceAccount.name}",
                    description = description.ifBlank { "تحويل من حساب ${sourceAccount.name}" },
                    notes = notes,
                    transferRefId = transferRefId
                )
            )
        }
        return true
    }

    // --- VOUCHERS ---

    fun getAllVouchers(): Flow<List<VoucherModel>> {
        return voucherDao.getAllVouchers().map { list ->
            list.map { entity ->
                VoucherModel(
                    id = entity.id,
                    voucherNumber = entity.voucherNumber,
                    type = VoucherType.fromString(entity.type),
                    accountId = entity.accountId,
                    accountName = entity.accountName,
                    amount = BigDecimal(entity.amount),
                    currencyCode = entity.currencyCode,
                    date = entity.date,
                    personName = entity.personName,
                    description = entity.description,
                    notes = entity.notes,
                    transactionId = entity.transactionId,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    suspend fun getNextVoucherNumber(type: VoucherType): String {
        val lastNumber = voucherDao.getLatestVoucherNumber(type.name)
        val year = Calendar.getInstance().get(Calendar.YEAR)
        if (lastNumber == null) {
            return "${type.prefix}$year-0001"
        }
        val parts = lastNumber.split("-")
        return if (parts.size >= 3) {
            val num = (parts[2].toIntOrNull() ?: 0) + 1
            "${type.prefix}$year-${String.format("%04d", num)}"
        } else {
            "${type.prefix}$year-0001"
        }
    }

    suspend fun createVoucher(voucher: VoucherModel): Long {
        return database.withTransaction {
            val transType = if (voucher.type == VoucherType.RECEIPT) {
                TransactionType.RECEIPT_VOUCHER
            } else {
                TransactionType.PAYMENT_VOUCHER
            }

            // 1. Create linked financial transaction
            val transId = transactionDao.insertTransaction(
                TransactionEntity(
                    accountId = voucher.accountId,
                    type = transType.name,
                    amount = voucher.amount.toPlainString(),
                    currencyCode = voucher.currencyCode,
                    date = voucher.date,
                    title = if (voucher.type == VoucherType.RECEIPT) "سند قبض: ${voucher.personName}" else "سند صرف: ${voucher.personName}",
                    description = voucher.description,
                    notes = voucher.notes,
                    voucherNumber = voucher.voucherNumber
                )
            )

            // 2. Insert voucher record
            val voucherEntity = VoucherEntity(
                voucherNumber = voucher.voucherNumber,
                type = voucher.type.name,
                accountId = voucher.accountId,
                accountName = voucher.accountName,
                amount = voucher.amount.toPlainString(),
                currencyCode = voucher.currencyCode,
                date = voucher.date,
                personName = voucher.personName,
                description = voucher.description,
                notes = voucher.notes,
                transactionId = transId
            )
            voucherDao.insertVoucher(voucherEntity)
        }
    }

    suspend fun deleteVoucher(voucherId: Long) {
        val voucher = voucherDao.getVoucherById(voucherId)
        database.withTransaction {
            voucherDao.deleteVoucherById(voucherId)
        }
    }

    // --- CURRENCIES ---

    fun getAllCurrencies(): Flow<List<CurrencyModel>> {
        return currencyDao.getAllCurrencies().map { list ->
            list.map {
                CurrencyModel(
                    code = it.code,
                    name = it.name,
                    symbol = it.symbol,
                    exchangeRateToMain = it.exchangeRateToMain,
                    isMain = it.isMain,
                    decimalPlaces = it.decimalPlaces,
                    isEnabled = it.isEnabled
                )
            }
        }
    }

    fun getMainCurrency(): Flow<CurrencyModel?> {
        return currencyDao.getMainCurrency().map {
            if (it == null) null
            else CurrencyModel(
                code = it.code,
                name = it.name,
                symbol = it.symbol,
                exchangeRateToMain = it.exchangeRateToMain,
                isMain = it.isMain,
                decimalPlaces = it.decimalPlaces,
                isEnabled = it.isEnabled
            )
        }
    }

    suspend fun insertOrUpdateCurrency(currency: CurrencyModel) {
        val entity = CurrencyEntity(
            code = currency.code,
            name = currency.name,
            symbol = currency.symbol,
            exchangeRateToMain = currency.exchangeRateToMain,
            isMain = currency.isMain,
            decimalPlaces = currency.decimalPlaces,
            isEnabled = currency.isEnabled
        )
        currencyDao.insertCurrency(entity)
    }

    suspend fun setMainCurrency(code: String) {
        database.withTransaction {
            currencyDao.clearMainCurrency()
            currencyDao.setMainCurrency(code)
        }
    }

    // --- CATEGORIES ---

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insertCategory(name: String, iconKey: String = "category"): Long {
        return categoryDao.insertCategory(CategoryEntity(name = name, iconKey = iconKey))
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    // --- FINANCIAL SUMMARY & MONTHLY STATS ---

    fun getFinancialSummary(): Flow<FinancialSummary> {
        return combine(
            getAccounts(),
            currencyDao.getAllCurrencies()
        ) { accounts, currencies ->
            val currencyMap = currencies.associateBy { it.code }
            val mainCurrency = currencies.find { it.isMain } ?: currencies.firstOrNull()
            val mainSymbol = mainCurrency?.symbol ?: "ر.س"

            var totalForMe = BigDecimal.ZERO
            var totalOnMe = BigDecimal.ZERO

            accounts.forEach { acct ->
                val curr = currencyMap[acct.currencyCode]
                val rate = curr?.exchangeRateToMain ?: 1.0
                val converted = if (rate != 1.0 && rate > 0) {
                    acct.currentBalance.divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP)
                } else {
                    acct.currentBalance
                }

                if (converted > BigDecimal.ZERO) {
                    totalForMe = totalForMe.add(converted)
                } else if (converted < BigDecimal.ZERO) {
                    totalOnMe = totalOnMe.add(converted.abs())
                }
            }

            val totalBalance = totalForMe.subtract(totalOnMe)

            FinancialSummary(
                totalBalance = totalBalance,
                totalForMe = totalForMe,
                totalOnMe = totalOnMe,
                accountsCount = accounts.size,
                mainCurrencySymbol = mainSymbol
            )
        }
    }

    fun getMonthlyStats(): Flow<List<MonthlyStat>> {
        return transactionDao.getAllTransactions().map { transactions ->
            val calendar = Calendar.getInstance()
            val result = mutableListOf<MonthlyStat>()

            // Compute last 6 months
            for (i in 5 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH)
                val monthLabel = DateFormatter.getMonthName(month)

                // Calculate incoming and outgoing for this month, excluding internal transfers
                var monthIncoming = BigDecimal.ZERO
                var monthOutgoing = BigDecimal.ZERO

                transactions.forEach { trans ->
                    val tCal = Calendar.getInstance().apply { timeInMillis = trans.date }
                    if (tCal.get(Calendar.YEAR) == year && tCal.get(Calendar.MONTH) == month) {
                        val type = TransactionType.fromString(trans.type)
                        if (!type.isTransfer) { // Exclude internal transfers from external revenue/expense
                            val amt = BigDecimal(trans.amount)
                            if (type.isIncoming) {
                                monthIncoming = monthIncoming.add(amt)
                            } else {
                                monthOutgoing = monthOutgoing.add(amt)
                            }
                        }
                    }
                }

                result.add(
                    MonthlyStat(
                        monthLabel = monthLabel,
                        monthIndex = month,
                        year = year,
                        totalIncoming = monthIncoming,
                        totalOutgoing = monthOutgoing
                    )
                )
            }
            result
        }
    }

    private fun calculateAccountModel(
        entity: AccountEntity,
        transactions: List<TransactionEntity>
    ): AccountModel {
        val opening = BigDecimal(entity.openingBalance)
        val signedOpening = if (entity.openingBalanceType == BalanceType.FOR_ME.name) opening else opening.negate()

        var current = signedOpening
        transactions.forEach { trans ->
            val type = TransactionType.fromString(trans.type)
            val amt = BigDecimal(trans.amount)
            current = if (type.isIncoming) {
                current.add(amt)
            } else {
                current.subtract(amt)
            }
        }

        return AccountModel(
            id = entity.id,
            name = entity.name,
            phone = entity.phone,
            category = entity.category,
            openingBalance = opening,
            openingBalanceType = BalanceType.fromString(entity.openingBalanceType),
            currencyCode = entity.currencyCode,
            notes = entity.notes,
            accountNumber = entity.accountNumber,
            currentBalance = current,
            createdAt = entity.createdAt
        )
    }
}
