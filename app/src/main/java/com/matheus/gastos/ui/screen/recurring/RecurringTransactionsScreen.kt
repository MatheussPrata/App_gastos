package com.matheus.gastos.ui.screen.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.matheus.gastos.R
import com.matheus.gastos.data.entity.RecurringTransaction
import com.matheus.gastos.data.entity.RecurringFrequency
import com.matheus.gastos.viewmodel.RecurringTransactionViewModel
import com.matheus.gastos.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecurringTransactionsScreen(
    recurringTransactionViewModel: RecurringTransactionViewModel,
    categoryViewModel: CategoryViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by recurringTransactionViewModel.recurringTransactions.collectAsStateWithLifecycle()
    val categories = categoryViewModel.categories.value ?: emptyList()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction: RecurringTransaction? by remember { mutableStateOf(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete: RecurringTransaction? by remember { mutableStateOf(null) }

    // Form state
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
    var dayOfMonth by remember { mutableStateOf(1) }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var isActive by remember { mutableStateOf(true) }

    val categoryMap = categories.associateBy({ it.id }, { it })

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Transações Recorrentes") },
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = { onNavigateBack() }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            },
            actions = {
                androidx.compose.material3.IconButton(onClick = {
                    editingTransaction = null
                    resetForm()
                    showAddDialog = true
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.Add,
                        contentDescription = "Adicionar"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        if (transactions.isEmpty()) {
            EmptyRecurringState(onAddClick = {
                editingTransaction = null
                resetForm()
                showAddDialog = true
            })
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, 0.dp, 0.dp, 100.dp)
            ) {
                items(transactions) { transaction ->
                    RecurringTransactionItem(
                        transaction = transaction,
                        category = categoryMap[transaction.categoryId],
                        onEdit = {
                            editingTransaction = transaction
                            populateForm(transaction)
                            showAddDialog = true
                        },
                        onDelete = {
                            transactionToDelete = transaction
                            showDeleteDialog = true
                        },
                        onToggleActive = {
                            recurringTransactionViewModel.toggleActive(transaction.id)
                        }
                    )
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog) {
        RecurringTransactionDialog(
            isEditing = editingTransaction != null,
            transaction = editingTransaction,
            categories = categories,
            description = description,
            onDescriptionChange = { description = it },
            amount = amount,
            onAmountChange = { amount = it },
            type = type,
            onTypeChange = { type = it },
            categoryId = categoryId,
            onCategoryIdChange = { categoryId = it },
            frequency = frequency,
            onFrequencyChange = { frequency = it },
            dayOfMonth = dayOfMonth,
            onDayOfMonthChange = { dayOfMonth = it },
            startDate = startDate,
            onStartDateChange = { startDate = it },
            endDate = endDate,
            onEndDateChange = { endDate = it },
            isActive = isActive,
            onIsActiveChange = { isActive = it },
            onSave = {
                if (validateForm()) {
                    saveTransaction()
                    showAddDialog = false
                    editingTransaction = null
                    resetForm()
                }
            },
            onCancel = {
                showAddDialog = false
                editingTransaction = null
                resetForm()
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false; transactionToDelete = null },
            title = { Text("Excluir transação recorrente?") },
            text = {
                transactionToDelete?.let {
                    Text("Tem certeza que deseja excluir \"${it.description}\"?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { recurringTransactionViewModel.delete(it.id) }
                        showDeleteDialog = false
                        transactionToDelete = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false; transactionToDelete = null },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun EmptyRecurringState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.filled.Icons.Default.Repeat,
                contentDescription = "Sem recorrentes",
                modifier = Modifier.size(72.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "Nenhuma transação recorrente",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Toque no botão + para adicionar uma.\nEx: Salário todo dia 5, aluguel todo dia 1.",
                textAlign = androidx.compose.ui.text.TextAlign.Center,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.FilledButton(
                onClick = onAddClick
            ) {
                Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.Add, contentDescription = "Adicionar")
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Recorrente")
            }
        }
    }
}

@Composable
fun RecurringTransactionItem(
    transaction: RecurringTransaction,
    category: com.matheus.gastos.data.entity.Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    val color = if (transaction.type == "income") Color.Green else Color.Red
    val sign = if (transaction.type == "income") "+" else "-"
    val nextDate = formatDate(transaction.nextDueDate)
    val freqText = formatFrequency(transaction.frequency, transaction.dayOfMonth)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        androidx.compose.material3.ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = transaction.description,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$sign R$ ${String.format("%.2f", transaction.amount).replace(".", ",")}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${category?.name ?: "Sem categoria"} • $freqText • Próxima: $nextDate",
                        fontSize = 12.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                }
            },
            leadingContent = {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.type == "income")
                            androidx.compose.material.icons.filled.Icons.Default.ArrowUpward
                        else
                            androidx.compose.material.icons.filled.Icons.Default.ArrowDownward,
                        contentDescription = if (transaction.type == "income") "Receita" else "Despesa",
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.material3.Switch(
                        checked = transaction.isActive,
                        onCheckedChange = { _ -> onToggleActive() },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = color,
                            checkedTrackColor = color.copy(alpha = 0.3f)
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = androidx.compose.material.icons.filled.Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun RecurringTransactionDialog(
    isEditing: Boolean,
    transaction: RecurringTransaction?,
    categories: List<com.matheus.gastos.data.entity.Category>,
    description: String,
    onDescriptionChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    categoryId: Long?,
    onCategoryIdChange: (Long?) -> Unit,
    frequency: RecurringFrequency,
    onFrequencyChange: (RecurringFrequency) -> Unit,
    dayOfMonth: Int,
    onDayOfMonthChange: (Int) -> Unit,
    startDate: Long,
    onStartDateChange: (Long) -> Unit,
    endDate: Long?,
    onEndDateChange: (Long?) -> Unit,
    isActive: Boolean,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (isEditing) "Editar Recorrente" else "Nova Transação Recorrente") },
        text = {
            Column(
                modifier = Modifier.width(320.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type Selector
                androidx.compose.material3.SegmentedButton(
                    selected = { type },
                    onSelectionChange = { newSelection ->
                        onTypeChange(newSelection.first())
                    }
                ) {
                    androidx.compose.material3.ButtonSegment(
                        onClick = { onTypeChange("expense") },
                        label = { Row(verticalArrangement = Arrangement.Center) {
                            Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.ArrowDownward, contentDescription = "")
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.Modifier.width(8.dp))
                            Text("Despesa")
                        }},
                        selected = type == "expense"
                    )
                    androidx.compose.material3.ButtonSegment(
                        onClick = { onTypeChange("income") },
                        label = { Row(verticalArrangement = Arrangement.Center) {
                            Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.ArrowUpward, contentDescription = "")
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.Modifier.width(8.dp))
                            Text("Receita")
                        }},
                        selected = type == "income"
                    )
                }

                // Description
                androidx.compose.material3.TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
                    )
                )

                // Amount
                androidx.compose.material3.TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("Valor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    visualTransformation = androidx.compose.material3.NumberVisualTransformation(),
                    keyboardActions = androidx.compose.ui.text.input.KeyboardActions(onNext = { /* focus next */ })
                )

                // Category
                androidx.compose.material3.ExposedDropdownMenuBox(
                    expanded = false, // simplified
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.TextField(
                        value = categories.find { it.id == categoryId }?.name ?: "",
                        onValueChange = {},
                        label = { Text("Categoria") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.ExpandMore, contentDescription = "") }
                    )
                    // Dropdown menu would go here - simplified for now
                }

                // Frequency
                androidx.compose.material3.ExposedDropdownMenuBox(
                    expanded = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.TextField(
                        value = formatFrequency(frequency, 1),
                        onValueChange = {},
                        label = { Text("Frequência") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.ExpandMore, contentDescription = "") }
                    )
                }

                // Day of Month
                androidx.compose.material3.TextField(
                    value = dayOfMonth.toString(),
                    onValueChange = { v -> onDayOfMonthChange(v.toIntOrNull() ?: 1) },
                    label = { Text("Dia do mês/semana") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )

                // Start Date
                androidx.compose.material3.TextField(
                    value = formatDate(startDate),
                    onValueChange = {},
                    label = { Text("Data de início") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.CalendarMonth, contentDescription = "") }
                )

                // End Date (optional)
                androidx.compose.material3.TextField(
                    value = endDate?.let { formatDate(it) } ?: "Sem data de fim",
                    onValueChange = {},
                    label = { Text("Data de fim (opcional)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { 
                        Icon(imageVector = if (endDate != null) androidx.compose.material.icons.filled.Icons.Default.Clear else androidx.compose.material.icons.filled.Icons.Default.CalendarMonth, contentDescription = "")
                    }
                )

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ativo")
                    androidx.compose.material3.Switch(
                        checked = true,
                        onCheckedChange = { /* onIsActiveChange */ }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { /* onSave */ },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isEditing) "Salvar" else "Adicionar") }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}

private fun resetForm() {
    // Reset form fields
}

private fun populateForm(transaction: RecurringTransaction) {
    // Populate form with transaction data
}

private fun validateForm(): Boolean = true

private fun saveTransaction() {
    // Save logic
}

private fun formatFrequency(freq: RecurringFrequency, day: Int): String = when (freq) {
    RecurringFrequency.MONTHLY -> "Mensal (dia $day)"
    RecurringFrequency.WEEKLY -> "Semanal (dia $day)"
    RecurringFrequency.DAILY -> "Diário"
    RecurringFrequency.YEARLY -> "Anual"
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
}