package com.matheus.gastos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matheus.gastos.ui.screen.add_expense.AddExpenseScreen
import com.matheus.gastos.ui.screen.budget.BudgetLimitsScreen
import com.matheus.gastos.ui.screen.categories.CategoriesScreen
import com.matheus.gastos.ui.screen.main.MainScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Expenses.route) {
        composable(Expenses.route) {
            MainScreen(
                currentScreen = Expenses,
                onNavigateToAddExpense = { navController.navigate(AddExpense.route) },
                onNavigateToEditExpense = { id -> navController.navigate("edit_expense/$id") },
                onNavigateToCategories = { navController.navigate(Categories.route) },
                onNavigateToBudgetLimits = { navController.navigate(BudgetLimits.route) }
            )
        }
        composable(Cards.route) {
            MainScreen(
                currentScreen = Cards,
                onNavigateToAddExpense = { navController.navigate(AddExpense.route) },
                onNavigateToEditExpense = { id -> navController.navigate("edit_expense/$id") },
                onNavigateToCategories = { navController.navigate(Categories.route) },
                onNavigateToBudgetLimits = { navController.navigate(BudgetLimits.route) }
            )
        }
        composable(Reports.route) {
            MainScreen(
                currentScreen = Reports,
                onNavigateToCategories = { navController.navigate(Categories.route) },
                onNavigateToBudgetLimits = { navController.navigate(BudgetLimits.route) }
            )
        }
        composable(Settings.route) {
            MainScreen(
                currentScreen = Settings,
                onNavigateToCategories = { navController.navigate(Categories.route) },
                onNavigateToBudgetLimits = { navController.navigate(BudgetLimits.route) }
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
            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: 0L
            AddExpenseScreen(
                expenseId = expenseId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(BudgetLimits.route) {
            BudgetLimitsScreen(onBack = { navController.popBackStack() })
        }
        composable(Categories.route) {
            CategoriesScreen(
                onClose = { navController.popBackStack() }
            )
        }
        composable(
            route = EditCategory.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            CategoriesScreen(
                categoryId = categoryId,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
