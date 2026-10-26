package com.matheus.gastos.di

import android.content.Context
import androidx.room.Room
import com.matheus.gastos.data.local.AppDatabase
import com.matheus.gastos.data.dao.*
import com.matheus.gastos.data.repository.*
import com.matheus.gastos.data.backup.DriveBackupManager
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
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gastos.db"
        ).fallbackToDestructiveMigration().build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    
    @Provides
    fun provideSubcategoryDao(db: AppDatabase): SubcategoryDao = db.subcategoryDao()
    
    @Provides
    fun provideCreditCardDao(db: AppDatabase): CreditCardDao = db.creditCardDao()
    
    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    
    @Provides
    fun provideBudgetLimitDao(db: AppDatabase): BudgetLimitDao = db.budgetLimitDao()
    
    @Provides
    fun provideAppSettingsDao(db: AppDatabase): AppSettingsDao = db.appSettingsDao()
    
    // NEW DAOs for sharing
    @Provides
    fun provideConnectionDao(db: AppDatabase): ConnectionDao = db.connectionDao()
    
    @Provides
    fun provideRecurringTransactionDao(db: AppDatabase): RecurringTransactionDao = db.recurringTransactionDao()
    
    @Provides
    fun provideShoppingItemDao(db: AppDatabase): ShoppingItemDao = db.shoppingItemDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        subcategoryDao: SubcategoryDao
    ): CategoryRepository = CategoryRepository(categoryDao, subcategoryDao)

    @Provides
    @Singleton
    fun provideCreditCardRepository(creditCardDao: CreditCardDao): CreditCardRepository = CreditCardRepository(creditCardDao)

    @Provides
    @Singleton
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository = TransactionRepository(transactionDao)

    @Provides
    @Singleton
    fun provideBudgetLimitRepository(
        budgetLimitDao: BudgetLimitDao,
        categoryRepository: CategoryRepository
    ): BudgetLimitRepository = BudgetLimitRepository(budgetLimitDao, categoryRepository)

    @Provides
    @Singleton
    fun provideSettingsRepository(appSettingsDao: AppSettingsDao): SettingsRepository = SettingsRepository(appSettingsDao)

    @Provides
    @Singleton
    fun provideDriveBackupManager(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): DriveBackupManager = DriveBackupManager(context)

    // NEW Repositories for sharing
    @Provides
    @Singleton
    fun provideConnectionRepository(
        connectionDao: ConnectionDao,
        settingsRepository: SettingsRepository
    ): ConnectionRepository = ConnectionRepository(connectionDao, settingsRepository)

    @Provides
    @Singleton
    fun provideRecurringTransactionRepository(
        recurringTransactionDao: RecurringTransactionDao,
        transactionRepository: TransactionRepository,
        settingsRepository: SettingsRepository
    ): RecurringTransactionRepository = RecurringTransactionRepository(
        recurringTransactionDao, transactionRepository, settingsRepository
    )

    @Provides
    @Singleton
    fun provideShoppingRepository(
        shoppingItemDao: ShoppingItemDao,
        settingsRepository: SettingsRepository
    ): ShoppingRepository = ShoppingRepository(shoppingItemDao, settingsRepository)
}