package com.matheus.gastos.ui.screen.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel
) {
    val expensesByCategory = transactionViewModel.expensesByCategory.observeAsState(initial = emptyList())
    val expensesByCard = transactionViewModel.expensesByCard.observeAsState(initial = emptyList())
    val totalExpenses = transactionViewModel.totalExpenses.observeAsState(initial = 0.0)
    val totalIncome = transactionViewModel.totalIncome.observeAsState(initial = 0.0)
    val balance = transactionViewModel.balance.observeAsState(initial = 0.0)

    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = categoryViewModel.categories.observeAsState(initial = emptyList())
    val cards = creditCardViewModel.allCards.observeAsState(initial = emptyList())

    val categoryMap = categories.value.associateBy { it.id }
    val cardMap = cards.value.associateBy { it.id }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(text = "Por categoria", selected = selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.weight(1f))
                TabButton(text = "Por cartão", selected = selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f))
                TabButton(text = "Resumo", selected = selectedTab == 2, onClick = { selectedTab = 2 }, modifier = Modifier.weight(1f))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        items(expensesByCategory.value) { item: TransactionDao.CategoryExpense ->
                            CategoryExpenseItem(
                                item = item,
                                category = categoryMap[item.categoryId],
                                totalExpenses = totalExpenses.value
                            )
                        }
                    }
                    1 -> {
                        items(expensesByCard.value) { item: TransactionDao.CardExpense ->
                            CardExpenseItem(
                                item = item,
                                card = cardMap[item.creditCardId],
                                totalExpenses = totalExpenses.value
                            )
                        }
                    }
                    else -> {
                        item {
                            SummaryCard(
                                title = "Total de gastos",
                                amount = totalExpenses.value,
                                color = MaterialTheme.colorScheme.error,
                                icon = Icons.Filled.TrendingDown
                            )
                        }
                        item {
                            SummaryCard(
                                title = "Total de receitas",
                                amount = totalIncome.value,
                                color = Color(0xFF4CAF50.toInt()),
                                icon = Icons.Filled.TrendingUp
                            )
                        }
                        item {
                            SummaryCard(
                                title = "Saldo",
                                amount = balance.value,
                                color = if (balance.value >= 0) Color(0xFF4CAF50.toInt()) else MaterialTheme.colorScheme.error,
                                icon = if (balance.value >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
                            )
                        }
                    }
                }
            }
        }
    }
}

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
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryExpenseItem(
    item: TransactionDao.CategoryExpense,
    category: Category?,
    totalExpenses: Double
) {
    val percentage = if (totalExpenses > 0) (item.total / totalExpenses * 100) else 0f
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
                    text = "%.1f%%".format(percentage),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.total),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color((category?.color ?: 0xFF6750A4).toInt())
            )
        }
    }
}

@Composable
fun CardExpenseItem(
    item: TransactionDao.CardExpense,
    card: CreditCard?,
    totalExpenses: Double
) {
    val percentage = if (totalExpenses > 0) (item.total / totalExpenses * 100) else 0f
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
                    text = "%.1f%%".format(percentage),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.total),
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
                text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
