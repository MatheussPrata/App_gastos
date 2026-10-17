package com.matheus.gastos.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Estado de aparência compartilhado entre TODAS as instâncias de ViewModel.
 *
 * Por que existe: cada rota do NavHost cria seu próprio `MainScreen` e, portanto,
 * sua própria instância de `SettingsViewModel` (escopo do back stack entry).
 * Sem um estado comum, alterar o tema em Configurações atualizava só aquela
 * instância e o `MainActivity` — que hospeda o `GastosTheme` — nunca era notificado.
 *
 * Usa `mutableStateOf` do Compose para que qualquer composable que leia
 * [themeMode]/[accentColor] recomponha imediatamente na troca.
 */
object AppearanceState {
    /** 0 = Sistema, 1 = Claro, 2 = Escuro */
    var themeMode by mutableIntStateOf(0)
        private set

    var accentColor by mutableIntStateOf(0xFF6750A4.toInt())
        private set

    var isLoaded by mutableStateOf(false)
        private set

    fun apply(themeMode: Int, accentColor: Int) {
        this.themeMode = themeMode
        this.accentColor = accentColor
        isLoaded = true
    }

    fun setThemeMode(mode: Int) {
        themeMode = mode
    }

    fun setAccentColor(color: Int) {
        accentColor = color
    }
}
