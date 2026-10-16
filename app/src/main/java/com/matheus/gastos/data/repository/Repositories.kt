package com.matheus.gastos.data.repository

import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.dao.SubcategoryDao
import com.matheus.gastos.data.dao.CreditCardDao
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.dao.BudgetLimitDao
import com.matheus.gastos.data.dao.AppSettingsDao
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.AppSettings
import com.matheus.gastos.data.entity.BudgetPeriod
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

class CategoryRepository(private val dao: CategoryDao) {
    suspend fun insert(category: Category) = dao.insert(category)
    suspend fun update(category: Category) = dao.update(category)
    suspend fun delete(category: Category) = dao.delete(category)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun getAll(): Flow<List<Category>> = dao.getAll()
    fun getDefaults(): Flow<List<Category>> = dao.getDefaults()
    fun getCustom(): Flow<List<Category>> = dao.getCustom()
    suspend fun getById(id: Long) = dao.getById(id)
}

class SubcategoryRepository(private val dao: SubcategoryDao) {
    suspend fun insert(subcategory: Subcategory) = dao.insert(subcategory)
    suspend fun update(subcategory: Subcategory) = dao.update(subcategory)
    suspend fun delete(subcategory: Subcategory) = dao.delete(subcategory)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun deleteByCategoryId(categoryId: Long) = dao.deleteByCategoryId(categoryId)
    fun getByCategoryId(categoryId: Long): Flow<List<Subcategory>> = dao.getByCategoryId(categoryId)
    suspend fun getById(id: Long) = dao.getById(id)
}

class CreditCardRepository(private val dao: CreditCardDao) {
    suspend fun insert(card: CreditCard) = dao.insert(card)
    suspend fun update(card: CreditCard) = dao.update(card)
    suspend fun delete(card: CreditCard) = dao.delete(card)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun getActive(): Flow<List<CreditCard>> = dao.getActive()
    fun getAll(): Flow<List<CreditCard>> = dao.getAll()
    suspend fun getById(id: Long) = dao.getById(id)
}

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun insert(transaction: Transaction) = dao.insert(transaction)
    suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions)
    suspend fun update(transaction: Transaction) = dao.update(transaction)
    suspend fun delete(transaction: Transaction) = dao.delete(transaction)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun deleteByInstallmentGroup(groupId: String) = dao.deleteByInstallmentGroup(groupId)
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByDateRange(startDate, endDate)
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByCategoryAndDateRange(categoryId, startDate, endDate)
    fun getByCardAndDateRange(cardId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByCardAndDateRange(cardId, startDate, endDate)
    fun getInstallmentsByGroup(groupId: String): Flow<List<Transaction>> = dao.getInstallmentsByGroup(groupId)
    fun getFutureInstallments(currentDate: Long): Flow<List<Transaction>> = dao.getFutureInstallments(currentDate)
    fun getTotalExpenses(startDate: Long, endDate: Long): Flow<Double?> = dao.getTotalExpenses(startDate, endDate)
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double?> = dao.getTotalIncome(startDate, endDate)
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<TransactionDao.CategoryExpense>> = dao.getExpensesByCategory(startDate, endDate)
    fun getExpensesByCard(startDate: Long, endDate: Long): Flow<List<TransactionDao.CardExpense>> = dao.getExpensesByCard(startDate, endDate)
    suspend fun getById(id: Long) = dao.getById(id)

    companion object {
        fun getMonthBounds(date: Date): Pair<Long, Long> {
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(java.util.Calendar.MONTH, 1)
            cal.add(java.util.Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis
            return start to end
        }
    }

    suspend fun createInstallments(
        baseTransaction: Transaction,
        totalInstallments: Int,
        categoryId: Long?,
        subcategoryId: Long?,
        creditCardId: Long?
    ) {
        val groupId = UUID.randomUUID().toString()
        val installments = mutableListOf<Transaction>()

        for (i in 1..totalInstallments) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = baseTransaction.date
            cal.add(java.util.Calendar.MONTH, i - 1)

            val installment = baseTransaction.copy(
                id = 0,
                installmentNumber = i,
                totalInstallments = totalInstallments,
                installmentGroupId = groupId,
                isInstallment = totalInstallments > 1,
                date = cal.timeInMillis,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            installments.add(installment)
        }
        dao.insertAll(installments)
    }
}

class BudgetLimitRepository(private val dao: BudgetLimitDao) {
    suspend fun insert(limit: BudgetLimit) = dao.insert(limit)
    suspend fun update(limit: BudgetLimit) = dao.update(limit)
    suspend fun delete(limit: BudgetLimit) = dao.delete(limit)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun getActive(): Flow<List<BudgetLimit>> = dao.getActive()
    fun getByCategory(categoryId: Long): Flow<List<BudgetLimit>> = dao.getByCategory(categoryId)
    suspend fun getById(id: Long) = dao.getById(id)

    data class BudgetLimitWithCategory(
        val budgetLimit: BudgetLimit,
        val categoryName: String?,
        val categoryColor: Int
    )

    fun getActiveWithCategories(): Flow<List<BudgetLimitWithCategory>> {
        return dao.getActive().map { limits ->
            limits.map { limit ->
                BudgetLimitWithCategory(limit, null, 0)
            }
        }
    }
}

class SettingsRepository(private val dao: AppSettingsDao) {
    suspend fun putString(key: String, value: String) = dao.insert(AppSettings(key, value))
    suspend fun putInt(key: String, value: Int) = dao.insert(AppSettings(key, value.toString()))
    suspend fun putLong(key: String, value: Long) = dao.insert(AppSettings(key, value.toString()))
    suspend fun putBoolean(key: String, value: Boolean) = dao.insert(AppSettings(key, value.toString()))

    suspend fun getString(key: String, default: String): String = dao.getValue(key) ?: default
    suspend fun getInt(key: String, default: Int): Int = dao.getValue(key)?.toIntOrNull() ?: default
    suspend fun getLong(key: String, default: Long): Long = dao.getValue(key)?.toLongOrNull() ?: default
    suspend fun getBoolean(key: String, default: Boolean): Boolean = dao.getValue(key)?.toBoolean() ?: default
}
