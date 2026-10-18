package com.matheus.gastos

import com.matheus.gastos.ui.components.sliceIndexAtAngle
import com.matheus.gastos.ui.components.sliceSweepAngles
import org.junit.Assert.assertEquals
import org.junit.Test

class ChartTapTest {

    private val metades = sliceSweepAngles(listOf(50f, 50f)) // 180 / 180
    private val quartos = sliceSweepAngles(listOf(1f, 1f, 1f, 1f)) // 90 cada

    @Test
    fun intervalosCorretos_duasFatias() {
        assertEquals(0, sliceIndexAtAngle(metades, 0f))
        assertEquals(0, sliceIndexAtAngle(metades, 90f))
        assertEquals(0, sliceIndexAtAngle(metades, 179.9f))
        assertEquals(1, sliceIndexAtAngle(metades, 180f))
        assertEquals(1, sliceIndexAtAngle(metades, 270f))
    }

    @Test
    fun intervalosCorretos_quatroFatias() {
        assertEquals(0, sliceIndexAtAngle(quartos, 0f))
        assertEquals(0, sliceIndexAtAngle(quartos, 89.99f))
        assertEquals(1, sliceIndexAtAngle(quartos, 90f))
        assertEquals(2, sliceIndexAtAngle(quartos, 180f))
        assertEquals(3, sliceIndexAtAngle(quartos, 270f))
    }

    @Test
    fun bordaDoCirculo_360EquivaleA0() {
        assertEquals(0, sliceIndexAtAngle(quartos, 360f))
        assertEquals(0, sliceIndexAtAngle(metades, 360f))
    }

    @Test
    fun angulo359_9_retornaUltimaFatia() {
        assertEquals(3, sliceIndexAtAngle(quartos, 359.9f))
        assertEquals(1, sliceIndexAtAngle(metades, 359.9f))
    }

    @Test
    fun listaVazia_retornaMenosUm() {
        assertEquals(-1, sliceIndexAtAngle(emptyList(), 10f))
    }

    @Test
    fun listaSemAngulosPositivos_retornaMenosUm() {
        assertEquals(-1, sliceIndexAtAngle(listOf(0f, 0f), 10f))
    }

    @Test
    fun anguloInvalido_retornaMenosUm() {
        assertEquals(-1, sliceIndexAtAngle(quartos, -1f))
        assertEquals(-1, sliceIndexAtAngle(quartos, 360.5f))
        assertEquals(-1, sliceIndexAtAngle(quartos, Float.NaN))
    }

    @Test
    fun fatiaComAnguloZero_eIgnorada() {
        // 0 -> 90 (idx1), 90 -> 180 (idx2)
        val sweeps = listOf(0f, 90f, 90f)
        assertEquals(1, sliceIndexAtAngle(sweeps, 0f))
        assertEquals(1, sliceIndexAtAngle(sweeps, 45f))
        assertEquals(2, sliceIndexAtAngle(sweeps, 90f))
        assertEquals(2, sliceIndexAtAngle(sweeps, 359.9f))
    }

    @Test
    fun somaDosAngulosContinua360() {
        val casos = listOf(
            listOf(1f, 1f, 1f),
            listOf(10f, 20f, 30f, 40f, 7.5f),
            listOf(0.1f, 0.2f, 0.7f),
            listOf(33f, 33f, 34f),
            listOf(1f, 2f, 3f, 4f, 5f, 6f, 7f)
        )
        casos.forEach { valores ->
            val soma = sliceSweepAngles(valores).sum()
            assertEquals(360f, soma, 0.0001f)
        }
    }

    @Test
    fun todaFatiaTemAnguloTocavel() {
        // Cada fatia deve ser encontrada ao tocar no meio do seu intervalo.
        val sweeps = sliceSweepAngles(listOf(30f, 10f, 60f))
        var start = 0f
        sweeps.forEachIndexed { index, sweep ->
            val meio = start + sweep / 2f
            assertEquals(index, sliceIndexAtAngle(sweeps, meio))
            start += sweep
        }
    }
}
