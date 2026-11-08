package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FinancialRepository
import com.example.domain.model.AccountModel
import com.example.domain.model.CurrencyModel
import com.example.domain.model.VoucherModel
import com.example.domain.model.VoucherType
import com.example.domain.util.CurrencyFormatter
import com.example.domain.util.PdfReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal

class VouchersViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    val vouchers: StateFlow<List<VoucherModel>> = repository.getAllVouchers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountModel>> = repository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencies: StateFlow<List<CurrencyModel>> = repository.getAllCurrencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val previewVoucher = MutableStateFlow<VoucherModel?>(null)

    fun getNextVoucherNumber(type: VoucherType, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val num = repository.getNextVoucherNumber(type)
            onResult(num)
        }
    }

    fun saveVoucher(
        voucherNumber: String,
        type: VoucherType,
        accountId: Long,
        amountText: String,
        currencyCode: String,
        date: Long,
        personName: String,
        description: String,
        notes: String,
        onComplete: (Boolean) -> Unit
    ) {
        val amount = CurrencyFormatter.parseAmount(amountText)
        val account = accounts.value.find { it.id == accountId }
        if (account == null || amount <= BigDecimal.ZERO || personName.isBlank()) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val model = VoucherModel(
                voucherNumber = voucherNumber,
                type = type,
                accountId = accountId,
                accountName = account.name,
                amount = amount,
                currencyCode = currencyCode,
                date = date,
                personName = personName.trim(),
                description = description.trim(),
                notes = notes.trim()
            )
            val id = repository.createVoucher(model)
            onComplete(id > 0)
        }
    }

    fun deleteVoucher(voucherId: Long) {
        viewModelScope.launch {
            repository.deleteVoucher(voucherId)
        }
    }

    fun exportVoucherPdf(context: Context, voucher: VoucherModel): File? {
        return PdfReportGenerator.generateVoucherPdf(context, voucher)
    }
}
