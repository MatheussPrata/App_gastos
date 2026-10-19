package com.matheus.gastos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matheus.gastos.data.dao.AppSettingsDao
import com.matheus.gastos.data.dao.BudgetLimitDao
import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.dao.CreditCardDao
import com.matheus.gastos.data.dao.SubcategoryDao
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.AppSettings
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.BudgetPeriod
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.Transaction

@Database(
    entities = [
        Category::class,
        Subcategory::class,
        CreditCard::class,
        Transaction::class,
        BudgetLimit::class,
        AppSettings::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun subcategoryDao(): SubcategoryDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetLimitDao(): BudgetLimitDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v2 -> v3: cartao ganhou foto personalizada (estilo carteira).
         * Migracao real (ALTER TABLE) para NAO apagar os dados do usuario.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE credit_cards ADD COLUMN imageUri TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gastos-db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
