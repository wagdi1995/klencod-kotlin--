package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CategoryEntity
import com.example.data.repository.FinancialRepository
import com.example.domain.model.AccountModel
import com.example.domain.model.AccountSortOrder
import com.example.domain.model.BalanceType
import com.example.domain.model.CurrencyModel
import com.example.domain.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class AccountsViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل")
    val sortOrder = MutableStateFlow(AccountSortOrder.RECENT)

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencies: StateFlow<List<CurrencyModel>> = repository.getAllCurrencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountModel>> = repository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAccounts: StateFlow<List<AccountModel>> = combine(
        accounts,
        searchQuery,
        selectedCategory,
        sortOrder
    ) { list, query, category, sort ->
        var result = list

        if (query.isNotBlank()) {
            result = result.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query) ||
                        it.accountNumber.contains(query)
            }
        }

        if (category != "الكل") {
            result = result.filter { it.category == category }
        }

        when (sort) {
            AccountSortOrder.RECENT -> result.sortedByDescending { it.id }
            AccountSortOrder.NAME -> result.sortedBy { it.name }
            AccountSortOrder.HIGHEST_BALANCE -> result.sortedByDescending { it.currentBalance }
            AccountSortOrder.LOWEST_BALANCE -> result.sortedBy { it.currentBalance }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveAccount(
        id: Long = 0,
        name: String,
        phone: String,
        category: String,
        amountText: String,
        balanceType: BalanceType,
        currencyCode: String,
        notes: String,
        accountNumber: String = "",
        onComplete: (Boolean) -> Unit
    ) {
        if (name.isBlank()) {
            onComplete(false)
            return
        }
        val amount = CurrencyFormatter.parseAmount(amountText)
        val model = AccountModel(
            id = id,
            name = name.trim(),
            phone = phone.trim(),
            category = category,
            openingBalance = amount,
            openingBalanceType = balanceType,
            currencyCode = currencyCode,
            notes = notes.trim(),
            accountNumber = accountNumber.ifBlank { (1000..9999).random().toString() }
        )
        viewModelScope.launch {
            if (id == 0L) {
                repository.insertAccount(model)
            } else {
                repository.updateAccount(model)
            }
            onComplete(true)
        }
    }

    fun deleteAccount(accountId: Long, force: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.deleteAccount(accountId, force)
            onResult(success)
        }
    }
}
