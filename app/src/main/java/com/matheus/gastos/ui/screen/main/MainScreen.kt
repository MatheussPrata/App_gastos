package com.matheus.gastos.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matheus.gastos.ui.navigation.Cards
import com.matheus.gastos.ui.navigation.Compare
import com.matheus.gastos.ui.navigation.Connection
import com.matheus.gastos.ui.navigation.Expenses
import com.matheus.gastos.ui.navigation.Reports
import com.matheus.gastos.ui.navigation.RecurringTransactions
import com.matheus.gastos.ui.navigation.Screen
import com.matheus.gastos.ui.navigation.Settings
import com.matheus.gastos.ui.navigation.Shopping
import com.matheus.gastos.ui.navigation.mainScreens
import com.matheus.gastos.ui.screen.add_card.AddCardDialog
import com.matheus.gastos.ui.screen.cards.CardsScreen
import com.matheus.gastos.ui.screen.compare.ComparePricesScreen
import com.matheus.gastos.ui.screen.connection.ConnectionScreen
import com.matheus.gastos.ui.screen.expenses.ExpensesScreen
import com.matheus.gastos.ui.screen.recurring.RecurringTransactionsScreen
import com.matheus.gastos.ui.screen.reports.ReportsScreen
import com.matheus.gastos.ui.screen.settings.SettingsScreen
import com.matheus.gastos.ui.screen.shopping.ShoppingScreen
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.ConnectionViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.RecurringTransactionViewModel
import com.matheus.gastos.viewmodel.SettingsViewModel
import com.matheus.gastos.viewmodel.ShoppingViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch

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
    val connectionViewModel: ConnectionViewModel = hiltViewModel()
    val recurringTransactionViewModel: RecurringTransactionViewModel = hiltViewModel()
    val shoppingViewModel: ShoppingViewModel = hiltViewModel()

    var selectedScreen by remember(currentScreen) { mutableStateOf(currentScreen) }

    // Estado do popup de cartão (adicionar/editar) — vale para qualquer aba.
    var showCardDialog by remember { mutableStateOf(false) }
    var editingCardId by remember { mutableStateOf(0L) }

    // Menu lateral (drawer): puxa da esquerda e cobre a página com as opções.
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Gastos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text(Expenses.title) },
                    icon = { Icon(Expenses.icon, contentDescription = null) },
                    selected = selectedScreen == Expenses,
                    onClick = {
                        selectedScreen = Expenses
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Reports.title) },
                    icon = { Icon(Reports.icon, contentDescription = null) },
                    selected = selectedScreen == Reports,
                    onClick = {
                        selectedScreen = Reports
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Comparar Preços") },
                    icon = { Icon(Compare.icon, contentDescription = null) },
                    selected = selectedScreen == Compare,
                    onClick = {
                        selectedScreen = Compare
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Conexão") },
                    icon = { Icon(Connection.icon, contentDescription = null) },
                    selected = selectedScreen == Connection,
                    onClick = {
                        selectedScreen = Connection
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Recorrentes") },
                    icon = { Icon(RecurringTransactions.icon, contentDescription = null) },
                    selected = selectedScreen == RecurringTransactions,
                    onClick = {
                        selectedScreen = RecurringTransactions
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Shopping.title) },
                    icon = { Icon(Shopping.icon, contentDescription = null) },
                    selected = selectedScreen == Shopping,
                    onClick = {
                        selectedScreen = Shopping
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Cards.title) },
                    icon = { Icon(Cards.icon, contentDescription = null) },
                    selected = selectedScreen == Cards,
                    onClick = {
                        selectedScreen = Cards
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text(Settings.title) },
                    icon = { Icon(Settings.icon, contentDescription = null) },
                    selected = selectedScreen == Settings,
                    onClick = {
                        selectedScreen = Settings
                        scope.launch { drawerState.close() }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Categorias") },
                    icon = { Icon(Icons.Filled.Category, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCategories()
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Limites de Gasto") },
                    icon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToBudgetLimits()
                    }
                )
            }
        }
    ) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // 5 abas + botao central (+).
            Box {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val tabs = listOf(Expenses, Connection, RecurringTransactions, Shopping, Reports)
                    val left = tabs.take(tabs.size / 2)
                    val right = tabs.drop(tabs.size / 2)

                    left.forEach { screen ->
                        NavItem(screen, selectedScreen) {
                            selectedScreen = screen
                            onNavigateToTab(screen)
                        }
                    }

                    // Vao central onde o botao (+) flutua.
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToAddExpense,
                        icon = { },
                        label = { },
                        enabled = false
                    )

                    right.forEach { screen ->
                        NavItem(screen, selectedScreen) {
                            selectedScreen = screen
                            onNavigateToTab(screen)
                        }
                    }
                }

                // Botao central (+) saltando para fora da navbar.
                FloatingActionButton(
                    onClick = onNavigateToAddExpense,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-26).dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Adicionar transacao")
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
                    onOpenDrawer = openDrawer,
                    viewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onAddExpense = onNavigateToAddExpense,
                    onEditExpense = onNavigateToEditExpense
                )
                Compare -> ComparePricesScreen(onOpenDrawer = openDrawer)
                Connection -> ConnectionScreen(
                    connectionViewModel = connectionViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateBack = { /* stay */ }
                )
                RecurringTransactions -> RecurringTransactionsScreen(
                    recurringTransactionViewModel = recurringTransactionViewModel,
                    categoryViewModel = categoryViewModel,
                    onNavigateBack = { /* stay */ }
                )
                Shopping -> ShoppingScreen(
                    shoppingViewModel = shoppingViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateBack = { /* stay */ }
                )
                Reports -> ReportsScreen(
                    transactionViewModel = transactionViewModel,
                    onOpenDrawer = openDrawer,
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel
                )
                Settings -> SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onOpenDrawer = openDrawer,
                    onNavigateToCards = { /* handled by drawer */ },
                    categoryViewModel = categoryViewModel,
                    creditCardViewModel = creditCardViewModel,
                    onNavigateToCategories = onNavigateToCategories,
                    onNavigateToBudgetLimits = onNavigateToBudgetLimits
                )
                Cards -> CardsScreen(
                    viewModel = creditCardViewModel,
                    onOpenDrawer = openDrawer,
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
}

/**
 * Item da barra inferior. O circulo de fundo so aparece na aba ativa
 * (antes o Material desenhava um indicador quadrado).
 */
@Composable
private fun androidx.compose.foundation.layout.RowScope.NavItem(
    screen: Screen,
    selectedScreen: Screen,
    onClick: () -> Unit
) {
    val isSelected = selectedScreen == screen
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.title,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        label = { Text(screen.title) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}