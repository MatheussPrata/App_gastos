package com.matheus.gastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.matheus.gastos.ui.navigation.AppNavHost
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeModeState = settingsViewModel.themeMode.observeAsState(initial = 0)
            val accentColorState = settingsViewModel.accentColor.observeAsState(initial = 0xFF6750A4.toInt())

            val themeMode = themeModeState.value
            val darkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            GastosTheme(
                darkTheme = darkTheme,
                dynamicColor = false,
                accentColor = accentColorState.value
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
