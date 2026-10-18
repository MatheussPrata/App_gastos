package com.matheus.gastos

import com.matheus.gastos.data.dao.BudgetLimitDao
import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.BudgetPeriod
import com.matheus.gastos.data.repository.BudgetLimitRepository
import com.matheus.gastos.ui.screen.budget.budgetPeriodLabel
import com.matheus.gastos.ui.screen.budget.usagePercent
import com.matheus.gastos.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class BudgetLimitTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var dao: BudgetLimitDao

    @Mock
    lateinit var categoryDao: CategoryDao

    @Mock
    lateinit var transactionDao: TransactionDao

    private lateinit var repository: BudgetLimitRepository

    @Before
    fun setup() {
        repository = BudgetLimitRepository(dao, categoryDao, transactionDao)
    }

    // --- Entidade / defaults ---

    @Test
    fun `budget limit defaults are monthly active and zero spent`() {
        val limit = BudgetLimit(categoryId = 1L, limitAmount = 500.0)

        assertEquals(BudgetPeriod.MONTHLY, limit.period)
        assertTrue(limit.isActive)
        assertEquals(0.0, limit.spentAmount, 0.0)
        assertNull(limit.endDate)
    }

    @Test
    fun `usagePercent computes percentage`() {
        assertEquals(50, usagePercent(50.0, 100.0))
        assertEquals(100, usagePercent(100.0, 100.0))
        assertEquals(1, usagePercent(1.0, 300.0))
    }

    @Test
    fun `usagePercent clamps above 100`() {
        assertEquals(100, usagePercent(250.0, 100.0))
    }

    @Test
    fun `usagePercent handles zero or negative limit safely`() {
        assertEquals(0, usagePercent(50.0, 0.0))
        assertEquals(0, usagePercent(50.0, -10.0))
        assertEquals(0, usagePercent(0.0, 100.0))
    }

    @Test
    fun `budgetPeriodLabel returns portuguese labels`() {
        assertEquals("Mensal", budgetPeriodLabel(BudgetPeriod.MONTHLY))
        assertEquals("Semanal", budgetPeriodLabel(BudgetPeriod.WEEKLY))
        assertEquals("Anual", budgetPeriodLabel(BudgetPeriod.YEARLY))
        assertEquals("Diário", budgetPeriodLabel(BudgetPeriod.DAILY))
    }

    @Test
    fun `amount parser accepts comma or dot`() {
        assertEquals(10.5, TransactionViewModel.parseAmount("10,50")!!, 0.0)
        assertEquals(10.5, TransactionViewModel.parseAmount("10.50")!!, 0.0)
        assertNull(TransactionViewModel.parseAmount("0"))
        assertNull(TransactionViewModel.parseAmount("abc"))
        assertNull(TransactionViewModel.parseAmount(""))
    }

    // --- Repository com DAO mockado ---

    @Test
    fun `insert delegates to dao and returns id`() = runBlocking {
        @Suppress("UNUSED_EXPRESSION")
        val limit = BudgetLimit(categoryId = 2L, limitAmount = 300.0)
        whenever(dao.insert(limit)).thenReturn(7L)

        val id = repository.insert(limit)

        assertEquals(7L, id)
        verify(dao).insert(limit)
        Unit
    }

    @Test
    fun `update delegates to dao`() = runBlocking {
        val limit = BudgetLimit(id = 3L, categoryId = 2L, limitAmount = 400.0)
        whenever(dao.update(limit)).thenReturn(1)

        repository.update(limit)

        verify(dao).update(limit)
        Unit
    }

    @Test
    fun `delete and deleteById delegate to dao`() = runBlocking {
        val limit = BudgetLimit(id = 4L, categoryId = 2L, limitAmount = 100.0)

        repository.delete(limit)
        repository.deleteById(4L)

        verify(dao).delete(limit)
        verify(dao).deleteById(4L)
        Unit
    }

    @Test
    fun `getActive forwards the dao flow`() = runBlocking {
        val limits = listOf(
            BudgetLimit(id = 1L, categoryId = 1L, limitAmount = 100.0),
            BudgetLimit(id = 2L, categoryId = 2L, limitAmount = 200.0)
        )
        whenever(dao.getActive()).thenReturn(flowOf(limits))

        val result = repository.getActive().first()

        assertEquals(2, result.size)
        assertEquals(limits, result)
    }

    @Test
    fun `getActiveWithCategories keeps active limits`() = runBlocking {
        val limits = listOf(BudgetLimit(id = 1L, categoryId = 1L, limitAmount = 100.0))
        whenever(dao.getActive()).thenReturn(flowOf(limits))
        whenever(categoryDao.getAllOnce()).thenReturn(emptyList())
        whenever(transactionDao.getExpensesByCategory(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(flowOf(emptyList()))

        val result = repository.getActiveWithCategories().first()

        assertEquals(1, result.size)
        assertEquals(1L, result.first().budgetLimit.id)
    }
}
