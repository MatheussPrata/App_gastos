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
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@Suppress("UNUSED_PARAMETER") context: Context) = Room.databaseBuilder(
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
    fun provideBudgetLimitRepository(dao: BudgetLimitDao) = com.matheus.gastos.data.repository.BudgetLimitRepository(dao)

    @Provides
    @Singleton
    fun provideSettingsRepository(dao: AppSettingsDao) = com.matheus.gastos.data.repository.SettingsRepository(dao)
}

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

    @Provides
    @Singleton
    fun provideCategoryViewModel(
        application: android.app.Application,
        repository: com.matheus.gastos.data.repository.CategoryRepository
    ): com.matheus.gastos.viewmodel.CategoryViewModel = com.matheus.gastos.viewmodel.CategoryViewModel(application, repository)

    @Provides
    @Singleton
    fun provideCreditCardViewModel(
        application: android.app.Application,
        repository: com.matheus.gastos.data.repository.CreditCardRepository
    ): com.matheus.gastos.viewmodel.CreditCardViewModel = com.matheus.gastos.viewmodel.CreditCardViewModel(application, repository)

    @Provides
    @Singleton
    fun provideTransactionViewModel(
        application: android.app.Application,
        repository: com.matheus.gastos.data.repository.TransactionRepository
    ): com.matheus.gastos.viewmodel.TransactionViewModel = com.matheus.gastos.viewmodel.TransactionViewModel(application, repository)

    @Provides
    @Singleton
    fun provideBudgetLimitViewModel(
        application: android.app.Application,
        repository: com.matheus.gastos.data.repository.BudgetLimitRepository
    ): com.matheus.gastos.viewmodel.BudgetLimitViewModel = com.matheus.gastos.viewmodel.BudgetLimitViewModel(application, repository)

    @Provides
    @Singleton
    fun provideSettingsViewModel(
        application: android.app.Application,
        repository: com.matheus.gastos.data.repository.SettingsRepository
    ): com.matheus.gastos.viewmodel.SettingsViewModel = com.matheus.gastos.viewmodel.SettingsViewModel(application, repository)
}
