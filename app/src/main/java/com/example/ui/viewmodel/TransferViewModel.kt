package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FinancialRepository
import com.example.domain.model.AccountModel
import com.example.domain.model.CurrencyModel
import com.example.domain.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class TransferViewModel(
    private val repository: FinancialRepository,
    initialSourceId: Long? = null
) : ViewModel() {

    val accounts: StateFlow<List<AccountModel>> = repository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencies: StateFlow<List<CurrencyModel>> = repository.getAllCurrencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sourceAccount = MutableStateFlow<AccountModel?>(null)
    val destinationAccount = MutableStateFlow<AccountModel?>(null)

    val amountInput = MutableStateFlow("")
    val selectedCurrency = MutableStateFlow("SAR")
    val exchangeRateInput = MutableStateFlow("1.0")
    val transferDate = MutableStateFlow(System.currentTimeMillis())
    val transferDescription = MutableStateFlow("")
    val transferNotes = MutableStateFlow("")

    val isReviewing = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val list = accounts.first { it.isNotEmpty() }
            if (initialSourceId != null) {
                sourceAccount.value = list.find { it.id == initialSourceId } ?: list.firstOrNull()
                destinationAccount.value = list.find { it.id != initialSourceId }
            } else {
                if (list.size >= 2) {
                    sourceAccount.value = list[0]
                    destinationAccount.value = list[1]
                } else if (list.isNotEmpty()) {
                    sourceAccount.value = list[0]
                }
            }
            sourceAccount.value?.let {
                selectedCurrency.value = it.currencyCode
            }
        }
    }

    fun swapAccounts() {
        val temp = sourceAccount.value
        sourceAccount.value = destinationAccount.value
        destinationAccount.value = temp
        sourceAccount.value?.let {
            selectedCurrency.value = it.currencyCode
        }
    }

    fun executeTransfer(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val src = sourceAccount.value
        val dest = destinationAccount.value
        if (src == null || dest == null) {
            onError("يرجى اختيار حساب المصدر والوجهة")
            return
        }
        if (src.id == dest.id) {
            onError("لا يمكن التحويل لنفس الحساب")
            return
        }
        val amount = CurrencyFormatter.parseAmount(amountInput.value)
        if (amount <= BigDecimal.ZERO) {
            onError("يرجى إدخال مبلغ صحيح")
            return
        }

        val rate = exchangeRateInput.value.toDoubleOrNull() ?: 1.0

        viewModelScope.launch {
            val success = repository.performTransfer(
                sourceAccountId = src.id,
                destinationAccountId = dest.id,
                amount = amount,
                currencyCode = selectedCurrency.value,
                exchangeRate = rate,
                date = transferDate.value,
                description = transferDescription.value.ifBlank { "تحويل من ${src.name} إلى ${dest.name}" },
                notes = transferNotes.value
            )
            if (success) {
                onSuccess()
            } else {
                onError("فشلت عملية التحويل، يرجى المحاولة لاحقاً")
            }
        }
    }
}
