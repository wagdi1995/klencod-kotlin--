package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vouchers",
    indices = [
        Index(value = ["voucherNumber"], unique = true),
        Index(value = ["accountId"]),
        Index(value = ["date"])
    ]
)
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherNumber: String,
    val type: String, // RECEIPT or PAYMENT
    val accountId: Long,
    val accountName: String,
    val amount: String,
    val currencyCode: String = "YER",
    val date: Long = System.currentTimeMillis(),
    val personName: String,
    val description: String = "",
    val notes: String = "",
    val transactionId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
