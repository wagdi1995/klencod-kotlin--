package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.HisabatiApplication
import com.example.data.repository.BackupRestoreManager
import com.example.data.repository.FinancialRepository

class ViewModelFactory(
    private val application: Application,
    private val repository: FinancialRepository,
    private val backupManager: BackupRestoreManager,
    private val extraId: Long? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AccountsViewModel::class.java) -> {
                AccountsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AccountDetailViewModel::class.java) -> {
                AccountDetailViewModel(repository, extraId ?: 0L) as T
            }
            modelClass.isAssignableFrom(TransferViewModel::class.java) -> {
                TransferViewModel(repository, extraId) as T
            }
            modelClass.isAssignableFrom(TransactionsViewModel::class.java) -> {
                TransactionsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(VouchersViewModel::class.java) -> {
                VouchersViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ReportsViewModel::class.java) -> {
                ReportsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(repository, backupManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun createFactory(application: Application, extraId: Long? = null): ViewModelFactory {
            val app = application as HisabatiApplication
            return ViewModelFactory(app, app.repository, app.backupManager, extraId)
        }
    }
}
