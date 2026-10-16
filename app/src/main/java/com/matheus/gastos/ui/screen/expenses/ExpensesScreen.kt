package com.matheus.gastos.ui.screen.expenses

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit
) {
    val transactionsState = viewModel.currentMonthTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val categoriesState = categoryViewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val cardsState = creditCardViewModel.allCards.collectAsStateWithLifecycle(initialValue = emptyList())

    val transactions = transactionsState.value
    val categories = categoriesState.value
    val cards = cardsState.value

    val categoryMap = categories.associateBy { it.id }
    val cardMap = cards.associateBy { it.id }

    var showDatePicker by remember { mutableStateOf(false) }
    val expandedItems = remember { mutableStateOf<Set<Long>>(emptySet()) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Gastos") },
            actions = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Selecionar mês")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { transaction ->
                val category = transaction.categoryId?.let { categoryMap[it] }
                val card = transaction.creditCardId?.let { cardMap[it] }

                val cardContainerColor = if (transaction.isExpense)
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)

                val amountColor = if (transaction.isExpense)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary

                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(transaction.date))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerColor)
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
                                    fontWeight = FontWeight.Medium
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
                                    text = "${if (transaction.isExpense) "-" else "+"} ${NumberFormat.getCurrencyInstance(Locale.getDefault()).format(transaction.amount)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor
                                )
                                Text(
                                    text = formattedDate,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { onEditExpense(transaction.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            }
        }
    }
}
