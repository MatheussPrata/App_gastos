package com.matheus.gastos.data.dao

import androidx.room.*
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.AppSettings
import com.matheus.gastos.data.entity.Connection
import com.matheus.gastos.data.entity.RecurringTransaction
import com.matheus.gastos.data.entity.RecurringFrequency
import com.matheus.gastos.data.entity.ShoppingItem
import com.matheus.gastos.data.entity.CategoryExpense
import com.matheus.gastos.data.entity.CardExpense
import com.matheus.gastos.data.entity.BudgetLimitWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category): Int

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM categories ORDER BY sortOrder, name")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?

    @Query("SELECT * FROM categories WHERE isDefault = 1 ORDER BY sortOrder, name")
    fun getDefaults(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isDefault = 0 ORDER BY sortOrder, name")
    fun getCustom(): Flow<List<Category>>
}

@Dao
interface SubcategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(subcategory: Subcategory): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(subcategories: List<Subcategory>)

    @Update
    suspend fun update(subcategory: Subcategory): Int

    @Delete
    suspend fun delete(subcategory: Subcategory)

    @Query("DELETE FROM subcategories WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM subcategories WHERE categoryId = :categoryId")
    suspend fun deleteByCategoryId(categoryId: Long): Int

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY sortOrder, name")
    fun getByCategoryId(categoryId: Long): Flow<List<Subcategory>>

    @Query("SELECT * FROM subcategories WHERE id = :id")
    suspend fun getById(id: Long): Subcategory?
}

@Dao
interface CreditCardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(card: CreditCard): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cards: List<CreditCard>)

    @Update
    suspend fun update(card: CreditCard): Int

    @Delete
    suspend fun delete(card: CreditCard)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM credit_cards WHERE isActive = 1 ORDER BY sortOrder, name")
    fun getActive(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards ORDER BY sortOrder, name")
    fun getAll(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getById(id: Long): CreditCard?
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction): Int

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM transactions WHERE installmentGroupId = :groupId")
    suspend fun deleteByInstallmentGroup(groupId: String): Int

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE creditCardId = :cardId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getByCardAndDateRange(cardId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isInstallment = 1 AND installmentGroupId = :groupId ORDER BY installmentNumber")
    fun getInstallmentsByGroup(groupId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isInstallment = 1 AND installmentNumber > 1 AND date > :currentDate ORDER BY date")
    fun getFutureInstallments(currentDate: Long): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE isExpense = 1 AND date >= :startDate AND date <= :endDate")
    fun getTotalExpenses(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isExpense = 0 AND date >= :startDate AND date <= :endDate")
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE isExpense = 1 AND date >= :startDate AND date <= :endDate AND categoryId IS NOT NULL GROUP BY categoryId ORDER BY total DESC")
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategoryExpense>>

    @Query("SELECT creditCardId, SUM(amount) as total FROM transactions WHERE isExpense = 1 AND date >= :startDate AND date <= :endDate AND creditCardId IS NOT NULL GROUP BY creditCardId ORDER BY total DESC")
    fun getExpensesByCard(startDate: Long, endDate: Long): Flow<List<CardExpense>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE ownerId = :ownerId ORDER BY date DESC")
    fun getByOwnerId(ownerId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE partnerSynced = 0 ORDER BY date DESC")
    fun getUnsynced(): Flow<List<Transaction>>

    @RawQuery(observedEntities = [Transaction::class])
    fun getAllTransactions(query: SimpleSQLiteQuery): Flow<List<Transaction>>
}

@Dao
interface BudgetLimitDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(limit: BudgetLimit): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(limits: List<BudgetLimit>)

    @Update
    suspend fun update(limit: BudgetLimit): Int

    @Delete
    suspend fun delete(limit: BudgetLimit)

    @Query("DELETE FROM budget_limits WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM budget_limits WHERE isActive = 1")
    fun getActive(): Flow<List<BudgetLimit>>

    @Query("SELECT * FROM budget_limits WHERE isActive = 1")
    fun getActiveWithCategories(): Flow<List<BudgetLimitWithCategory>>

    @Query("SELECT * FROM budget_limits WHERE categoryId = :categoryId AND isActive = 1")
    fun getByCategory(categoryId: Long): Flow<List<BudgetLimit>>

    @Query("SELECT * FROM budget_limits WHERE id = :id")
    suspend fun getById(id: Long): BudgetLimit?
}

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: AppSettings)

    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun get(key: String): AppSettings?

    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun delete(key: String)

    @Query("SELECT * FROM app_settings")
    fun getAll(): Flow<List<AppSettings>>
}

// ==================== NEW DAOS FOR SHARING ====================

@Dao
interface ConnectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: Connection): Long

    @Update
    suspend fun update(connection: Connection): Int

    @Query("SELECT * FROM connections WHERE isActive = 1 LIMIT 1")
    fun getActive(): Flow<Connection?>

    @Query("SELECT * FROM connections WHERE inviteCode = :code")
    suspend fun getByInviteCode(code: String): Connection?

    @Query("DELETE FROM connections WHERE id = :id")
    suspend fun deleteById(id: String): Int
}

@Dao
interface RecurringTransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rt: RecurringTransaction): Long

    @Update
    suspend fun update(rt: RecurringTransaction): Int

    @Delete
    suspend fun delete(rt: RecurringTransaction)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDueDate")
    fun getActive(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getByOwner(ownerId: String): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND nextDueDate <= :now ORDER BY nextDueDate")
    fun getDue(now: Long): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransaction?
}

@Dao
interface ShoppingItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ShoppingItem): Long

    @Update
    suspend fun update(item: ShoppingItem): Int

    @Delete
    suspend fun delete(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM shopping_items ORDER BY isChecked ASC, createdAt DESC")
    fun getAll(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE ownerId = :ownerId ORDER BY isChecked ASC, createdAt DESC")
    fun getByOwner(ownerId: String): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getById(id: Long): ShoppingItem?
}
