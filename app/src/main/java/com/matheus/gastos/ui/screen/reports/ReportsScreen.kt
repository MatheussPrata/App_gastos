package com.matheus.gastos.ui.screen.reports

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matheus.gastos.data.dao.MonthlyBalance
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.dao.CategoryExpense
import com.matheus.gastos.data.dao.CardExpense
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.ui.components.ChartEntry
import com.matheus.gastos.ui.components.EmptyChartMessage
import com.matheus.gastos.ui.components.HorizontalBarChart
import com.matheus.gastos.ui.components.PieChart
import com.matheus.gastos.ui.components.VerticalMonthlyBars
import com.matheus.gastos.ui.components.percentLabel
import com.matheus.gastos.ui.components.withDistinctColors
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Quantidade de visualizacoes por aba: Lista -> Barras -> Pizza. */
private const val VIEW_COUNT = 3

/**
 * Total de paginas do pager infinito.
 * Alto o suficiente para o usuario nunca ver o limite; o conteudo e ciclico via `page % VIEW_COUNT`.
 */
private const val LOOP_PAGES = 3000

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReportsScreen(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel,
    onOpenDrawer: (() -> Unit)? = null
) {
    val expensesByCategory = transactionViewModel.expensesByCategory.observeAsState(initial = emptyList())
    val expensesByCard = transactionViewModel.expensesByCard.observeAsState(initial = emptyList())
    val totalExpenses = transactionViewModel.totalExpenses.observeAsState(initial = 0.0)
    val totalIncome = transactionViewModel.totalIncome.observeAsState(initial = 0.0)
    val balance = transactionViewModel.balance.observeAsState(initial = 0.0)
    val monthlyBalances = transactionViewModel.monthlyBalances.observeAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = categoryViewModel.categories.observeAsState(initial = emptyList())
    val cards = creditCardViewModel.allCards.observeAsState(initial = emptyList())

    val categoryMap = categories.value.associateBy { it.id }
    val cardMap = cards.value.associateBy { it.id }

    // Indice da visualizacao (0=Lista, 1=Barras, 2=Pizza) por aba.
    val viewByTab = remember { mutableIntStateOf(0) }

    // Pager INFINITO: muitas paginas, sempre comecando no meio.
    // O conteudo real e `page % VIEW_COUNT`, entao arrastar nunca chega no fim:
    // do ultimo (Pizza) volta para o primeiro (Lista) e vice-versa.
    val loopStart = LOOP_PAGES / 2 - (LOOP_PAGES / 2) % VIEW_COUNT
    val pagerState = rememberPagerState(initialPage = loopStart) { LOOP_PAGES }
    var syncingPager by remember { mutableStateOf(false) }

    // Troca de aba: volta para a primeira visualizacao (Lista).
    LaunchedEffect(selectedTab) {
        viewByTab.intValue = 0
        syncingPager = true
        pagerState.scrollToPage(loopStart)
        syncingPager = false
    }

    LaunchedEffect(pagerState.currentPage) {
        if (syncingPager) return@LaunchedEffect
        viewByTab.intValue = ((pagerState.currentPage % VIEW_COUNT) + VIEW_COUNT) % VIEW_COUNT
    }

    var selectedCategoryIndex by remember(selectedTab) { mutableStateOf<Int?>(null) }
    var selectedCardIndex by remember(selectedTab) { mutableStateOf<Int?>(null) }

    // Filtro do relatorio "Por cartao": null = todos os cartoes, senao um cartao especifico
    // (nesse caso o conteudo passa a ser o gasto POR CATEGORIA daquele cartao).
    var cardFilter by remember { mutableStateOf<Long?>(null) }
    var selectedCardCategoryIndex by remember { mutableStateOf<Int?>(null) }
    val cardCategoryExpenses = transactionViewModel.expensesByCategoryForCard
        .observeAsState(initial = emptyList())

    LaunchedEffect(cardFilter) {
        selectedCardCategoryIndex = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                        }
                    }
                },
                title = { Text("Relatórios", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Seletor de mes do relatorio (mesma selecao usada na tela inicial).
            val reportMonth = transactionViewModel.selectedMonth
                .observeAsState(initial = System.currentTimeMillis())
            val monthLabel = remember(reportMonth.value) {
                SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
                    .format(Date(reportMonth.value))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { transactionViewModel.shiftSelectedMonth(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
                }
                Text(
                    text = monthLabel.replaceFirstChar { it.uppercase() },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { transactionViewModel.shiftSelectedMonth(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Proximo mes")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(text = "Por categoria", selected = selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.weight(1f))
                TabButton(text = "Por cartão", selected = selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f))
                TabButton(text = "Resumo", selected = selectedTab == 2, onClick = { selectedTab = 2 }, modifier = Modifier.weight(1f))
                TabButton(text = "Histórico", selected = selectedTab == 3, onClick = { selectedTab = 3 }, modifier = Modifier.weight(1f))
            }

            // Seletor de cartao (aba "Por cartao"): Todos ou um cartao especifico.
            if (selectedTab == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = cardFilter == null,
                        onClick = {
                            cardFilter = null
                            transactionViewModel.setCardFilter(null)
                        },
                        label = { Text("Todos os cartões") }
                    )
                    cards.value.forEach { card ->
                        FilterChip(
                            selected = cardFilter == card.id,
                            onClick = {
                                cardFilter = card.id
                                transactionViewModel.setCardFilter(card.id)
                            },
                            label = { Text(card.name) }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Swipe infinito: o indice real da visualizacao e `page % VIEW_COUNT`.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                key = { it }
            ) { page ->
                val view = ((page % VIEW_COUNT) + VIEW_COUNT) % VIEW_COUNT
                when (selectedTab) {
                    0 -> CategoryTabContent(
                        view = view,
                        items = expensesByCategory.value,
                        categoryMap = categoryMap,
                        totalExpenses = totalExpenses.value,
                        selectedIndex = selectedCategoryIndex,
                        onSelect = { selectedCategoryIndex = it }
                    )
                    1 -> if (cardFilter == null) {
                        CardTabContent(
                            view = view,
                            items = expensesByCard.value,
                            cardMap = cardMap,
                            totalExpenses = totalExpenses.value,
                            selectedIndex = selectedCardIndex,
                            onSelect = { selectedCardIndex = it }
                        )
                    } else {
                        // Cartao especifico: mostra o gasto por categoria daquele cartao.
                        val items = cardCategoryExpenses.value
                        val sum = items.sumOf { it.total }
                        val map = categoryMap
                        CategoryTabContent(
                            view = view,
                            items = items,
                            categoryMap = map,
                            totalExpenses = sum,
                            selectedIndex = selectedCardCategoryIndex,
                            onSelect = { selectedCardCategoryIndex = it }
                        )
                    }
                    2 -> SummaryTabContent(
                        view = view,
                        totalExpenses = totalExpenses.value,
                        totalIncome = totalIncome.value,
                        balance = balance.value
                    )
                    else -> HistoryTabContent(
                        view = view,
                        balances = monthlyBalances.value
                    )
                }
            }

            PageIndicator(
                selected = viewByTab.intValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 4.dp)
            )
        }
    }
}

// ---------------------------------------------------------------- abas

@Composable
private fun CategoryTabContent(
    view: Int,
    items: List<CategoryExpense>,
    categoryMap: Map<Long, Category>,
    totalExpenses: Double,
    selectedIndex: Int?,
    onSelect: (Int?) -> Unit
) {
    val entries = withDistinctColors(items.map { item ->
        val cat = categoryMap[item.categoryId]
        ChartEntry(
            label = cat?.name ?: "Categoria ${item.categoryId}",
            value = item.total,
            color = Color((cat?.color ?: 0xFF6750A4).toInt())
        )
    })
    when (view) {
        1 -> ChartTab(entries = entries, selectedIndex = selectedIndex, onSelect = onSelect)
        2 -> ChartTab(entries = entries, selectedIndex = selectedIndex, onSelect = onSelect, forcePie = true)
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item: CategoryExpense ->
                CategoryExpenseItem(
                    item = item,
                    category = categoryMap[item.categoryId],
                    totalExpenses = totalExpenses
                )
            }
        }
    }
}

@Composable
private fun CardTabContent(
    view: Int,
    items: List<CardExpense>,
    cardMap: Map<Long, CreditCard>,
    totalExpenses: Double,
    selectedIndex: Int?,
    onSelect: (Int?) -> Unit
) {
    val entries = withDistinctColors(items.map { item ->
        val card = cardMap[item.creditCardId]
        ChartEntry(
            label = card?.name ?: "Cartão ${item.creditCardId}",
            value = item.total,
            color = Color((card?.color ?: 0xFF2196F3).toInt())
        )
    })
    when (view) {
        1 -> ChartTab(entries = entries, selectedIndex = selectedIndex, onSelect = onSelect)
        2 -> ChartTab(entries = entries, selectedIndex = selectedIndex, onSelect = onSelect, forcePie = true)
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item: CardExpense ->
                CardExpenseItem(
                    item = item,
                    card = cardMap[item.creditCardId],
                    totalExpenses = totalExpenses
                )
            }
        }
    }
}

@Composable
private fun DeficitNotice(
    totalExpenses: Double,
    totalIncome: Double,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Sem receitas neste mês",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "A pizza compara receitas e gastos. Como não houve entrada, " +
                    "ela não teria fatias para mostrar.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gastos: ${money(totalExpenses)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "Saldo: ${money(balance)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SummaryTabContent(
    view: Int,
    totalExpenses: Double,
    totalIncome: Double,
    balance: Double
) {
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error

    when (view) {
        1, 2 -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val entries = listOf(
                ChartEntry("Receitas", totalIncome, incomeColor),
                ChartEntry("Gastos", totalExpenses, expenseColor)
            ).filter { it.value > 0.0 }
            if (view == 2) {
                // Com saldo negativo (gastou mais do que recebeu) a pizza fica sem
                // leitura útil: mostra o déficit de forma explícita em vez do gráfico.
                if (totalIncome <= 0.0) {
                    DeficitNotice(
                        totalExpenses = totalExpenses,
                        totalIncome = totalIncome,
                        balance = balance
                    )
                } else {
                    PieChart(entries = entries)
                }
            } else {
                HorizontalBarChart(entries = entries)
            }
            SummaryCard(
                title = "Saldo",
                amount = balance,
                color = if (balance >= 0) incomeColor else expenseColor,
                icon = if (balance >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
            )
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryCard(
                    title = "Total de gastos",
                    amount = totalExpenses,
                    color = expenseColor,
                    icon = Icons.Filled.TrendingDown
                )
            }
            item {
                SummaryCard(
                    title = "Total de receitas",
                    amount = totalIncome,
                    color = incomeColor,
                    icon = Icons.Filled.TrendingUp
                )
            }
            item {
                SummaryCard(
                    title = "Saldo",
                    amount = balance,
                    color = if (balance >= 0) incomeColor else expenseColor,
                    icon = if (balance >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
                )
            }
        }
    }
}

@Composable
private fun HistoryTabContent(
    view: Int,
    balances: List<MonthlyBalance>
) {
    when (view) {
        1, 2 -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            VerticalMonthlyBars(
                entries = balances.map { mb ->
                    ChartEntry(
                        label = mb.month,
                        value = mb.balance,
                        color = if (mb.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                positiveColor = MaterialTheme.colorScheme.primary,
                negativeColor = MaterialTheme.colorScheme.error
            )
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(balances) { mb: MonthlyBalance ->
                MonthlyBalanceItem(mb = mb)
            }
        }
    }
}

/** Renderiza o grafico (barras ou pizza) com o cabecalho do item tocado. */
@Composable
private fun ChartTab(
    entries: List<ChartEntry>,
    selectedIndex: Int?,
    onSelect: (Int?) -> Unit,
    forcePie: Boolean = false
) {
    val sorted = entries.filter { it.value > 0.0 }.sortedByDescending { it.value }
    val total = sorted.sumOf { it.value }
    val selected = selectedIndex?.let { sorted.getOrNull(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            // Rolavel: com muitas categorias a legenda crescia alem da tela e
            // o conteudo de baixo (valores/percentuais) ficava cortado.
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selected != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = selected.color.copy(alpha = 0.14f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selected.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${money(selected.value)} · ${percentLabel(selected.value, total)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (sorted.isNotEmpty()) {
            Text(
                text = "Toque no gráfico para ver a categoria",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (sorted.isEmpty()) {
            EmptyChartMessage()
        } else if (forcePie) {
            PieChart(
                entries = sorted,
                selectedIndex = selectedIndex,
                onSliceClick = { onSelect(it) }
            )
        } else {
            HorizontalBarChart(
                entries = sorted,
                selectedIndex = selectedIndex,
                onBarClick = { onSelect(it) }
            )
        }
    }
}

@Composable
private fun PageIndicator(selected: Int, modifier: Modifier = Modifier) {
    val labels = listOf("Lista", "Barras", "Pizza")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(if (index == selected) 8.dp else 6.dp)
                    .background(
                        color = if (index == selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            if (index == selected) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 2.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ---------------------------------------------------------------- itens

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryExpenseItem(
    item: CategoryExpense,
    category: Category?,
    totalExpenses: Double
) {
    val percentage = if (totalExpenses > 0) (item.total / totalExpenses * 100) else 0.0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color((category?.color ?: 0xFF6750A4).toInt()).copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PieChart,
                    contentDescription = "Categoria",
                    tint = Color((category?.color ?: 0xFF6750A4).toInt()),
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)) {
                Text(
                    text = category?.name ?: "Categoria ${item.categoryId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${percentLabel(item.total, totalExpenses)} do total",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = money(item.total),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color((category?.color ?: 0xFF6750A4).toInt())
            )
        }
    }
}

@Composable
fun CardExpenseItem(
    item: CardExpense,
    card: CreditCard?,
    totalExpenses: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color((card?.color ?: 0xFF2196F3).toInt()).copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CreditCard,
                    contentDescription = "Cartão",
                    tint = Color((card?.color ?: 0xFF2196F3).toInt()),
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)) {
                Text(
                    text = card?.name ?: "Cartão ${item.creditCardId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${percentLabel(item.total, totalExpenses)} do total",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = money(item.total),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color((card?.color ?: 0xFF2196F3).toInt())
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = money(amount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun MonthlyBalanceItem(mb: MonthlyBalance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = mb.month,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Receitas: ${money(mb.income)}",
                        fontSize = 12.sp,
                        // Verde do tema (antes era fixo e ficava fora da paleta escolhida).
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Gastos: ${money(mb.expenses)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(
                text = "Saldo: ${money(mb.balance)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (mb.balance >= 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

private fun money(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)
