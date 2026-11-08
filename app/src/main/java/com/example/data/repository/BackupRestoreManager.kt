package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CurrencyEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.VoucherEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class BackupRestoreManager(
    private val context: Context,
    private val database: AppDatabase
) {

    suspend fun createBackupJson(): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("appName", "حساباتي")

        // 1. Accounts
        val accounts = database.accountDao().getAllAccounts().first()
        val accArray = JSONArray()
        accounts.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("phone", it.phone)
            obj.put("category", it.category)
            obj.put("openingBalance", it.openingBalance)
            obj.put("openingBalanceType", it.openingBalanceType)
            obj.put("currencyCode", it.currencyCode)
            obj.put("notes", it.notes)
            obj.put("accountNumber", it.accountNumber)
            obj.put("createdAt", it.createdAt)
            accArray.put(obj)
        }
        root.put("accounts", accArray)

        // 2. Transactions
        val transactions = database.transactionDao().getAllTransactions().first()
        val transArray = JSONArray()
        transactions.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("accountId", it.accountId)
            obj.put("type", it.type)
            obj.put("amount", it.amount)
            obj.put("currencyCode", it.currencyCode)
            obj.put("date", it.date)
            obj.put("title", it.title)
            obj.put("description", it.description)
            obj.put("notes", it.notes)
            obj.put("transferRefId", it.transferRefId ?: "")
            obj.put("voucherNumber", it.voucherNumber ?: "")
            obj.put("createdAt", it.createdAt)
            transArray.put(obj)
        }
        root.put("transactions", transArray)

        // 3. Vouchers
        val vouchers = database.voucherDao().getAllVouchers().first()
        val vouchArray = JSONArray()
        vouchers.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("voucherNumber", it.voucherNumber)
            obj.put("type", it.type)
            obj.put("accountId", it.accountId)
            obj.put("accountName", it.accountName)
            obj.put("amount", it.amount)
            obj.put("currencyCode", it.currencyCode)
            obj.put("date", it.date)
            obj.put("personName", it.personName)
            obj.put("description", it.description)
            obj.put("notes", it.notes)
            obj.put("transactionId", it.transactionId ?: -1)
            vouchArray.put(obj)
        }
        root.put("vouchers", vouchArray)

        // 4. Currencies
        val currencies = database.currencyDao().getAllCurrencies().first()
        val currArray = JSONArray()
        currencies.forEach {
            val obj = JSONObject()
            obj.put("code", it.code)
            obj.put("name", it.name)
            obj.put("symbol", it.symbol)
            obj.put("exchangeRateToMain", it.exchangeRateToMain)
            obj.put("isMain", it.isMain)
            currArray.put(obj)
        }
        root.put("currencies", currArray)

        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        val file = File(backupDir, "hisabati_backup_${System.currentTimeMillis()}.json")
        FileOutputStream(file).use {
            it.write(root.toString(2).toByteArray())
        }
        file
    }

    suspend fun restoreBackup(jsonContent: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonContent)
            if (!root.has("accounts") || !root.has("transactions")) {
                return@withContext Result.failure(Exception("ملف النسخة الاحتياطية غير صالح"))
            }

            val accArray = root.getJSONArray("accounts")
            val transArray = root.getJSONArray("transactions")

            database.withTransaction {
                // Restore accounts
                for (i in 0 until accArray.length()) {
                    val obj = accArray.getJSONObject(i)
                    database.accountDao().insertAccount(
                        AccountEntity(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            phone = obj.optString("phone", ""),
                            category = obj.optString("category", "عملاء"),
                            openingBalance = obj.optString("openingBalance", "0.00"),
                            openingBalanceType = obj.optString("openingBalanceType", "FOR_ME"),
                            currencyCode = obj.optString("currencyCode", "SAR"),
                            notes = obj.optString("notes", ""),
                            accountNumber = obj.optString("accountNumber", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }

                // Restore transactions
                for (i in 0 until transArray.length()) {
                    val obj = transArray.getJSONObject(i)
                    database.transactionDao().insertTransaction(
                        TransactionEntity(
                            id = obj.optLong("id", 0),
                            accountId = obj.getLong("accountId"),
                            type = obj.getString("type"),
                            amount = obj.getString("amount"),
                            currencyCode = obj.optString("currencyCode", "SAR"),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            notes = obj.optString("notes", ""),
                            transferRefId = obj.optString("transferRefId").ifBlank { null },
                            voucherNumber = obj.optString("voucherNumber").ifBlank { null },
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            Result.success(accArray.length() + transArray.length())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
