package com.matheus.gastos

import com.matheus.gastos.viewmodel.TransactionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Testes do parser de valor usado no formulário de transação.
 * Aceita "10", "10,50", "10.50", "1.234,56" e rejeita vazio/negativo/inválido.
 */
class AmountParserTest {

    @Test
    fun `parses plain integer`() {
        assertEquals(10.0, TransactionViewModel.parseAmount("10")!!, 0.001)
    }

    @Test
    fun `parses comma decimal`() {
        assertEquals(10.5, TransactionViewModel.parseAmount("10,50")!!, 0.001)
    }

    @Test
    fun `parses dot decimal`() {
        assertEquals(10.5, TransactionViewModel.parseAmount("10.50")!!, 0.001)
    }

    @Test
    fun `parses comma as thousands with dot decimal`() {
        assertEquals(1234.56, TransactionViewModel.parseAmount("1.234,56")!!, 0.001)
    }

    @Test
    fun `parses value with surrounding spaces`() {
        assertEquals(42.0, TransactionViewModel.parseAmount("  42  ")!!, 0.001)
    }

    @Test
    fun `returns null for empty string`() {
        assertNull(TransactionViewModel.parseAmount(""))
    }

    @Test
    fun `returns null for blank string`() {
        assertNull(TransactionViewModel.parseAmount("   "))
    }

    @Test
    fun `returns null for non numeric input`() {
        assertNull(TransactionViewModel.parseAmount("abc"))
    }

    @Test
    fun `returns null for zero`() {
        assertNull(TransactionViewModel.parseAmount("0"))
    }

    @Test
    fun `returns null for negative value`() {
        assertNull(TransactionViewModel.parseAmount("-5"))
    }
}
