package com.example.domain.model

enum class BalanceType(val labelArabic: String, val sign: Int) {
    FOR_ME("لك", 1),
    ON_ME("عليك", -1);

    companion object {
        fun fromString(value: String): BalanceType {
            return when (value.uppercase()) {
                "FOR_ME", "لك" -> FOR_ME
                "ON_ME", "عليك" -> ON_ME
                else -> FOR_ME
            }
        }
    }
}

enum class TransactionType(
    val labelArabic: String,
    val isIncoming: Boolean,
    val isTransfer: Boolean = false,
    val isVoucher: Boolean = false
) {
    INCOMING("وارد", true),
    OUTGOING("صادر", false),
    RECEIPT_VOUCHER("سند قبض", true, isVoucher = true),
    PAYMENT_VOUCHER("سند صرف", false, isVoucher = true),
    TRANSFER_IN("تحويل وارد", true, isTransfer = true),
    TRANSFER_OUT("تحويل صادر", false, isTransfer = true),
    DEBIT_NOTE("إشعار مدين", false),
    CREDIT_NOTE("إشعار دائن", true);

    companion object {
        fun fromString(value: String): TransactionType {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.labelArabic == value }
                ?: INCOMING
        }
    }
}

enum class VoucherType(val labelArabic: String, val prefix: String) {
    RECEIPT("سند قبض", "ق-"),
    PAYMENT("سند صرف", "ص-");

    companion object {
        fun fromString(value: String): VoucherType {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.labelArabic == value }
                ?: RECEIPT
        }
    }
}

enum class AccountSortOrder(val labelArabic: String) {
    RECENT("الأحدث"),
    NAME("الاسم أبجدياً"),
    HIGHEST_BALANCE("الأعلى رصيداً"),
    LOWEST_BALANCE("الأقل رصيداً")
}
