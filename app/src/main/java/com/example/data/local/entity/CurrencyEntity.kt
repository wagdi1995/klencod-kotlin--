package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey val code: String, // e.g. YER, SAR, USD
    val name: String,
    val symbol: String,
    val exchangeRateToMain: Double = 1.0,
    val isMain: Boolean = false,
    val decimalPlaces: Int = 2,
    val isEnabled: Boolean = true
)
