package com.matheus.gastos.data.dao

import androidx.room.*
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.AppSettings
import com.matheus.gastos.data.entity.BudgetPeriod
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

    @Query("SELECT * FROM categories WHERE isExpenseDefault = :isExpense ORDER BY sortOrder, name")
    fun getByType(isExpense: Boolean): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
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

    @RawQuery(observedEntities = [Transaction::class])
    fun getAllTransactions(query: SimpleSQLiteQuery): Flow<List<Transaction>>

    data class CategoryExpense(
        val categoryId: Long,
        val total: Double
    )

    data class CardExpense(
        val creditCardId: Long,
        val total: Double
    )
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
