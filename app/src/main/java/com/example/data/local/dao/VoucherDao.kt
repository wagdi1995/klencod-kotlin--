package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.VoucherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers ORDER BY date DESC, id DESC")
    fun getAllVouchers(): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers WHERE id = :id")
    fun getVoucherById(id: Long): Flow<VoucherEntity?>

    @Query("SELECT * FROM vouchers WHERE accountId = :accountId ORDER BY date DESC")
    fun getVouchersForAccount(accountId: Long): Flow<List<VoucherEntity>>

    @Query("SELECT voucherNumber FROM vouchers WHERE type = :type ORDER BY id DESC LIMIT 1")
    suspend fun getLatestVoucherNumber(type: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity): Long

    @Update
    suspend fun updateVoucher(voucher: VoucherEntity)

    @Delete
    suspend fun deleteVoucher(voucher: VoucherEntity)

    @Query("DELETE FROM vouchers WHERE id = :id")
    suspend fun deleteVoucherById(id: Long)

    @Query("DELETE FROM vouchers WHERE transactionId = :transactionId")
    suspend fun deleteVoucherByTransactionId(transactionId: Long)
}
