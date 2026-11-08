package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["name"]), Index(value = ["category"])]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val category: String = "عملاء",
    val openingBalance: String = "0.00",
    val openingBalanceType: String = "FOR_ME", // "FOR_ME" or "ON_ME"
    val currencyCode: String = "YER",
    val notes: String = "",
    val accountNumber: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
