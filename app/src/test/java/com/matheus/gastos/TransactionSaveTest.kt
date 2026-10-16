package com.matheus.gastos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.PaymentMethod
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.repository.TransactionRepository
import com.matheus.gastos.viewmodel.TransactionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * Testes de persistência do TransactionViewModel com DAO mockado.
 * Objetivo principal: garantir que insert/update realmente chamam o DAO
 * (o bug original era o botão Salvar não persistir nada).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionSaveTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dao: TransactionDao = mock()
    private val repository = TransactionRepository(dao)
    private val viewModel = TransactionViewModel(repository)

    @Test
    fun `insert persists transaction with correct fields`() = runTest {
        val transaction = Transaction(
            description = "Café",
            amount = 12.5,
            date = 1_700_000_000_000L,
            isExpense = true,
            categoryId = 3L,
            paymentMethod = PaymentMethod.PIX
        )

        viewModel.insert(transaction)

        val captor = argumentCaptor<Transaction>()
        verify(dao).insert(captor.capture())
        val saved = captor.firstValue
        assertEquals("Café", saved.description)
        assertEquals(12.5, saved.amount, 0.001)
        assertEquals(PaymentMethod.PIX, saved.paymentMethod)
        assertEquals(3L, saved.categoryId)
    }

    @Test
    fun `update forwards the transaction to the dao`() = runTest {
        val transaction = Transaction(
            id = 7L,
            description = "Aluguel",
            amount = 1500.0,
            date = 1_700_000_000_000L,
            isExpense = true
        )

        viewModel.update(transaction)

        verify(dao).update(transaction)
    }

    @Test
    fun `createInstallments inserts the right number of rows`() = runTest {
        val base = Transaction(
            description = "Notebook",
            amount = 1000.0,
            date = 1_700_000_000_000L,
            isExpense = true,
            paymentMethod = PaymentMethod.CARTAO
        )

        viewModel.createInstallments(base, totalInstallments = 4, categoryId = 1L, subcategoryId = null, creditCardId = 2L)

        val captor = argumentCaptor<List<Transaction>>()
        verify(dao).insertAll(captor.capture())
        val rows = captor.firstValue
        assertEquals(4, rows.size)
        assertEquals(1, rows.first().installmentNumber)
        assertEquals(4, rows.last().installmentNumber)
        assertEquals(rows.first().installmentGroupId, rows.last().installmentGroupId)
    }

    @Test
    fun `plain insert is not treated as installments`() = runTest {
        val transaction = Transaction(
            description = "Mercado",
            amount = 80.0,
            date = 1_700_000_000_000L,
            isExpense = true,
            isInstallment = false
        )

        viewModel.insert(transaction)

        verify(dao, never()).insertAll(any())
    }
}
