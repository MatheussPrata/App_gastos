package com.matheus.gastos

import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Calendar

/**
 * Parcelamento real: a compra de R$ 1000 em 10x deve gerar 10 lancamentos
 * de R$ 100, um por mes, e NAO um unico lancamento de R$ 1000.
 */
class InstallmentSplitTest {

    @Test
    fun `split divides total evenly`() {
        assertEquals(listOf(100.0, 100.0, 100.0), TransactionRepository.splitInstallmentAmounts(300.0, 3))
        assertEquals(10, TransactionRepository.splitInstallmentAmounts(1000.0, 10).size)
        assertTrue(TransactionRepository.splitInstallmentAmounts(1000.0, 10).all { it == 100.0 })
    }

    @Test
    fun `split keeps cents and puts remainder on last installment`() {
        val parts = TransactionRepository.splitInstallmentAmounts(100.0, 3)

        assertEquals(3, parts.size)
        assertEquals(33.33, parts[0], 0.0001)
        assertEquals(33.33, parts[1], 0.0001)
        assertEquals(33.34, parts[2], 0.0001)
        // Soma exata, sem centavo perdido.
        assertEquals(100.0, parts.sum(), 0.0001)
    }

    @Test
    fun `split sum always equals total`() {
        listOf(0.01, 0.07, 10.0, 99.99, 1234.56, 1000.0).forEach { total ->
            listOf(1, 2, 3, 7, 10, 12).forEach { count ->
                val parts = TransactionRepository.splitInstallmentAmounts(total, count)
                assertEquals("total=$total count=$count", total, parts.sum(), 0.0001)
            }
        }
    }

    @Test
    fun `split rejects non positive installment counts`() {
        assertTrue(TransactionRepository.splitInstallmentAmounts(100.0, 0).isEmpty())
        assertTrue(TransactionRepository.splitInstallmentAmounts(100.0, -1).isEmpty())
    }

    @Test
    fun `createInstallments persists one row per month with divided amounts`() = runTest {
        val dao: TransactionDao = mock()
        whenever(dao.insertAll(any<List<Transaction>>())).thenReturn(Unit)

        val base = Transaction(
            id = 0,
            description = "Notebook",
            amount = 1000.0,
            isExpense = true,
            isInstallment = true,
            totalInstallments = 10,
            date = 1_700_000_000_000L // 2023-11-14
        )

        val repo = TransactionRepository(dao)
        repo.createInstallments(base, 10, categoryId = null, subcategoryId = null, creditCardId = null)

        val captor = argumentCaptor<List<Transaction>>()
        verify(dao).insertAll(captor.capture())
        val rows = captor.firstValue

        assertEquals(10, rows.size)
        assertTrue("cada parcela deve valer 100", rows.all { it.amount == 100.0 })
        assertEquals((1..10).toList(), rows.map { it.installmentNumber })
        // Grupo unico para todas as parcelas.
        assertEquals(1, rows.map { it.installmentGroupId }.distinct().size)

        // Cada parcela em um mes consecutivo diferente.
        val months = rows.map { monthKeyOf(it.date) }
        assertEquals(months.distinct().size, months.size)

        // Ordem cronologica: mes i+1 avanca exatamente 1 mes do anterior.
        rows.zipWithNext().forEach { (a, b) ->
            val cal = Calendar.getInstance().apply { timeInMillis = a.date }
            val mesEsperado = Calendar.getInstance().apply {
                timeInMillis = a.date
                add(Calendar.MONTH, 1)
            }
            cal.timeInMillis = b.date
            assertEquals(mesEsperado.get(Calendar.MONTH), cal.get(Calendar.MONTH))
            assertEquals(mesEsperado.get(Calendar.YEAR), cal.get(Calendar.YEAR))
        }
    }

    @Test
    fun `installments appear in the month balance they belong to`() = runTest {
        val dao: TransactionDao = mock()
        whenever(dao.insertAll(any<List<Transaction>>())).thenReturn(Unit)
        val repo = TransactionRepository(dao)

        val base = Transaction(
            description = "TV",
            amount = 600.0,
            isExpense = true,
            isInstallment = true,
            totalInstallments = 6,
            date = System.currentTimeMillis()
        )
        repo.createInstallments(base, 6, null, null, null)

        val captor = argumentCaptor<List<Transaction>>()
        verify(dao).insertAll(captor.capture())
        val rows = captor.firstValue

        // Nenhuma parcela carrega o valor cheio: o saldo mensal recebe so a parcela.
        assertTrue(rows.none { it.amount == 600.0 })
        assertTrue(rows.all { it.amount == 100.0 })
        assertEquals(600.0, rows.sumOf { it.amount }, 0.0001)
    }

    private fun monthKeyOf(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
    }
}
