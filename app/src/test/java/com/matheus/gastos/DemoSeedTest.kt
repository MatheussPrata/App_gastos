package com.matheus.gastos

import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.PaymentMethod
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.repository.CategoryRepository
import com.matheus.gastos.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Testes do seed de lançamentos de demonstração
 * ([TransactionRepository.seedDemoIfEmpty]): só roda com banco vazio e nunca duplica.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DemoSeedTest {

    private val transactionDao: TransactionDao = mock()
    private val categoryDao: CategoryDao = mock()
    private val repository = TransactionRepository(transactionDao)

    /** Categorias padrão com ids sintéticos, como se já estivessem no banco. */
    private fun seededCategories(): List<Category> =
        CategoryRepository.defaultCategories().mapIndexed { index, category ->
            category.copy(id = (index + 1).toLong())
        }

    private fun categoryId(categories: List<Category>, name: String, isExpense: Boolean): Long =
        categories.first { it.name == name && it.isExpenseDefault == isExpense }.id

    @Test
    fun `nao insere nada quando ja existe transacao`() = runTest {
        whenever(transactionDao.countAll()).thenReturn(1)

        repository.seedDemoIfEmpty(seededCategories())

        verify(transactionDao, never()).insert(any())
        verify(transactionDao, never()).insertAll(any())
    }

    @Test
    fun `banco vazio insere salario casa e 6 parcelas do drone`() = runTest {
        whenever(transactionDao.countAll()).thenReturn(0)

        repository.seedDemoIfEmpty(seededCategories())

        val single = argumentCaptor<Transaction>()
        verify(transactionDao, times(2)).insert(single.capture())
        val inserted = single.allValues

        val salary = inserted.first { it.description == "Salário" }
        assertEquals(2300.0, salary.amount, 0.001)
        assertFalse(salary.isExpense)
        assertEquals(PaymentMethod.DINHEIRO, salary.paymentMethod)
        assertFalse(salary.isInstallment)
        assertEquals(1, salary.installmentNumber)
        assertEquals(1, salary.totalInstallments)

        val home = inserted.first { it.description == "Casa" }
        assertEquals(900.0, home.amount, 0.001)
        assertTrue(home.isExpense)
        assertEquals(PaymentMethod.DINHEIRO, home.paymentMethod)
        assertFalse(home.isInstallment)

        val installments = argumentCaptor<List<Transaction>>()
        verify(transactionDao).insertAll(installments.capture())
        val drone = installments.firstValue

        assertEquals(6, drone.size)
        assertTrue(drone.all { it.description == "Drone" })
        assertTrue(drone.all { it.amount == 100.0 })
        assertTrue(drone.all { it.isExpense })
        assertTrue(drone.all { it.isInstallment })
        assertEquals((1..6).toList(), drone.map { it.installmentNumber })
        assertTrue(drone.all { it.totalInstallments == 6 })
        assertEquals(600.0, drone.sumOf { it.amount }, 0.001)
        // Um único grupo de parcelamento
        assertEquals(1, drone.mapNotNull { it.installmentGroupId }.toSet().size)
    }

    @Test
    fun `categorias corretas sao usadas`() = runTest {
        whenever(transactionDao.countAll()).thenReturn(0)
        val categories = seededCategories()

        repository.seedDemoIfEmpty(categories)

        val single = argumentCaptor<Transaction>()
        verify(transactionDao, times(2)).insert(single.capture())

        val salary = single.allValues.first { it.description == "Salário" }
        val home = single.allValues.first { it.description == "Casa" }
        assertEquals(categoryId(categories, "Salário", false), salary.categoryId)
        assertEquals(categoryId(categories, "Moradia", true), home.categoryId)

        val installments = argumentCaptor<List<Transaction>>()
        verify(transactionDao).insertAll(installments.capture())
        val leisureId = categoryId(categories, "Lazer", true)
        assertTrue(installments.firstValue.all { it.categoryId == leisureId })
    }

    @Test
    fun `todas as transacoes usam a data de hoje`() = runTest {
        whenever(transactionDao.countAll()).thenReturn(0)
        val before = System.currentTimeMillis()

        repository.seedDemoIfEmpty(seededCategories())

        val after = System.currentTimeMillis()
        val single = argumentCaptor<Transaction>()
        verify(transactionDao, times(2)).insert(single.capture())
        assertTrue(single.allValues.all { it.date in before..after })

        val installments = argumentCaptor<List<Transaction>>()
        verify(transactionDao).insertAll(installments.capture())
        assertTrue(installments.firstValue.all { it.date >= before })
    }

    @Test
    fun `idempotente ao chamar duas vezes`() = runTest {
        whenever(transactionDao.countAll()).thenReturn(0, 3)

        repository.seedDemoIfEmpty(seededCategories())
        repository.seedDemoIfEmpty(seededCategories())

        verify(transactionDao, times(2)).insert(any())
        verify(transactionDao, times(1)).insertAll(any())
    }
}
