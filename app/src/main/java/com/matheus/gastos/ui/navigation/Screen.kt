package com.matheus.gastos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface Screen {
    val route: String
    val title: String
    val icon: ImageVector
}

object Expenses : Screen {
    override val route = "expenses"
    override val title = "Gastos"
    override val icon = Icons.Filled.ShoppingCart
}

object Cards : Screen {
    override val route = "cards"
    override val title = "Cartões"
    override val icon = Icons.Filled.CreditCard
}

object Reports : Screen {
    override val route = "reports"
    override val title = "Relatórios"
    override val icon = Icons.Filled.TrendingUp
}

object Settings : Screen {
    override val route = "settings"
    override val title = "Configurações"
    override val icon = Icons.Filled.Settings
}

object AddExpense : Screen {
    override val route = "add_expense"
    override val title = "Novo Gasto"
    override val icon = Icons.Filled.Add
}

object EditExpense : Screen {
    override val route = "edit_expense/{expenseId}"
    override val title = "Editar Gasto"
    override val icon = Icons.Filled.Add
}

object AddCard : Screen {
    override val route = "add_card"
    override val title = "Novo Cartão"
    override val icon = Icons.Filled.CreditCard
}

object EditCard : Screen {
    override val route = "edit_card/{cardId}"
    override val title = "Editar Cartão"
    override val icon = Icons.Filled.CreditCard
}

object Categories : Screen {
    override val route = "categories"
    override val title = "Categorias"
    override val icon = Icons.Filled.Category
}

object AddCategory : Screen {
    override val route = "add_category"
    override val title = "Nova Categoria"
    override val icon = Icons.Filled.Add
}

object EditCategory : Screen {
    override val route = "edit_category/{categoryId}"
    override val title = "Editar Categoria"
    override val icon = Icons.Filled.Add
}

object BudgetLimits : Screen {
    override val route = "budget_limits"
    override val title = "Limites de Gasto"
    override val icon = Icons.Filled.Speed
}

val mainScreens = listOf(Expenses, Cards, Reports, Settings)
