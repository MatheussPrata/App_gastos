package com.matheus.gastos

import com.matheus.gastos.ui.theme.AppearanceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceStateTest {

    @Test
    fun apply_atualiza_tema_cor_e_isLoaded() {
        AppearanceState.apply(2, 0xFFFF0000.toInt())

        assertEquals(2, AppearanceState.themeMode)
        assertEquals(0xFFFF0000.toInt(), AppearanceState.accentColor)
        assertTrue(AppearanceState.isLoaded)
    }

    @Test
    fun setThemeMode_altera_tema() {
        AppearanceState.apply(0, 0xFF6750A4.toInt())
        AppearanceState.updateThemeMode(1)

        assertEquals(1, AppearanceState.themeMode)
    }

    @Test
    fun setAccentColor_altera_cor() {
        AppearanceState.apply(0, 0xFF6750A4.toInt())
        AppearanceState.updateAccentColor(0xFF2196F3.toInt())

        assertEquals(0xFF2196F3.toInt(), AppearanceState.accentColor)
    }
}
