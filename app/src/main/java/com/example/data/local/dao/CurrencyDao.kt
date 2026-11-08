package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies WHERE isEnabled = 1")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies")
    fun getAllCurrenciesIncludingDisabled(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies WHERE code = :code")
    suspend fun getCurrencyByCode(code: String): CurrencyEntity?

    @Query("SELECT * FROM currencies WHERE isMain = 1 LIMIT 1")
    fun getMainCurrency(): Flow<CurrencyEntity?>

    @Query("SELECT * FROM currencies WHERE isMain = 1 LIMIT 1")
    suspend fun getMainCurrencyDirect(): CurrencyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: CurrencyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(currencies: List<CurrencyEntity>)

    @Update
    suspend fun updateCurrency(currency: CurrencyEntity)

    @Delete
    suspend fun deleteCurrency(currency: CurrencyEntity)

    @Query("UPDATE currencies SET isMain = 0")
    suspend fun clearMainCurrency()

    @Query("UPDATE currencies SET isMain = 1 WHERE code = :code")
    suspend fun setMainCurrency(code: String)
}
