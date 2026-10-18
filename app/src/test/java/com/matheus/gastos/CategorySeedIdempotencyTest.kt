package com.matheus.gastos

import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.repository.CategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Testes da idempotência de [CategoryRepository.seedDefaultsIfEmpty].
 *
 * O seed lê o banco (getAllOnce) e insere apenas os pares
 * (nome normalizado + isExpenseDefault) que faltam.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategorySeedIdempotencyTest {

    private val dao: CategoryDao = mock()
    private val repository = CategoryRepository(dao)

    private fun allDefaults(): List<Category> = CategoryRepository.defaultCategories()

    private suspend fun captureInserted(): List<Category> {
        val captor = argumentCaptor<List<Category>>()
        verify(dao).insertAll(captor.capture())
        return captor.firstValue
    }

    @Test
    fun `empty database inserts all 15 defaults in a single call`() = runTest {
        whenever(dao.getAllOnce()).thenReturn(emptyList())

        repository.seedDefaultsIfEmpty()

        val inserted = captureInserted()
        assertEquals(15, inserted.size)
        assertEquals(9, inserted.count { it.isExpenseDefault })
        assertEquals(6, inserted.count { !it.isExpenseDefault })
        assertTrue(inserted.all { it.isDefault })
        verify(dao, org.mockito.kotlin.times(1)).insertAll(any<List<Category>>())
    }

    @Test
    fun `database with all defaults does not insert anything`() = runTest {
        whenever(dao.getAllOnce()).thenReturn(allDefaults())

        repository.seedDefaultsIfEmpty()

        // Comportamento real: lista vazia -> insertAll não é chamado.
        verify(dao, never()).insertAll(any<List<Category>>())
    }

    @Test
    fun `partial database inserts exactly the 13 missing categories`() = runTest {
        val existing = listOf(
            Category(id = 1, name = "Alimentação", isDefault = true, isExpenseDefault = true),
            Category(id = 2, name = "Salário", isDefault = true, isExpenseDefault = false)
        )
        whenever(dao.getAllOnce()).thenReturn(existing)

        repository.seedDefaultsIfEmpty()

        val inserted = captureInserted()
        assertEquals(13, inserted.size)
        val names = inserted.map { it.name to it.isExpenseDefault }.toSet()
        assertTrue("Alimentação" to true !in names)
        assertTrue("Salário" to false !in names)
        assertEquals(8, inserted.count { it.isExpenseDefault })
        assertEquals(5, inserted.count { !it.isExpenseDefault })
    }

    @Test
    fun `comparison is case insensitive and trims surrounding spaces`() = runTest {
        val existing = listOf(
            Category(id = 1, name = "  alimentação  ", isExpenseDefault = true),
            Category(id = 2, name = "SALÁRIO", isExpenseDefault = false),
            Category(id = 3, name = "  Pix  ", isExpenseDefault = true) // não é padrão: irrelevante
        )
        whenever(dao.getAllOnce()).thenReturn(existing)

        repository.seedDefaultsIfEmpty()

        val inserted = captureInserted()
        assertEquals(13, inserted.size)
        assertTrue(inserted.none { it.name == "Alimentação" && it.isExpenseDefault })
        assertTrue(inserted.none { it.name == "Salário" && !it.isExpenseDefault })
        // A categoria custom "Pix" não impede o seed das demais.
        assertTrue(inserted.any { it.name == "Transporte" })
    }

    @Test
    fun `same name with different type is treated as missing`() = runTest {
        // "Outros" existe como gasto, mas falta como entrada -> deve inserir só a de entrada.
        val existing = listOf(
            Category(id = 1, name = "Outros", isExpenseDefault = true)
        )
        whenever(dao.getAllOnce()).thenReturn(existing)

        repository.seedDefaultsIfEmpty()

        val inserted = captureInserted()
        assertEquals(14, inserted.size)
        assertEquals(1, inserted.count { it.name == "Outros" })
        assertTrue(inserted.first { it.name == "Outros" }.isExpenseDefault == false)
    }

    @Test
    fun `preserves existing categories untouched`() = runTest {
        val existing = listOf(
            Category(id = 42, name = "Alimentação", icon = "custom", isDefault = false, isExpenseDefault = true)
        )
        whenever(dao.getAllOnce()).thenReturn(existing)

        repository.seedDefaultsIfEmpty()

        verify(dao, never()).update(any())
        verify(dao, never()).delete(any())
        assertEquals(14, captureInserted().size)
    }
}
