package com.matheus.gastos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.navArgument
import com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen
import com.matheus.gastos.ui.screen.budget.BudgetLimitsScreen
import com.matheus.gastos.ui.screen.categories.CategoriesScreen
import com.matheus.gastos.ui.screen.connection.ConnectionScreen
import com.matheus.gastos.ui.screen.main.MainScreen
import com.matheus.gastos.ui.screen.recurring.RecurringTransactionsScreen
import com.matheus.gastos.ui.screen.settings.SettingsScreen
import com.matheus.gastos.ui.screen.shopping.ShoppingScreen

sealed class Screen(val route: String, val title: String, val icon: String) {
    object Expenses : Screen("expenses", "Gastos", "ic_expenses")
    object Cards : Screen("cards", "Cartões", "ic_cards")
    object Reports : Screen("reports", "Relatórios", "ic_reports")
    object Settings : Screen("settings", "Configurações", "ic_settings")
    object Connection : Screen("connection", "Conexão", "ic_link")
    object RecurringTransactions : Screen("recurring_transactions", "Recorrentes", "ic_repeat")
    object Shopping : Screen("shopping", "Compras", "ic_shopping_cart")
    object AddExpense : Screen("add_expense", "Novo Gasto", "")
    object EditExpense : Screen("edit_expense/{expenseId}", "Editar Gasto", "")
    object Categories : Screen("categories", "Categorias", "")
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Expenses.route) {
        composable(Expenses.route) {
            MainScreen(
                currentScreen = Expenses,
                onNavigateToAddExpense = { navController.navigate(AddExpense.route) },
                onNavigateToEditExpense = { id -> navController.navigate("edit_expense/$id") },
                onNavigateToCategories = { navController.navigate(Categories.route) }
            )
        }
        composable(Cards.route) {
            MainScreen(
                currentScreen = Cards,
                onNavigateToAddExpense = { navController.navigate(AddExpense.route) },
                onNavigateToEditExpense = { id -> navController.navigate("edit_expense/$id") },
                onNavigateToCategories = { navController.navigate(Categories.route) }
            )
        }
        composable(Reports.route) {
            MainScreen(
                currentScreen = Reports,
                onNavigateToAddExpense = { navController.navigate(AddExpense.route) },
                onNavigateToEditExpense = { id -> navController.navigate("edit_expense/$id") },
                onNavigateToCategories = { navController.navigate(Categories.route) }
            )
        }
        composable(Settings.route) {
            MainScreen(
                currentScreen = Settings,
                onNavigateToAddExpense = { navController.navigate(AddExpense.route) },
                onNavigateToEditExpense = { id -> navController.navigate("edit_expense/$id") },
                onNavigateToCategories = { navController.navigate(Categories.route) }
            )
        }
        composable(Connection.route) {
            ConnectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(RecurringTransactions.route) {
            RecurringTransactionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Shopping.route) {
            ShoppingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AddExpense.route) {
            com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen(
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit_expense/{expenseId}",
            arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: 0L
            com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen(
                expenseId = expenseId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = Categories.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            com.matheus.gastos.ui.screen.categories.CategoriesScreen(
                categoryId = categoryId,
                onClose = { navController.popBackStack() }
            )
        }
        composable(Categories.route) {
            com.matheus.gastos.ui.screen.categories.CategoriesScreen(
                onClose = { navController.popBackStack() }
            )
        }
    }
}