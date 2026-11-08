package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CurrencyDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.VoucherDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CurrencyEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.VoucherEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        VoucherEntity::class,
        CurrencyEntity::class,
        CategoryEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun voucherDao(): VoucherDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisabati_database.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val currencyDao = database.currencyDao()
            val categoryDao = database.categoryDao()
            val accountDao = database.accountDao()
            val transactionDao = database.transactionDao()
            val voucherDao = database.voucherDao()
            val settingsDao = database.settingsDao()

            // 1. Settings
            settingsDao.insertOrUpdate(
                AppSettingsEntity(
                    id = 1,
                    appName = "حساباتي",
                    mainCurrencyCode = "SAR",
                    decimalPrecision = 2,
                    notificationEnabled = true
                )
            )

            // 2. Currencies
            currencyDao.insertAll(
                listOf(
                    CurrencyEntity("SAR", "ريال سعودي", "ر.س", 1.0, isMain = true, decimalPlaces = 2),
                    CurrencyEntity("YER", "ريال يمني", "ر.ي", 0.0015, isMain = false, decimalPlaces = 2),
                    CurrencyEntity("USD", "دولار أمريكي", "$", 3.75, isMain = false, decimalPlaces = 2)
                )
            )

            // 3. Categories
            categoryDao.insertAll(
                listOf(
                    CategoryEntity(id = 1, name = "عملاء", iconKey = "business", isDefault = true),
                    CategoryEntity(id = 2, name = "موردون", iconKey = "store", isDefault = true),
                    CategoryEntity(id = 3, name = "موظفون", iconKey = "person", isDefault = true),
                    CategoryEntity(id = 4, name = "أخرى", iconKey = "category", isDefault = true)
                )
            )

            // 4. Accounts matching screenshots
            val acc1Id = accountDao.insertAccount(
                AccountEntity(
                    name = "شركة النور",
                    phone = "+966 50 123 4567",
                    category = "عملاء",
                    openingBalance = "5000.00",
                    openingBalanceType = "FOR_ME",
                    currencyCode = "SAR",
                    accountNumber = "4587",
                    notes = "عميل مميز توريدات شهرية"
                )
            )

            val acc2Id = accountDao.insertAccount(
                AccountEntity(
                    name = "مؤسسة الأفق",
                    phone = "+966 55 987 6543",
                    category = "موردون",
                    openingBalance = "2600.00",
                    openingBalanceType = "ON_ME",
                    currencyCode = "SAR",
                    accountNumber = "7643",
                    notes = "مورد أجهزة ومستلزمات مكتبية"
                )
            )

            val acc3Id = accountDao.insertAccount(
                AccountEntity(
                    name = "سعيد خالد",
                    phone = "+966 53 246 8101",
                    category = "موظفون",
                    openingBalance = "0.00",
                    openingBalanceType = "FOR_ME",
                    currencyCode = "SAR",
                    accountNumber = "8101",
                    notes = "مدير المبيعات"
                )
            )

            val acc4Id = accountDao.insertAccount(
                AccountEntity(
                    name = "متجر الإبداع",
                    phone = "+966 54 111 2233",
                    category = "عملاء",
                    openingBalance = "40050.00",
                    openingBalanceType = "FOR_ME",
                    currencyCode = "SAR",
                    accountNumber = "2233",
                    notes = "متجر تجزئة"
                )
            )

            val cal = Calendar.getInstance()
            val now = cal.timeInMillis

            // 5. Account 1 transactions: Opening balance 5000 (FOR_ME).
            // Let's add transactions that sum to 12,300 current balance:
            // 5000 + 9800 (invoice/sales) - 2500 (payment) = 12,300.
            val trans1Id = transactionDao.insertTransaction(
                TransactionEntity(
                    accountId = acc1Id,
                    type = "OUTGOING",
                    amount = "9800.00",
                    currencyCode = "SAR",
                    date = now - 86400000L * 5,
                    title = "فاتورة مبيعات",
                    description = "توريد بضاعة للمستودع الرئيسي"
                )
            )

            val trans2Id = transactionDao.insertTransaction(
                TransactionEntity(
                    accountId = acc1Id,
                    type = "RECEIPT_VOUCHER",
                    amount = "2500.00",
                    currencyCode = "SAR",
                    date = now - 86400000L * 2,
                    title = "دفعة فاتورة",
                    description = "تحويل وارد دفعة مقدمة",
                    voucherNumber = "ق-2026-0001"
                )
            )

            voucherDao.insertVoucher(
                VoucherEntity(
                    voucherNumber = "ق-2026-0001",
                    type = "RECEIPT",
                    accountId = acc1Id,
                    accountName = "شركة النور",
                    amount = "2500.00",
                    currencyCode = "SAR",
                    date = now - 86400000L * 2,
                    personName = "أحمد عبد الله",
                    description = "دفعة فاتورة بضاعة",
                    transactionId = trans2Id
                )
            )

            // 6. Account 2: -2600 (ON_ME). Add -1500 -> total -4100.
            val trans3Id = transactionDao.insertTransaction(
                TransactionEntity(
                    accountId = acc2Id,
                    type = "PAYMENT_VOUCHER",
                    amount = "1500.00",
                    currencyCode = "SAR",
                    date = now - 86400000L * 1,
                    title = "سند صرف",
                    description = "شراء مستلزمات مكتبية",
                    voucherNumber = "ص-2026-0001"
                )
            )

            voucherDao.insertVoucher(
                VoucherEntity(
                    voucherNumber = "ص-2026-0001",
                    type = "PAYMENT",
                    accountId = acc2Id,
                    accountName = "مؤسسة الأفق",
                    amount = "1500.00",
                    currencyCode = "SAR",
                    date = now - 86400000L * 1,
                    personName = "محمد العلي",
                    description = "سند صرف مستلزمات",
                    transactionId = trans3Id
                )
            )

            // Monthly distribution for bar chart (last 6 months: Nov, Dec, Jan, Feb, Mar, Apr)
            val monthOffsets = listOf(
                Pair(5, Pair("14000.00", "7000.00")),
                Pair(4, Pair("22000.00", "13000.00")),
                Pair(3, Pair("26000.00", "10000.00")),
                Pair(2, Pair("16000.00", "8000.00")),
                Pair(1, Pair("19000.00", "11000.00")),
                Pair(0, Pair("24000.00", "10500.00"))
            )

            monthOffsets.forEach { (offset, amounts) ->
                val mCal = Calendar.getInstance()
                mCal.add(Calendar.MONTH, -offset)
                mCal.set(Calendar.DAY_OF_MONTH, 15)
                val mTime = mCal.timeInMillis

                transactionDao.insertTransaction(
                    TransactionEntity(
                        accountId = acc4Id,
                        type = "INCOMING",
                        amount = amounts.first,
                        currencyCode = "SAR",
                        date = mTime,
                        title = "مبيعات توريد",
                        description = "حركة تجارية"
                    )
                )
                transactionDao.insertTransaction(
                    TransactionEntity(
                        accountId = acc2Id,
                        type = "OUTGOING",
                        amount = amounts.second,
                        currencyCode = "SAR",
                        date = mTime + 3600000L,
                        title = "مشتريات مستلزمات",
                        description = "حركة تجارية"
                    )
                )
            }
        }
    }
}
