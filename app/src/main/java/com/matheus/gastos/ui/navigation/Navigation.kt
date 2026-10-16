package com.matheus.gastos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
// import androidx.hilt.navigation.compose.hiltViewModel  // DISABLED for memory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matheus.gastos.ui.screen.add_card.AddCardScreen
import com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen
import com.matheus.gastos.ui.screen.categories.CategoriesScreen
import com.matheus.gastos.ui.screen.cards.CardsScreen
import com.matheus.gastos.ui.screen.expenses.ExpensesScreen
import com.matheus.gastos.ui.screen.main.MainScreen
import com.matheus.gastos.ui.screen.reports.ReportsScreen
import com.matheus.gastos.ui.screen.settings.SettingsScreen

@Composable
fun AppNavHost(
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToEditExpense: (Long) -> Unit = {},
    onNavigateToAddCard: () -> Unit = {},
    onNavigateToEditCard: (Long) -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToAddCategory: () -> Unit = {},
    onNavigateToEditCategory: (Long) -> Unit = {}
) {
    val navController = rememberNavController()
    var selectedScreen by remember { mutableStateOf(mainScreens[0]) }

    NavHost(navController, startDestination = mainScreens[0].route) {
        composable(mainScreens[0].route) {
            MainScreen(
                currentScreen = mainScreens[0],
                onNavigateToAddExpense = onNavigateToAddExpense,
                onNavigateToEditExpense = onNavigateToEditExpense,
                onNavigateToAddCard = onNavigateToAddCard,
                onNavigateToEditCard = onNavigateToEditCard,
                onNavigateToCategories = onNavigateToCategories
            )
        }
        composable(mainScreens[1].route) {
            MainScreen(
                currentScreen = mainScreens[1],
                onNavigateToAddExpense = onNavigateToAddExpense,
                onNavigateToEditExpense = onNavigateToEditExpense,
                onNavigateToAddCard = onNavigateToAddCard,
                onNavigateToEditCard = onNavigateToEditCard,
                onNavigateToCategories = onNavigateToCategories
            )
        }
        composable(mainScreens[2].route) {
            MainScreen(
                currentScreen = mainScreens[2],
                onNavigateToAddExpense = onNavigateToAddExpense,
                onNavigateToEditExpense = onNavigateToEditExpense,
                onNavigateToAddCard = onNavigateToAddCard,
                onNavigateToEditCard = onNavigateToEditCard,
                onNavigateToCategories = onNavigateToCategories
            )
        }
        composable(mainScreens[3].route) {
            MainScreen(
                currentScreen = mainScreens[3],
                onNavigateToAddExpense = onNavigateToAddExpense,
                onNavigateToEditExpense = onNavigateToEditExpense,
                onNavigateToAddCard = onNavigateToAddCard,
                onNavigateToEditCard = onNavigateToEditCard,
                onNavigateToCategories = onNavigateToCategories
            )
        }
        composable(AddExpense.route) {
            AddExpenseScreen(
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = EditExpense.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.getLong("expenseId", 0L)
            AddExpenseScreen(
                expenseId = expenseId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(AddCard.route) {
            AddCardScreen(
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = EditCard.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.getLong("cardId", 0L)
            AddCardScreen(
                cardId = cardId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Categories.route) {
            CategoriesScreen(
                onClose = { navController.popBackStack() }
            )
        }
        composable(AddCategory.route) {
            CategoriesScreen(
                onClose = { navController.popBackStack() }
            )
        }
        composable(
            route = EditCategory.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.getLong("categoryId", 0L)
            CategoriesScreen(
                categoryId = categoryId,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
