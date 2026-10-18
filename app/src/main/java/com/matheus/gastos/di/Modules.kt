package com.matheus.gastos.di

import android.content.Context
import androidx.room.Room
import com.matheus.gastos.data.dao.AppSettingsDao
import com.matheus.gastos.data.dao.BudgetLimitDao
import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.dao.CreditCardDao
import com.matheus.gastos.data.dao.SubcategoryDao
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.local.Converters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context.applicationContext,
        com.matheus.gastos.data.local.AppDatabase::class.java,
        "gastos-db"
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideCategoryDao(db: com.matheus.gastos.data.local.AppDatabase) = db.categoryDao()

    @Provides
    fun provideSubcategoryDao(db: com.matheus.gastos.data.local.AppDatabase) = db.subcategoryDao()

    @Provides
    fun provideCreditCardDao(db: com.matheus.gastos.data.local.AppDatabase) = db.creditCardDao()

    @Provides
    fun provideTransactionDao(db: com.matheus.gastos.data.local.AppDatabase) = db.transactionDao()

    @Provides
    fun provideBudgetLimitDao(db: com.matheus.gastos.data.local.AppDatabase) = db.budgetLimitDao()

    @Provides
    fun provideAppSettingsDao(db: com.matheus.gastos.data.local.AppDatabase) = db.appSettingsDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCategoryRepository(dao: CategoryDao) = com.matheus.gastos.data.repository.CategoryRepository(dao)

    @Provides
    @Singleton
    fun provideSubcategoryRepository(dao: SubcategoryDao) = com.matheus.gastos.data.repository.SubcategoryRepository(dao)

    @Provides
    @Singleton
    fun provideCreditCardRepository(dao: CreditCardDao) = com.matheus.gastos.data.repository.CreditCardRepository(dao)

    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao) = com.matheus.gastos.data.repository.TransactionRepository(dao)

    @Provides
    @Singleton
    fun provideBudgetLimitRepository(
        dao: BudgetLimitDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
    ) = com.matheus.gastos.data.repository.BudgetLimitRepository(dao, categoryDao, transactionDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(dao: AppSettingsDao) = com.matheus.gastos.data.repository.SettingsRepository(dao)
}

