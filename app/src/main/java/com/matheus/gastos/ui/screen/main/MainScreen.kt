package com.matheus.gastos.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matheus.gastos.ui.navigation.Cards
import com.matheus.gastos.ui.navigation.Expenses
import com.matheus.gastos.ui.navigation.Reports
import com.matheus.gastos.ui.navigation.Screen
import com.matheus.gastos.ui.navigation.Settings
import com.matheus.gastos.ui.navigation.mainScreens
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
    onNavigateToAddCard: () -> Unit = {},
    onNavigateToEditCard: (Long) -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToTab: (Screen) -> Unit = {}
) {
    val transactionViewModel: TransactionViewModel = hiltViewModel()
    val categoryViewModel: CategoryViewModel = hiltViewModel()
    val creditCardViewModel: CreditCardViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    var selectedScreen by remember(currentScreen) { mutableStateOf(currentScreen) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
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
                    onAddCard = onNavigateToAddCard,
                    onEditCard = onNavigateToEditCard
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
                    onNavigateToCategories = onNavigateToCategories
                )
                else -> {}
            }
        }

        CustomBottomNavigation(
            selectedScreen = selectedScreen,
            onScreenSelected = {
                selectedScreen = it
                onNavigateToTab(it)
            }
        )
    }
}

@Composable
fun CustomBottomNavigation(
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        mainScreens.forEach { screen ->
            val isSelected = selectedScreen == screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clickable { onScreenSelected(screen) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
