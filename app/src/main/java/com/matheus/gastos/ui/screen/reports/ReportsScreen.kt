package com.matheus.gastos.ui.screen.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
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
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matheus.gastos.R
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.TransactionViewModel
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Category
import java.text.NumberFormat
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel
) {
    val expensesByCategory = transactionViewModel.expensesByCategory.asFlow().collectAsStateWithLifecycle(initialValue = emptyList<TransactionDao.CategoryExpense>())
    val expensesByCard = transactionViewModel.expensesByCard.asFlow().collectAsStateWithLifecycle(initialValue = emptyList<TransactionDao.CardExpense>())
    val totalExpenses = transactionViewModel.totalExpenses.asFlow().collectAsStateWithLifecycle(initialValue = 0.0)
    val totalIncome = transactionViewModel.totalIncome.asFlow().collectAsStateWithLifecycle(initialValue = 0.0)
    val balance = transactionViewModel.balance.asFlow().collectAsStateWithLifecycle(initialValue = 0.0)

    var selectedTab = remember { mutableStateOf(0) }
    val categories = categoryViewModel.categories.asFlow().collectAsStateWithLifecycle(initialValue = emptyList<Category>())
    val cards = creditCardViewModel.allCards.asFlow().collectAsStateWithLifecycle(initialValue = emptyList<CreditCard>())

    val categoryMap = categories.value.associateBy { it.id }
    val cardMap = cards.value.associateBy { it.id }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Relatórios") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
            )
        )

        // Simple tab row implementation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(text = "Por Categoria", selected = selectedTab.value == 0, onClick = { selectedTab.value = 0 })
            TabButton(text = "Por Cartão", selected = selectedTab.value == 1, onClick = { selectedTab.value = 1 })
            TabButton(text = "Resumo", selected = selectedTab.value == 2, onClick = { selectedTab.value = 2 })
        }

        // Divider
        androidx.compose.material3.Divider(
            modifier = Modifier.fillMaxWidth(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab.value) {
                0 -> {
                    // Por Categoria
                    items(expensesByCategory.value) { item: TransactionDao.CategoryExpense ->
                        CategoryExpenseItem(
                            item = item,
                            category = categoryMap[item.categoryId],
                            totalExpenses = totalExpenses.value
                        )
                    }
                }
                1 -> {
                    // Por Cartão
                    items(expensesByCard.value) { item: TransactionDao.CardExpense ->
                        CardExpenseItem(
                            item = item,
                            card = cardMap[item.creditCardId],
                            totalExpenses = totalExpenses.value
                        )
                    }
                }
                2 -> {
                    // Resumo
                    item {
                        SummaryCard(
                            title = "Total de Gastos",
                            amount = totalExpenses.value,
                            color = Color(0xFFF44336.toInt()),
                            icon = Icons.Filled.TrendingDown
                        )
                    }
                    item {
                        SummaryCard(
                            title = "Total de Receitas",
                            amount = totalIncome.value,
                            color = Color(0xFF4CAF50.toInt()),
                            icon = Icons.Filled.TrendingUp
                        )
                    }
                    item {
                        SummaryCard(
                            title = "Saldo",
                            amount = balance.value,
                            color = if (balance.value >= 0) Color(0xFF4CAF50.toInt()) else Color(0xFFF44336.toInt()),
                            icon = if (balance.value >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
                        )
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
    onClick: () -> Unit
) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .background(if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    )
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
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        )
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
                    .size(48.dp)
                    .background(Color((category?.color ?: 0xFF6750A4).toInt()).copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PieChart,
                    contentDescription = "Categoria",
                    tint = Color((category?.color ?: 0xFF6750A4).toInt()),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(text = category?.name ?: "Categoria ${item.categoryId}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = "%.1f%%".format(percentage),
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color((category?.color ?: 0xFF6750A4).toInt())
                )
            }
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
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        )
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
                    .size(48.dp)
                    .background(Color((card?.color ?: 0xFF2196F3).toInt()).copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CreditCard,
                    contentDescription = "Cartão",
                    tint = Color((card?.color ?: 0xFF2196F3).toInt()),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(text = card?.name ?: "Cartão ${item.creditCardId}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = "%.1f%%".format(percentage),
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color((card?.color ?: 0xFF2196F3).toInt())
                )
            }
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
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        )
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
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount),
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
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