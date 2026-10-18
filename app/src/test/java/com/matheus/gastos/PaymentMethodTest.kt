package com.matheus.gastos

import com.matheus.gastos.data.entity.PaymentMethod
import com.matheus.gastos.data.entity.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentMethodTest {

    @Test
    fun `enum has exactly six values`() {
        assertEquals(6, PaymentMethod.entries.size)
        assertEquals(
            listOf("DINHEIRO", "CARTAO", "PIX", "DEBITO", "BOLETO", "TRANSFERENCIA"),
            PaymentMethod.entries.map { it.name }
        )
    }

    @Test
    fun `every value has a non blank label`() {
        PaymentMethod.entries.forEach {
            assertTrue("label vazio para $it", it.label.isNotBlank())
        }
        assertEquals("Dinheiro", PaymentMethod.DINHEIRO.label)
        assertEquals("Cartão", PaymentMethod.CARTAO.label)
        assertEquals("Pix", PaymentMethod.PIX.label)
        assertEquals("Débito", PaymentMethod.DEBITO.label)
        assertEquals("Boleto", PaymentMethod.BOLETO.label)
        assertEquals("Transferência", PaymentMethod.TRANSFERENCIA.label)
    }

    @Test
    fun `labels are unique`() {
        val labels = PaymentMethod.entries.map { it.label }
        assertEquals(labels.size, labels.toSet().size)
    }

    @Test
    fun `transaction stores payment method`() {
        PaymentMethod.entries.forEach { method ->
            val tx = Transaction(
                description = "Teste $method",
                amount = 10.0,
                date = 0L,
                paymentMethod = method
            )
            assertEquals(method, tx.paymentMethod)
        }
    }

    @Test
    fun `transaction default payment method is cash`() {
        val tx = Transaction(description = "Sem forma", amount = 5.0, date = 0L)
        assertEquals(PaymentMethod.DINHEIRO, tx.paymentMethod)
        assertTrue(tx.isExpense)
    }

    @Test
    fun `transaction copy changes payment method only`() {
        val tx = Transaction(description = "Mercado", amount = 50.0, date = 1L, paymentMethod = PaymentMethod.PIX)
        val updated = tx.copy(paymentMethod = PaymentMethod.CARTAO)
        assertEquals(PaymentMethod.CARTAO, updated.paymentMethod)
        assertEquals(tx.description, updated.description)
        assertEquals(tx.amount, updated.amount, 0.0)
    }

    @Test
    fun `valueOf roundtrip works for persisted names`() {
        PaymentMethod.entries.forEach {
            assertEquals(it, PaymentMethod.valueOf(it.name))
        }
        // nome persistido (Converters) é estável
        assertFalse(PaymentMethod.CARTAO.name.contains("Ç"))
    }
}
