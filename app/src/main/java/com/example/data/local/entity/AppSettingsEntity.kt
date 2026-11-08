package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val appName: String = "حساباتي",
    val mainCurrencyCode: String = "YER",
    val decimalPrecision: Int = 2,
    val notificationEnabled: Boolean = true,
    val backupReminderDays: Int = 7
)
