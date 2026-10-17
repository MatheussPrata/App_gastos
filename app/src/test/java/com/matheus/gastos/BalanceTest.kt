package com.matheus.gastos

import com.matheus.gastos.data.dao.MonthlyBalance
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Testes do cálculo de saldo: acumulado por transação (dentro do mês)
 * e agregação mensal usada no histórico de relatórios.
 */
class BalanceTest {

    private fun tx(
        id: Long,
        amount: Double,
        isExpense: Boolean,
        date: Long
    ) = Transaction(
        id = id,
        amount = amount,
        description = "tx$id",
        isExpense = isExpense,
        date = date
    )

    @Test
    fun runningBalance_sumsIncomeAndSubtractsExpenses_inDateOrder() = runTest {
        val dao: TransactionDao = mock()
        // DAO devolve em ordem DESC (como na tela)
        whenever(dao.getByDateRange(0L, 1_000L)).thenReturn(
            flowOf(
                listOf(
                    tx(3, 30.0, true, 300L),
                    tx(2, 50.0, false, 200L),
                    tx(1, 100.0, false, 100L)
                )
            )
        )

        val repo = TransactionRepository(dao)
        val result = repo.getTransactionsWithRunningBalance(0L, 1_000L).first()

        // +100 = 100, +50 = 150, -30 = 120
        assertEquals(3, result.size)
        // ordem final continua DESC (mais recente primeiro)
        assertEquals(3L, result[0].first.id)
        assertEquals(120.0, result[0].second, 0.001)
        assertEquals(2L, result[1].first.id)
        assertEquals(150.0, result[1].second, 0.001)
        assertEquals(1L, result[2].first.id)
        assertEquals(100.0, result[2].second, 0.001)
    }

    @Test
    fun runningBalance_canGoNegative() = runTest {
        val dao: TransactionDao = mock()
        whenever(dao.getByDateRange(0L, 1_000L)).thenReturn(
            flowOf(listOf(tx(1, 80.0, true, 100L)))
        )

        val repo = TransactionRepository(dao)
        val result = repo.getTransactionsWithRunningBalance(0L, 1_000L).first()

        assertEquals(1, result.size)
        assertEquals(-80.0, result[0].second, 0.001)
    }

    @Test
    fun runningBalance_isEmptyWhenNoTransactions() = runTest {
        val dao: TransactionDao = mock()
        whenever(dao.getByDateRange(0L, 1_000L)).thenReturn(flowOf(emptyList()))

        val repo = TransactionRepository(dao)
        val result = repo.getTransactionsWithRunningBalance(0L, 1_000L).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun monthlyBalance_balanceIsIncomeMinusExpenses() {
        val mb = MonthlyBalance(month = "2026-10", expenses = 300.0, income = 1000.0)

        assertEquals(700.0, mb.balance, 0.001)
    }

    @Test
    fun monthlyBalance_canBeNegative() {
        val mb = MonthlyBalance(month = "2026-10", expenses = 1200.0, income = 900.0)

        assertEquals(-300.0, mb.balance, 0.001)
    }

    @Test
    fun monthlyBalances_areExposedFromRepository() = runTest {
        val dao: TransactionDao = mock()
        val balances = listOf(
            MonthlyBalance("2026-10", 300.0, 1000.0),
            MonthlyBalance("2026-09", 500.0, 400.0)
        )
        whenever(dao.getMonthlyBalances()).thenReturn(flowOf(balances))

        val repo = TransactionRepository(dao)
        val result = repo.getMonthlyBalances().first()

        assertEquals(2, result.size)
        assertEquals("2026-10", result[0].month)
        assertEquals(700.0, result[0].balance, 0.001)
        assertEquals(-100.0, result[1].balance, 0.001)
    }
}
