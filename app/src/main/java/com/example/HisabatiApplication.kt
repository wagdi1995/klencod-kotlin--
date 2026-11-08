package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.BackupRestoreManager
import com.example.data.repository.FinancialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class HisabatiApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this, applicationScope)
    }

    val repository: FinancialRepository by lazy {
        FinancialRepository(database)
    }

    val backupManager: BackupRestoreManager by lazy {
        BackupRestoreManager(this, database)
    }
}
