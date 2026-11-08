package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FinancialRepository
import com.example.domain.model.FinancialSummary
import com.example.domain.model.MonthlyStat
import com.example.domain.model.TransactionModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal

class HomeViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    val financialSummary: StateFlow<FinancialSummary> = repository.getFinancialSummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FinancialSummary(
                totalBalance = BigDecimal.ZERO,
                totalForMe = BigDecimal.ZERO,
                totalOnMe = BigDecimal.ZERO,
                accountsCount = 0,
                mainCurrencySymbol = "ر.س"
            )
        )

    val monthlyStats: StateFlow<List<MonthlyStat>> = repository.getMonthlyStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentTransactions: StateFlow<List<TransactionModel>> = repository.getRecentTransactions(limit = 6)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
