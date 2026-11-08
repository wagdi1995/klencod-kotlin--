package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FinancialRepository
import com.example.domain.model.AccountModel
import com.example.domain.model.TransactionModel
import com.example.domain.util.PdfReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal

class AccountDetailViewModel(
    private val repository: FinancialRepository,
    val accountId: Long
) : ViewModel() {

    val account: StateFlow<AccountModel?> = repository.getAccountById(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionModel>> = repository.getTransactionsForAccount(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val periodFilter = MutableStateFlow("الكل") // الكل, هذا الشهر, آخر 3 أشهر, هذا العام
    val typeFilter = MutableStateFlow("الكل") // الكل, وارد, صادر, سندات, تحويلات

    val filteredTransactions: StateFlow<List<TransactionModel>> = combine(
        transactions,
        periodFilter,
        typeFilter
    ) { list, period, type ->
        var result = list
        if (type != "الكل") {
            result = when (type) {
                "وارد" -> result.filter { it.type.isIncoming }
                "صادر" -> result.filter { !it.type.isIncoming }
                "سندات" -> result.filter { it.type.isVoucher }
                "تحويلات" -> result.filter { it.type.isTransfer }
                else -> result
            }
        }
        val now = System.currentTimeMillis()
        if (period != "الكل") {
            val millisInDay = 86400000L
            result = when (period) {
                "هذا الأسبوع" -> result.filter { it.date >= now - millisInDay * 7 }
                "هذا الشهر" -> result.filter { it.date >= now - millisInDay * 30 }
                "آخر 3 أشهر" -> result.filter { it.date >= now - millisInDay * 90 }
                "هذا العام" -> result.filter { it.date >= now - millisInDay * 365 }
                else -> result
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncoming: StateFlow<BigDecimal> = transactions.combine(account) { list, _ ->
        var sum = BigDecimal.ZERO
        list.forEach { if (it.type.isIncoming) sum = sum.add(it.amount) }
        sum
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val totalOutgoing: StateFlow<BigDecimal> = transactions.combine(account) { list, _ ->
        var sum = BigDecimal.ZERO
        list.forEach { if (!it.type.isIncoming) sum = sum.add(it.amount) }
        sum
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    fun exportPdf(context: Context): File? {
        val currentAccount = account.value ?: return null
        val currentTrans = transactions.value
        return PdfReportGenerator.generateAccountStatementPdf(context, currentAccount, currentTrans)
    }
}
