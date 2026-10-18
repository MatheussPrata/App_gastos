package com.matheus.gastos

import com.matheus.gastos.ui.components.barFraction
import com.matheus.gastos.ui.components.percentLabel
import com.matheus.gastos.ui.components.sliceSweepAngles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartMathTest {

    @Test
    fun sliceSweepAngles_quatroPartesIguais_somam360() {
        val angles = sliceSweepAngles(listOf(1f, 1f, 1f, 1f))
        assertEquals(4, angles.size)
        assertEquals(360f, angles.sum(), 0.0001f)
        angles.forEach { assertEquals(90f, it, 0.001f) }
    }

    @Test
    fun sliceSweepAngles_metadeEMetade() {
        val angles = sliceSweepAngles(listOf(50f, 50f))
        assertEquals(360f, angles.sum(), 0.0001f)
        assertEquals(180f, angles[0], 0.001f)
        assertEquals(180f, angles[1], 0.001f)
    }

    @Test
    fun sliceSweepAngles_listaVazia_retornaVazia() {
        assertTrue(sliceSweepAngles(emptyList()).isEmpty())
    }

    @Test
    fun sliceSweepAngles_valoresComZero_zeroRecebeAnguloZero() {
        val angles = sliceSweepAngles(listOf(100f, 0f, 100f))
        assertEquals(3, angles.size)
        assertEquals(0f, angles[1], 0f)
        assertEquals(360f, angles.sum(), 0.0001f)
        assertEquals(180f, angles[0], 0.001f)
        assertEquals(180f, angles[2], 0.001f)
    }

    @Test
    fun sliceSweepAngles_valorUnico_360() {
        val angles = sliceSweepAngles(listOf(42f))
        assertEquals(1, angles.size)
        assertEquals(360f, angles[0], 0.0001f)
    }

    @Test
    fun sliceSweepAngles_somaExataComArredondamento() {
        val angles = sliceSweepAngles(listOf(1f, 1f, 1f))
        assertEquals(360f, angles.sum(), 0f)
    }

    @Test
    fun barFraction_casoNormal() {
        assertEquals(0.5f, barFraction(50.0, 100.0), 0.0001f)
    }

    @Test
    fun barFraction_maxZero_retornaZero() {
        assertEquals(0f, barFraction(10.0, 0.0), 0f)
    }

    @Test
    fun barFraction_valorZero_retornaZero() {
        assertEquals(0f, barFraction(0.0, 100.0), 0f)
    }

    @Test
    fun barFraction_valorMaiorQueMax_clampaEmUm() {
        assertEquals(1f, barFraction(150.0, 100.0), 0f)
    }

    @Test
    fun percentLabel_totalZero() {
        assertEquals("0,0%", percentLabel(10.0, 0.0))
    }

    @Test
    fun percentLabel_normal() {
        assertEquals("25,0%", percentLabel(25.0, 100.0))
    }

    @Test
    fun percentLabel_arredondamento() {
        assertEquals("33,3%", percentLabel(1.0, 3.0))
        assertEquals("66,7%", percentLabel(2.0, 3.0))
        assertEquals("12,3%", percentLabel(12.345, 100.0))
    }
}
