package com.matheus.gastos.data.repository

import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.dao.SubcategoryDao
import com.matheus.gastos.data.dao.CreditCardDao
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.dao.CategoryExpense
import com.matheus.gastos.data.dao.CardExpense
import com.matheus.gastos.data.dao.BudgetLimitDao
import com.matheus.gastos.data.dao.AppSettingsDao
import com.matheus.gastos.data.dao.ConnectionDao
import com.matheus.gastos.data.dao.RecurringTransactionDao
import com.matheus.gastos.data.dao.ShoppingItemDao
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.*

class CategoryRepository(private val dao: CategoryDao, private val subDao: SubcategoryDao) {
    suspend fun insert(category: Category): Long = dao.insert(category)
    suspend fun update(category: Category) = dao.update(category)
    suspend fun delete(category: Category) {
        subDao.deleteByCategoryId(category.id)
        dao.delete(category)
    }
    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
        subDao.deleteByCategoryId(id)
    }
    fun getAll(): Flow<List<Category>> = dao.getAll()
    fun getDefaults(): Flow<List<Category>> = dao.getDefaults()
    fun getCustom(): Flow<List<Category>> = dao.getCustom()
    suspend fun getById(id: Long): Category? = dao.getById(id)

    // Subcategories
    suspend fun insertSubcategory(sub: Subcategory): Long = subDao.insert(sub)
    suspend fun updateSubcategory(sub: Subcategory) = subDao.update(sub)
    suspend fun deleteSubcategory(sub: Subcategory) = subDao.delete(sub)
    suspend fun deleteSubcategoryById(id: Long) = subDao.deleteById(id)
    suspend fun deleteByCategoryId(categoryId: Long) = subDao.deleteByCategoryId(categoryId)
    fun getSubcategories(categoryId: Long): Flow<List<Subcategory>> = subDao.getByCategoryId(categoryId)
    suspend fun getSubcategoryById(id: Long): Subcategory? = subDao.getById(id)
}

class CreditCardRepository(private val dao: CreditCardDao) {
    suspend fun insert(card: CreditCard): Long = dao.insert(card)
    suspend fun update(card: CreditCard) = dao.update(card)
    suspend fun delete(card: CreditCard) = dao.delete(card)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun getActive(): Flow<List<CreditCard>> = dao.getActive()
    fun getAll(): Flow<List<CreditCard>> = dao.getAll()
    suspend fun getById(id: Long): CreditCard? = dao.getById(id)
}

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun insert(transaction: Transaction): Long = dao.insert(transaction)
    suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions)
    suspend fun update(transaction: Transaction) = dao.update(transaction)
    suspend fun delete(transaction: Transaction) = dao.delete(transaction)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun deleteByInstallmentGroup(groupId: String) = dao.deleteByInstallmentGroup(groupId)
    
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByDateRange(startDate, endDate)
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByCategoryAndDateRange(categoryId, startDate, endDate)
    fun getByCardAndDateRange(cardId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByCardAndDateRange(cardId, startDate, endDate)
    fun getFutureInstallments(currentDate: Long): Flow<List<Transaction>> = dao.getFutureInstallments(currentDate)
    fun getInstallmentsByGroup(groupId: String): Flow<List<Transaction>> = dao.getInstallmentsByGroup(groupId)
    fun getTotalExpenses(startDate: Long, endDate: Long): Flow<Double> = dao.getTotalExpenses(startDate, endDate).map { it ?: 0.0 }
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double> = dao.getTotalIncome(startDate, endDate).map { it ?: 0.0 }
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategoryExpense>> = dao.getExpensesByCategory(startDate, endDate)
    fun getExpensesByCard(startDate: Long, endDate: Long): Flow<List<CardExpense>> = dao.getExpensesByCard(startDate, endDate)
    suspend fun getById(id: Long): Transaction? = dao.getById(id)
    fun getByOwnerId(ownerId: String): Flow<List<Transaction>> = dao.getByOwnerId(ownerId)
    fun getUnsynced(): Flow<List<Transaction>> = dao.getUnsynced()

    // Helper to get month boundaries
    companion object {
        fun getMonthBounds(date: Date = Date()): Pair<Long, Long> {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis
            return start to end
        }

        fun getWeekBounds(date: Date = Date()): Pair<Long, Long> {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.WEEK_OF_YEAR, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis
            return start to end
        }
    }
}

class BudgetLimitRepository(private val dao: BudgetLimitDao, private val categoryRepo: CategoryRepository) {
    suspend fun insert(limit: BudgetLimit): Long = dao.insert(limit)
    suspend fun update(limit: BudgetLimit) = dao.update(limit)
    suspend fun delete(limit: BudgetLimit) = dao.delete(limit)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun getActive(): Flow<List<BudgetLimit>> = dao.getActive()
    fun getByCategory(categoryId: Long): Flow<List<BudgetLimit>> = dao.getByCategory(categoryId)
    suspend fun getById(id: Long): BudgetLimit? = dao.getById(id)

    // Get active limits with category info
    fun getActiveWithCategories(): Flow<List<BudgetLimitWithCategory>> = dao.getActive().map { limits ->
        limits.map { limit ->
            BudgetLimitWithCategory(limit, categoryRepo.getById(limit.categoryId).await())
        }
    }

    data class BudgetLimitWithCategory(
        val limit: BudgetLimit,
        val category: Category?
    )
}

class SettingsRepository(private val dao: AppSettingsDao) {
    suspend fun putString(key: String, value: String) = dao.insert(AppSettings(key, value))
    suspend fun getString(key: String, default: String = ""): String = dao.getValue(key).await() ?: default
    suspend fun putBoolean(key: String, value: Boolean) = dao.insert(AppSettings(key, value.toString()))
    suspend fun getBoolean(key: String, default: Boolean = false): Boolean = dao.getValue(key).await()?.toBoolean() ?: default
    suspend fun putLong(key: String, value: Long) = dao.insert(AppSettings(key, value.toString()))
    suspend fun getLong(key: String, default: Long = 0L): Long = dao.getValue(key).await()?.toLongOrNull() ?: default
    suspend fun putInt(key: String, value: Int) = dao.insert(AppSettings(key, value.toString()))
    suspend fun getInt(key: String, default: Int = 0): Int = dao.getValue(key).await()?.toIntOrNull() ?: default
    suspend fun delete(key: String) = dao.delete(key)
}

// ==================== NEW REPOSITORIES FOR SHARING ====================

class ConnectionRepository(
    private val dao: ConnectionDao,
    private val settingsRepository: SettingsRepository
) {
    suspend fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Removed confusing chars
        var code: String
        do {
            code = (1..8).map { chars.random() }.joinToString("")
        } while (dao.getByInviteCode(code) != null)
        return code
    }

    suspend fun acceptInvite(code: String, myUserId: String, myUsername: String): Connection? {
        val existing = dao.getByInviteCode(code) ?: return null
        if (existing.partnerUserId == myUserId) return null // Can't connect to self

        val connection = Connection(
            id = java.util.UUID.randomUUID().toString(),
            myUserId = myUserId,
            partnerUserId = existing.myUserId,
            partnerUsername = existing.partnerUsername,
            inviteCode = code
        )
        dao.insert(connection)
        return connection
    }

    fun getActive(): Flow<Connection?> = dao.getActive()

    suspend fun disconnect() {
        val conn = dao.getActive().first() ?: return
        val updated = conn.copy(isActive = false)
        dao.update(updated)
    }
}

class RecurringTransactionRepository(
    private val dao: RecurringTransactionDao,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) {
    fun getActive() = dao.getActive()
    fun getByOwner(ownerId: String) = dao.getByOwner(ownerId)
    fun getDue(now: Long) = dao.getDue(now)

    suspend fun insert(rt: RecurringTransaction) = dao.insert(rt)
    suspend fun update(rt: RecurringTransaction) = dao.update(rt)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun toggleActive(id: Long) {
        val rt = dao.getById(id) ?: return
        val updated = rt.copy(isActive = !rt.isActive)
        dao.update(updated)
    }

    // Process due recurring transactions -> create real transactions
    suspend fun processDueRecurrences() {
        val now = System.currentTimeMillis()
        val due = dao.getDue(now).first()

        for (rt in due) {
            // Check if already generated for this due date
            val alreadyExists = transactionRepository.getByDateRange(
                rt.nextDueDate, rt.nextDueDate
            ).first().any { t ->
                t.description == rt.description &&
                t.amount == rt.amount &&
                t.isExpense == (rt.type == "expense") &&
                t.categoryId == rt.categoryId
            }

            if (!alreadyExists) {
                val transaction = Transaction(
                    amount = rt.amount,
                    description = rt.description,
                    date = rt.nextDueDate,
                    categoryId = rt.categoryId,
                    subcategoryId = rt.subcategoryId,
                    creditCardId = rt.creditCardId,
                    isExpense = rt.type == "expense",
                    ownerId = rt.ownerId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                transactionRepository.insert(transaction)
            }

            // Calculate next occurrence
            val next = calculateNextOccurrence(rt, rt.nextDueDate)
            if (next != null && (rt.endDate == null || next <= rt.endDate!!)) {
                val updated = rt.copy(
                    nextDueDate = next,
                    lastGeneratedAt = rt.nextDueDate
                )
                dao.update(updated)
            } else {
                val updated = rt.copy(isActive = false)
                dao.update(updated)
            }
        }
    }

    private fun calculateNextOccurrence(rt: RecurringTransaction, after: Long): Long? {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = after
        when (rt.frequency) {
            RecurringFrequency.MONTHLY -> {
                cal.add(java.util.Calendar.MONTH, 1)
                cal.set(java.util.Calendar.DAY_OF_MONTH, rt.dayOfMonth.coerceAtMost(cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)))
            }
            RecurringFrequency.WEEKLY -> cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.DAILY -> cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.YEARLY -> cal.add(java.util.Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }
}

class ShoppingRepository(
    private val dao: ShoppingItemDao,
    private val settingsRepository: SettingsRepository
) {
    fun getAll() = dao.getAll()
    fun getByOwner(ownerId: String) = dao.getByOwner(ownerId)

    suspend fun insert(item: ShoppingItem) = dao.insert(item)
    suspend fun update(item: ShoppingItem) = dao.update(item)
    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun toggleCheck(id: Long, userId: String) {
        val item = dao.getById(id) ?: return
        val updated = item.copy(
            isChecked = !item.isChecked,
            checkedAt = if (!item.isChecked) System.currentTimeMillis() else null,
            checkedBy = if (!item.isChecked) userId else null
        )
        dao.update(updated)
    }
}