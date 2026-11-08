package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["date"]),
        Index(value = ["type"]),
        Index(value = ["transferRefId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val type: String, // INCOMING, OUTGOING, RECEIPT_VOUCHER, PAYMENT_VOUCHER, TRANSFER_IN, TRANSFER_OUT, DEBIT_NOTE, CREDIT_NOTE
    val amount: String, // Stored as BigDecimal string representation
    val currencyCode: String = "YER",
    val date: Long = System.currentTimeMillis(),
    val title: String,
    val description: String = "",
    val notes: String = "",
    val transferRefId: String? = null,
    val voucherNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
