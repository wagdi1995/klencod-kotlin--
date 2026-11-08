package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CategoryEntity
import com.example.data.repository.BackupRestoreManager
import com.example.data.repository.FinancialRepository
import com.example.domain.model.CurrencyModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val repository: FinancialRepository,
    private val backupManager: BackupRestoreManager
) : ViewModel() {

    val currencies: StateFlow<List<CurrencyModel>> = repository.getAllCurrencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mainCurrency: StateFlow<CurrencyModel?> = repository.getMainCurrency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backupStatusMessage = MutableStateFlow<String?>(null)
    val latestBackupFile = MutableStateFlow<File?>(null)

    fun setMainCurrency(code: String) {
        viewModelScope.launch {
            repository.setMainCurrency(code)
        }
    }

    fun addOrUpdateCurrency(
        code: String,
        name: String,
        symbol: String,
        exchangeRate: Double,
        isMain: Boolean = false
    ) {
        viewModelScope.launch {
            val model = CurrencyModel(
                code = code.uppercase().trim(),
                name = name.trim(),
                symbol = symbol.trim(),
                exchangeRateToMain = exchangeRate,
                isMain = isMain
            )
            repository.insertOrUpdateCurrency(model)
        }
    }

    fun addCategory(name: String, iconKey: String = "category") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertCategory(name.trim(), iconKey)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun createBackup(onSuccess: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val file = backupManager.createBackupJson()
                latestBackupFile.value = file
                backupStatusMessage.value = "تم إنشاء النسخة الاحتياطية بنجاح"
                onSuccess(file)
            } catch (e: Exception) {
                backupStatusMessage.value = "خطأ في إنشاء النسخة: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "حدث خطأ")
            }
        }
    }

    fun restoreBackup(file: File, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = file.readText()
                val result = backupManager.restoreBackup(json)
                if (result.isSuccess) {
                    onComplete(true, "تمت استعادة ${result.getOrNull()} سجلاً بنجاح")
                } else {
                    onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "فشل استعادة البيانات")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "فشل قراءة الملف")
            }
        }
    }
}
