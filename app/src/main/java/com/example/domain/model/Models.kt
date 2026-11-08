package com.example.domain.model

import java.math.BigDecimal

data class AccountModel(
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val category: String = "عملاء",
    val openingBalance: BigDecimal = BigDecimal.ZERO,
    val openingBalanceType: BalanceType = BalanceType.FOR_ME,
    val currencyCode: String = "YER",
    val notes: String = "",
    val accountNumber: String = "",
    val currentBalance: BigDecimal = BigDecimal.ZERO,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isPositive: Boolean get() = currentBalance > BigDecimal.ZERO
    val isNegative: Boolean get() = currentBalance < BigDecimal.ZERO
    val isZero: Boolean get() = currentBalance.compareTo(BigDecimal.ZERO) == 0

    val balanceStatusLabel: String
        get() = when {
            isPositive -> "لك"
            isNegative -> "عليك"
            else -> "خالص"
        }
}

data class TransactionModel(
    val id: Long = 0,
    val accountId: Long,
    val accountName: String = "",
    val accountCategory: String = "",
    val type: TransactionType,
    val amount: BigDecimal,
    val currencyCode: String,
    val date: Long,
    val title: String,
    val description: String = "",
    val notes: String = "",
    val transferRefId: String? = null,
    val voucherNumber: String? = null,
    val runningBalance: BigDecimal = BigDecimal.ZERO,
    val createdAt: Long = System.currentTimeMillis()
)

data class VoucherModel(
    val id: Long = 0,
    val voucherNumber: String,
    val type: VoucherType,
    val accountId: Long,
    val accountName: String,
    val amount: BigDecimal,
    val currencyCode: String,
    val date: Long,
    val personName: String,
    val description: String,
    val notes: String = "",
    val transactionId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class CurrencyModel(
    val code: String,
    val name: String,
    val symbol: String,
    val exchangeRateToMain: Double = 1.0,
    val isMain: Boolean = false,
    val decimalPlaces: Int = 2,
    val isEnabled: Boolean = true
)

data class MonthlyStat(
    val monthLabel: String,
    val monthIndex: Int,
    val year: Int,
    val totalIncoming: BigDecimal,
    val totalOutgoing: BigDecimal
)

data class FinancialSummary(
    val totalBalance: BigDecimal,
    val totalForMe: BigDecimal,
    val totalOnMe: BigDecimal,
    val accountsCount: Int,
    val mainCurrencySymbol: String
)
