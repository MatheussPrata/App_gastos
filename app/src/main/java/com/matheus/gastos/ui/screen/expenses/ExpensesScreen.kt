package com.matheus.gastos.ui.screen.expenses

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import com.matheus.gastos.util.addMonths
import com.matheus.gastos.util.isCurrentMonth
import com.matheus.gastos.util.millisUntilNextMidnight
import com.matheus.gastos.util.monthStart
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpensesScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit
) {
    val selectedMonthState = viewModel.selectedMonth.observeAsState(initial = monthStart(System.currentTimeMillis()))
    val selectedMonth: Long = selectedMonthState.value

    // Janela fixa de meses: índice 0 = 24 meses atrás, índice 24 = mês atual, índice 48 = +24 meses.
    val centerPage = 24
    val currentMonthPageBase = remember { monthStart(System.currentTimeMillis()) }

    fun pageMonth(page: Int): Long = addMonths(currentMonthPageBase, page - centerPage)

    fun pageForMonth(millis: Long): Int {
        val calNow = java.util.Calendar.getInstance().apply { timeInMillis = monthStart(currentMonthPageBase) }
        val calOther = java.util.Calendar.getInstance().apply { timeInMillis = monthStart(millis) }
        val delta = (calOther.get(java.util.Calendar.YEAR) - calNow.get(java.util.Calendar.YEAR)) * 12 +
            (calOther.get(java.util.Calendar.MONTH) - calNow.get(java.util.Calendar.MONTH))
        return (centerPage + delta).coerceIn(0, centerPage * 2)
    }

    val pagerState = rememberPagerState(initialPage = pageForMonth(selectedMonth)) { centerPage * 2 + 1 }
    val scope = rememberCoroutineScope()

    // Ao virar a página, seleciona o mês correspondente no ViewModel.
    LaunchedEffect(pagerState.currentPage) {
        val month = pageMonth(pagerState.currentPage)
        if (!isSameMonth(month, selectedMonth)) {
            viewModel.setSelectedMonth(month)
        }
    }

    // Se o mês selecionado mudar por fora (setas, date picker, virada de mês), alinha o pager.
    LaunchedEffect(selectedMonth) {
        val target = pageForMonth(selectedMonth)
        if (target != pagerState.currentPage) {
            pagerState.scrollToPage(target)
        }
    }

    // Virada automática do mês: ao voltar para o app e à meia-noite local.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshToCurrentMonthIfNeeded()
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(millisUntilNextMidnight())
            viewModel.refreshToCurrentMonthIfNeeded()
        }
    }

    val transactionsState = viewModel.currentMonthTransactionsWithBalance.observeAsState(initial = emptyList())
    val categoriesState = categoryViewModel.categories.observeAsState(initial = emptyList())
    val cardsState = creditCardViewModel.allCards.observeAsState(initial = emptyList())
    val balanceState = viewModel.balance.observeAsState(initial = 0.0)

    val transactionsWithBalance: List<Pair<Transaction, Double>> = transactionsState.value
    val categories: List<Category> = categoriesState.value
    val cards: List<CreditCard> = cardsState.value
    val currentBalance: Double = balanceState.value

    val categoryMap = categories.associateBy { it.id }
    val cardMap = cards.associateBy { it.id }

    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Gastos", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.size(12.dp))
                        IconButton(onClick = {
                            scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) }
                        }, enabled = pagerState.currentPage > 0) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
                        }
                        Text(
                            text = formatMonthLabel(selectedMonth),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = {
                            scope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(centerPage * 2)) }
                        }, enabled = pagerState.currentPage < centerPage * 2) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Proximo mes")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar transacao")
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            key = { page -> page }
        ) { page ->
            if (page == pagerState.currentPage) {
                Column(modifier = Modifier.fillMaxSize()) {
                    BalanceCard(currentBalance = currentBalance)

                    Spacer(modifier = Modifier.height(8.dp))

                    if (transactionsWithBalance.isEmpty()) {
                        EmptyExpensesState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(transactionsWithBalance) { pair ->
                                val transaction = pair.first
                                val runningBalance = pair.second
                                val category = transaction.categoryId?.let { categoryMap[it] }
                                val card = transaction.creditCardId?.let { cardMap[it] }
                                TransactionItem(
                                    transaction = transaction,
                                    runningBalance = runningBalance,
                                    category = category,
                                    card = card,
                                    onEdit = { onEditExpense(transaction.id) },
                                    onDelete = { pendingDeleteId = transaction.id }
                                )
                            }
                        }
                    }
                }
            } else {
                // Páginas vizinhas não exibem dados: o ViewModel tem um único mês selecionado.
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Excluir transacao") },
            text = { Text("Deseja realmente excluir esta transacao?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.deleteById(id)
                        pendingDeleteId = null
                    }
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    runningBalance: Double,
    category: Category?,
    card: CreditCard?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val amountColor = if (transaction.isExpense) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(transaction.date))
    val money = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val sign = if (transaction.isExpense) "-" else "+"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (category != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(category.color), RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = category.name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (card != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(card.color), RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = card.name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$sign ${money.format(transaction.amount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Saldo: ${money.format(runningBalance)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (runningBalance >= 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            if (!transaction.notes.isNullOrBlank()) {
                Text(
                    text = transaction.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(currentBalance: Double) {
    val money = NumberFormat.getCurrencyInstance(Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Saldo do mes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = money.format(currentBalance),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentBalance >= 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (currentBalance >= 0) "Positivo" else "Negativo",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyExpensesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhuma despesa cadastrada",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque no + para adicionar",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMonthLabel(millis: Long): String {
    val month = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(Date(millis))
    return month.replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
}

private fun isSameMonth(a: Long, b: Long): Boolean {
    val ca = java.util.Calendar.getInstance().apply { timeInMillis = a }
    val cb = java.util.Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(java.util.Calendar.YEAR) == cb.get(java.util.Calendar.YEAR) &&
        ca.get(java.util.Calendar.MONTH) == cb.get(java.util.Calendar.MONTH)
}
