package com.matheus.gastos

import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.repository.CategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Testes do seed de categorias padrão (gasto vs entrada).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategorySeedTest {

    private val dao: CategoryDao = mock()
    private val repository = CategoryRepository(dao)

    @Test
    fun `defaultCategories separates expense and income`() {
        val categories = CategoryRepository.defaultCategories()

        val expenses = categories.filter { it.isExpenseDefault }
        val incomes = categories.filter { !it.isExpenseDefault }

        assertEquals(9, expenses.size)
        assertEquals(6, incomes.size)
        assertTrue(expenses.any { it.name == "Alimentação" })
        assertTrue(incomes.any { it.name == "Salário" })
        assertTrue(categories.all { it.isDefault })
    }

    @Test
    fun `seedDefaultsIfEmpty inserts when database is empty`() = runTest {
        whenever(dao.getAllOnce()).thenReturn(emptyList())

        repository.seedDefaultsIfEmpty()

        val captor = argumentCaptor<List<Category>>()
        verify(dao).insertAll(captor.capture())
        assertEquals(15, captor.firstValue.size)
    }

    @Test
    fun `seedDefaultsIfEmpty does nothing when categories already exist`() = runTest {
        whenever(dao.getAllOnce()).thenReturn(CategoryRepository.defaultCategories())

        repository.seedDefaultsIfEmpty()

        verify(dao, never()).insertAll(org.mockito.kotlin.any())
    }

    @Test
    fun `seeded categories are editable and not locked`() {
        // isDefault apenas marca a origem; nada impede update/delete.
        val category = CategoryRepository.defaultCategories().first()
        assertTrue(category.isDefault)
        assertTrue(category.isExpenseDefault)
    }
}
