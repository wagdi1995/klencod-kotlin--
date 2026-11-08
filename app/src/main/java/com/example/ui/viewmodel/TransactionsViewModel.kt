package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FinancialRepository
import com.example.domain.model.AccountModel
import com.example.domain.model.CurrencyModel
import com.example.domain.model.TransactionModel
import com.example.domain.model.TransactionType
import com.example.domain.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class TransactionsViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    val accounts: StateFlow<List<AccountModel>> = repository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencies: StateFlow<List<CurrencyModel>> = repository.getAllCurrencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionModel>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("الكل")

    fun saveTransaction(
        id: Long = 0,
        accountId: Long,
        type: TransactionType,
        amountText: String,
        currencyCode: String,
        date: Long,
        title: String,
        description: String,
        notes: String,
        onComplete: (Boolean) -> Unit
    ) {
        val amount = CurrencyFormatter.parseAmount(amountText)
        if (accountId <= 0L || amount <= BigDecimal.ZERO || title.isBlank()) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val model = TransactionModel(
                id = id,
                accountId = accountId,
                type = type,
                amount = amount,
                currencyCode = currencyCode,
                date = date,
                title = title.trim(),
                description = description.trim(),
                notes = notes.trim()
            )
            if (id == 0L) {
                repository.insertTransaction(model)
            } else {
                repository.updateTransaction(model)
            }
            onComplete(true)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }
}
