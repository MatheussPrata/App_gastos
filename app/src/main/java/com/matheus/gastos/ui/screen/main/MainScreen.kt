package com.matheus.gastos.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.matheus.gastos.ui.navigation.Cards
import com.matheus.gastos.ui.navigation.Expenses
import com.matheus.gastos.ui.navigation.Reports
import com.matheus.gastos.ui.navigation.Screen
import com.matheus.gastos.ui.navigation.Settings
import com.matheus.gastos.ui.navigation.mainScreens
import com.matheus.gastos.ui.screen.add_card.AddCardDialog
import com.matheus.gastos.ui.screen.cards.CardsScreen
import com.matheus.gastos.ui.screen.expenses.ExpensesScreen
import com.matheus.gastos.ui.screen.reports.ReportsScreen
import com.matheus.gastos.ui.screen.settings.SettingsScreen
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.SettingsViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel

@Composable
fun MainScreen(
    currentScreen: Screen = Expenses,
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToEditExpense: (Long) -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToBudgetLimits: () -> Unit = {},
    onNavigateToTab: (Screen) -> Unit = {}
) {
    val transactionViewModel: TransactionViewModel = hiltViewModel()
    val categoryViewModel: CategoryViewModel = hiltViewModel()
    val creditCardViewModel: CreditCardViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    var selectedScreen by remember(currentScreen) { mutableStateOf(currentScreen) }

    // Estado do popup de cartão (adicionar/editar) — vale para qualquer aba.
    var showCardDialog by remember { mutableStateOf(false) }
    var editingCardId by remember { mutableStateOf(0L) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                // surfaceContainer e derivado da cor de destaque em Theme.kt,
                // entao a barra acompanha a troca de tema.
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                mainScreens.forEach { screen ->
                    val isSelected = selectedScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedScreen = screen
                            onNavigateToTab(screen)
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedScreen) {
                Expenses -> ExpensesScreen(
                    viewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onAddExpense = onNavigateToAddExpense,
                    onEditExpense = onNavigateToEditExpense
                )
                Cards -> CardsScreen(
                    viewModel = creditCardViewModel,
                    transactionViewModel = transactionViewModel,
                    onAddCard = {
                        editingCardId = 0L
                        showCardDialog = true
                    },
                    onEditCard = { id ->
                        editingCardId = id
                        showCardDialog = true
                    }
                )
                Reports -> ReportsScreen(
                    transactionViewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel
                )
                Settings -> SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onNavigateToCategories = onNavigateToCategories,
                    onNavigateToBudgetLimits = onNavigateToBudgetLimits
                )
                else -> {}
            }
        }

        if (showCardDialog) {
            AddCardDialog(
                cardId = editingCardId,
                onDismiss = { showCardDialog = false },
                creditCardViewModel = creditCardViewModel
            )
        }
    }
}
