package com.matheus.gastos

import com.matheus.gastos.util.addMonths
import com.matheus.gastos.util.isSameMonth
import com.matheus.gastos.util.monthKey
import com.matheus.gastos.util.monthStart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MonthUtilsTest {

    private fun millis(year: Int, month1Based: Int, day: Int, hour: Int = 15, minute: Int = 30): Long {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(year, month1Based - 1, day, hour, minute, 0)
        return cal.timeInMillis
    }

    private fun ymd(millis: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return Triple(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    @Test
    fun monthStart_retornaPrimeiroInstanteDoMes() {
        val start = monthStart(millis(2026, 10, 18))
        val cal = Calendar.getInstance()
        cal.timeInMillis = start
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test
    fun monthKey_formatoAnoMes() {
        assertEquals("2026-10", monthKey(millis(2026, 10, 18)))
        assertEquals("2026-01", monthKey(millis(2026, 1, 1)))
        assertEquals("2025-12", monthKey(millis(2025, 12, 31, 23, 59)))
    }

    @Test
    fun addMonths_avancaERetornaPrimeiroDia() {
        val next = addMonths(millis(2026, 10, 18), 1)
        assertEquals("2026-11", monthKey(next))
        assertEquals(Triple(2026, 11, 1), ymd(next))
    }

    @Test
    fun addMonths_31JaneiroMaisUmCaiEmFevereiro() {
        val feb = addMonths(millis(2026, 1, 31), 1)
        assertEquals("2026-02", monthKey(feb))
        assertEquals(Triple(2026, 2, 1), ymd(feb))
    }

    @Test
    fun addMonths_voltaParaOMesAnterior() {
        val prev = addMonths(millis(2026, 1, 15), -1)
        assertEquals("2025-12", monthKey(prev))
    }

    @Test
    fun addMonths_trocaDeAno() {
        assertEquals("2027-01", monthKey(addMonths(millis(2026, 12, 10), 1)))
        assertEquals("2025-12", monthKey(addMonths(millis(2026, 12, 10), -12)))
    }

    @Test
    fun isSameMonth_mesmoMesDiasDiferentes() {
        assertTrue(isSameMonth(millis(2026, 10, 1), millis(2026, 10, 31, 23, 59)))
        assertFalse(isSameMonth(millis(2026, 10, 31), millis(2026, 11, 1)))
        assertNotEquals(monthKey(millis(2026, 12, 31)), monthKey(millis(2027, 1, 1)))
    }
}
