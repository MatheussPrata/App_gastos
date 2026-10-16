package com.matheus.gastos.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.getLong
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.matheus.gastos.R
import com.matheus.gastos.ui.navigation.Expenses
import com.matheus.gastos.ui.navigation.Cards
import com.matheus.gastos.ui.navigation.Reports
import com.matheus.gastos.ui.navigation.Settings
import com.matheus.gastos.ui.navigation.mainScreens
import com.matheus.gastos.ui.screen.add_card.AddCardScreen
import com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen
import com.matheus.gastos.ui.screen.cards.CardsScreen
import com.matheus.gastos.ui.screen.categories.CategoriesScreen
import com.matheus.gastos.ui.screen.expenses.ExpensesScreen
import com.matheus.gastos.ui.screen.reports.ReportsScreen
import com.matheus.gastos.ui.screen.settings.SettingsScreen
import com.matheus.gastos.ui.theme.GastosTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.SettingsViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentScreen: Screen = Screen.Expenses,
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToEditExpense: (Long) -> Unit = {},
    onNavigateToAddCard: () -> Unit = {},
    onNavigateToEditCard: (Long) -> Unit = {},
    onNavigateToCategories: () -> Unit = {}
) {
    val navController = rememberNavController()
    val transactionViewModel: TransactionViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val creditCardViewModel: CreditCardViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    var selectedScreen by remember { mutableStateOf(currentScreen) }

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(navController, startDestination = selectedScreen.route) {
            composable(Expenses.route) {
                ExpensesScreen(
                    viewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onAddExpense = onNavigateToAddExpense,
                    onEditExpense = onNavigateToEditExpense
                )
            }
            composable(Cards.route) {
                CardsScreen(
                    viewModel = creditCardViewModel,
                    transactionViewModel = transactionViewModel,
                    onAddCard = onNavigateToAddCard,
                    onEditCard = onNavigateToEditCard
                )
            }
            composable(Reports.route) {
                ReportsScreen(
                    transactionViewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel
                )
            }
            composable(Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onNavigateToCategories = onNavigateToCategories
                )
            }
            composable(
                route = "add_expense"
            ) {
                AddExpenseScreen(
                    transactionViewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_expense/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.getLong("expenseId") ?: 0L
                AddExpenseScreen(
                    expenseId = expenseId,
                    transactionViewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = "add_card"
            ) {
                AddCardScreen(
                    creditCardViewModel = creditCardViewModel,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_card/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cardId = backStackEntry.getLong("cardId") ?: 0L
                AddCardScreen(
                    cardId = cardId,
                    creditCardViewModel = creditCardViewModel,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(route = "categories") {
                CategoriesScreen(
                    categoryViewModel = categoryViewModel,
                    onClose = { navController.popBackStack() }
                )
            }
        }

        // Custom bottom navigation
        CustomBottomNavigation(
            selectedScreen = selectedScreen,
            onScreenSelected = { selectedScreen = it }
        )
    }
}

@Composable
fun CustomBottomNavigation(
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        mainScreens.forEach { screen ->
            val isSelected = selectedScreen == screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                        tint = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.Modifier.padding(top = 4.dp))
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}