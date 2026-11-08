package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FinancialRepository
import com.example.domain.model.AccountModel
import com.example.domain.model.CurrencyModel
import com.example.domain.model.FinancialSummary
import com.example.domain.model.TransactionModel
import com.example.domain.model.TransactionType
import com.example.domain.util.PdfReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.io.File
import java.math.BigDecimal
import java.util.Calendar

class ReportsViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    val accounts: StateFlow<List<AccountModel>> = repository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencies: StateFlow<List<CurrencyModel>> = repository.getAllCurrencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionModel>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialSummary: StateFlow<FinancialSummary> = repository.getFinancialSummary()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, "ر.س")
        )

    val selectedReportType = MutableStateFlow("كشف حساب") // كشف حساب, التقرير الشهري, تقرير الوارد, تقرير الصادر, ملخص الأرصدة, تقرير السندات, تقرير التحويلات
    val selectedAccountId = MutableStateFlow<Long?>(null)
    val selectedPeriod = MutableStateFlow("الكل") // الكل, هذا الشهر, آخر 3 أشهر, هذا العام
    val selectedCurrencyCode = MutableStateFlow("الكل")

    val filteredReportTransactions: StateFlow<List<TransactionModel>> = combine(
        allTransactions,
        selectedReportType,
        selectedAccountId,
        selectedPeriod,
        selectedCurrencyCode
    ) { list, reportType, accountId, period, currency ->
        var result = list

        if (accountId != null && accountId > 0L) {
            result = result.filter { it.accountId == accountId }
        }

        if (currency != "الكل") {
            result = result.filter { it.currencyCode == currency }
        }

        when (reportType) {
            "تقرير الوارد" -> result = result.filter { it.type.isIncoming }
            "تقرير الصادر" -> result = result.filter { !it.type.isIncoming }
            "تقرير السندات" -> result = result.filter { it.type.isVoucher }
            "تقرير التحويلات" -> result = result.filter { it.type.isTransfer }
        }

        val now = System.currentTimeMillis()
        val millisInDay = 86400000L
        when (period) {
            "هذا الأسبوع" -> result = result.filter { it.date >= now - millisInDay * 7 }
            "هذا الشهر" -> result = result.filter { it.date >= now - millisInDay * 30 }
            "آخر 3 أشهر" -> result = result.filter { it.date >= now - millisInDay * 90 }
            "هذا العام" -> result = result.filter { it.date >= now - millisInDay * 365 }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportIncomingSum: StateFlow<BigDecimal> = filteredReportTransactions.combine(selectedReportType) { list, _ ->
        var sum = BigDecimal.ZERO
        list.forEach { if (it.type.isIncoming) sum = sum.add(it.amount) }
        sum
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val reportOutgoingSum: StateFlow<BigDecimal> = filteredReportTransactions.combine(selectedReportType) { list, _ ->
        var sum = BigDecimal.ZERO
        list.forEach { if (!it.type.isIncoming) sum = sum.add(it.amount) }
        sum
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    fun exportCurrentReportPdf(context: Context): File? {
        val acctId = selectedAccountId.value
        val acct = accounts.value.find { it.id == acctId } ?: accounts.value.firstOrNull() ?: return null
        return PdfReportGenerator.generateAccountStatementPdf(context, acct, filteredReportTransactions.value)
    }
}
