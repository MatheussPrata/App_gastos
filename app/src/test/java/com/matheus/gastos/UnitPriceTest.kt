package com.matheus.gastos

import com.matheus.gastos.util.PriceOption
import com.matheus.gastos.util.PriceUnit
import com.matheus.gastos.util.comparePrices
import com.matheus.gastos.util.parsePriceInput
import com.matheus.gastos.util.parseQuantityInput
import com.matheus.gastos.util.round2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comparador de preco por unidade (item 36):
 * normaliza g/kg/ml/L para achar a embalagem mais economica.
 */
class UnitPriceTest {

    private fun opt(price: String, qty: String, unit: PriceUnit) = PriceOption(
        label = "$qty${unit.label}",
        price = parsePriceInput(price)!!,
        quantity = parseQuantityInput(qty)!!,
        unit = unit
    )

    @Test
    fun `500 g por 8 reais custa 16 por kg`() {
        val option = opt("8,00", "500", PriceUnit.GRAM)
        assertEquals(16.0, round2(option.unitPrice), 0.001)
    }

    @Test
    fun `1 kg por 14 reais custa 14 por kg`() {
        val option = opt("14,00", "1", PriceUnit.KILOGRAM)
        assertEquals(14.0, round2(option.unitPrice), 0.001)
    }

    @Test
    fun `1 kg e mais barato que 500 g`() {
        val result = comparePrices(
            listOf(opt("8,00", "500", PriceUnit.GRAM), opt("14,00", "1", PriceUnit.KILOGRAM))
        )!!
        assertEquals(14.0, round2(result.best.unitPrice), 0.001)
    }

    @Test
    fun `litro e mililitro comparam na mesma base`() {
        val ml = opt("5,00", "500", PriceUnit.MILLILITER)
        val liter = opt("8,00", "1", PriceUnit.LITER)
        assertEquals(10.0, round2(ml.unitPrice), 0.001)
        assertEquals(8.0, round2(liter.unitPrice), 0.001)

        val result = comparePrices(listOf(ml, liter))!!
        assertEquals(8.0, round2(result.best.unitPrice), 0.001)
    }

    @Test
    fun `economia considera a diferenca por kg`() {
        val result = comparePrices(
            listOf(opt("8,00", "500", PriceUnit.GRAM), opt("14,00", "1", PriceUnit.KILOGRAM))
        )!!
        // 16 - 14 = 2 por kg, comprando 1 kg do melhor.
        assertEquals(2.0, round2(result.savings), 0.001)
        assertEquals(12.5, round2(result.savingsPercent), 0.001)
    }

    @Test
    fun `uma opcao so nao compara`() {
        assertNull(comparePrices(listOf(opt("8,00", "500", PriceUnit.GRAM))))
    }

    @Test
    fun `quantidade zerada e ignorada`() {
        assertNull(
            comparePrices(
                listOf(
                    opt("8,00", "500", PriceUnit.GRAM),
                    PriceOption(price = 10.0, quantity = 0.0, unit = PriceUnit.KILOGRAM)
                )
            )
        )
    }

    @Test
    fun `aceita virgula e ponto no preco`() {
        assertEquals(12.5, parsePriceInput("12,50")!!, 0.001)
        assertEquals(12.5, parsePriceInput("12.50")!!, 0.001)
        assertEquals(12.5, parsePriceInput("R$ 12,50")!!, 0.001)
        assertNull(parsePriceInput("abc"))
        assertNull(parsePriceInput("0"))
    }

    @Test
    fun `tres opcoes escolhem a melhor`() {
        val result = comparePrices(
            listOf(
                opt("8,00", "500", PriceUnit.GRAM),
                opt("14,00", "1", PriceUnit.KILOGRAM),
                opt("25,00", "2", PriceUnit.KILOGRAM)
            )
        )!!
        assertEquals(12.5, round2(result.best.unitPrice), 0.001)
        assertTrue(result.bestIndex == 2)
    }
}
