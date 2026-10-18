package com.matheus.gastos.ui.screen.budget

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.BudgetPeriod
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.ui.components.SelectorField
import com.matheus.gastos.viewmodel.BudgetLimitViewModel
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Percentual de uso do limite, limitado a 0..100.
 * Divisão por zero (ou valores inválidos) retorna 0.
 */
fun usagePercent(spent: Double, limit: Double): Int {
    if (limit <= 0.0) return 0
    if (spent <= 0.0) return 0
    val pct = kotlin.math.ceil(spent / limit * 100.0).toInt()
    return pct.coerceIn(0, 100)
}

/** Rótulo em português do período do limite. */
fun budgetPeriodLabel(period: BudgetPeriod): String = when (period) {
    BudgetPeriod.DAILY -> "Diário"
    BudgetPeriod.WEEKLY -> "Semanal"
    BudgetPeriod.MONTHLY -> "Mensal"
    BudgetPeriod.YEARLY -> "Anual"
}

/** Início do período corrente, usado para calcular startDate/endDate ao salvar. */
internal fun budgetPeriodStart(period: BudgetPeriod, now: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    when (period) {
        BudgetPeriod.DAILY -> Unit
        BudgetPeriod.WEEKLY -> cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        BudgetPeriod.MONTHLY -> cal.set(Calendar.DAY_OF_MONTH, 1)
        BudgetPeriod.YEARLY -> cal.set(Calendar.DAY_OF_YEAR, 1)
    }
    return cal.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetLimitsScreen(
    onBack: () -> Unit,
    budgetLimitViewModel: BudgetLimitViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val limitsState = budgetLimitViewModel.activeLimits.observeAsState(initial = emptyList())
    val categoriesState = categoryViewModel.categories.observeAsState(initial = emptyList())
    val limits = limitsState.value ?: emptyList()
    val categories = categoriesState.value ?: emptyList()
    val expenseCategories = remember(categories) { categories.filter { it.isExpenseDefault } }

    val coroutineScope = rememberCoroutineScope()
    val money = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    var showEditor by remember { mutableStateOf(false) }
    var editingLimit by remember { mutableStateOf<BudgetLimit?>(null) }
    var deletingLimit by remember { mutableStateOf<BudgetLimit?>(null) }

    fun categoryName(id: Long): String =
        categories.firstOrNull { it.id == id }?.name ?: "Categoria $id"

    fun categoryColor(id: Long): Color =
        Color(categories.firstOrNull { it.id == id }?.color ?: 0xFF6750A4.toInt())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Limites de Gasto", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
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
                onClick = {
                    editingLimit = null
                    showEditor = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar limite")
            }
        }
    ) { padding ->
        if (limits.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(limits, key = { it.budgetLimit.id }) { item ->
                    val limit = item.budgetLimit
                    BudgetLimitCard(
                        title = item.categoryName ?: categoryName(limit.categoryId),
                        color = Color(item.categoryColor),
                        limitLabel = money.format(limit.limitAmount),
                        periodLabel = budgetPeriodLabel(limit.period),
                        limitValue = limit.limitAmount,
                        // Gasto real da categoria no período (antes vinha sempre 0).
                        spent = item.spentAmount,
                        onEdit = {
                            editingLimit = limit
                            showEditor = true
                        },
                        onDelete = { deletingLimit = limit }
                    )
                }
            }
        }
    }

    if (showEditor) {
        BudgetLimitEditorDialog(
            limit = editingLimit,
            categories = expenseCategories,
            onDismiss = {
                showEditor = false
                editingLimit = null
            },
            onSave = { saved ->
                coroutineScope.launch {
                    if (saved.id == 0L) budgetLimitViewModel.insert(saved)
                    else budgetLimitViewModel.update(saved)
                }
                showEditor = false
                editingLimit = null
            }
        )
    }

    deletingLimit?.let { limit ->
        AlertDialog(
            onDismissRequest = { deletingLimit = null },
            title = { Text("Excluir limite") },
            text = {
                Text("Excluir o limite de ${categoryName(limit.categoryId)}? O histórico de gastos não será afetado.")
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch { budgetLimitViewModel.delete(limit) }
                    deletingLimit = null
                }) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingLimit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Speed,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum limite definido",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Toque em + para definir um limite por categoria e período.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BudgetLimitCard(
    title: String,
    color: Color,
    limitLabel: String,
    periodLabel: String,
    limitValue: Double,
    spent: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Limite $periodLabel de $limitLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar limite")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir limite")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            val percent = usagePercent(spent, limitValue)
            Text(
                text = "Gasto: ${formatMoney(spent)} ($percent%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = if (percent >= 100) MaterialTheme.colorScheme.error
                else if (percent >= 90) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatMoney(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)

@Composable
private fun BudgetLimitEditorDialog(
    limit: BudgetLimit?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (BudgetLimit) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(limit?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }
    var amountText by remember {
        mutableStateOf(limit?.let { String.format(Locale.US, "%.2f", it.limitAmount).replace('.', ',') } ?: "")
    }
    var period by remember { mutableStateOf(limit?.period ?: BudgetPeriod.MONTHLY) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val parsedAmount = TransactionViewModel.parseAmount(amountText)
    val canSave = selectedCategoryId != 0L && parsedAmount != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (limit == null) "Novo limite" else "Editar limite") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SelectorField(
                    value = selectedCategory?.name ?: "",
                    label = "Categoria",
                    onClick = { showCategoryPicker = true }
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor do limite (R$)") },
                    singleLine = true,
                    isError = amountText.isNotBlank() && parsedAmount == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                SelectorField(
                    value = budgetPeriodLabel(period),
                    label = "Período",
                    onClick = { showPeriodPicker = true }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val amount = parsedAmount ?: return@TextButton
                    val now = System.currentTimeMillis()
                    val base = limit
                    val saved = BudgetLimit(
                        id = base?.id ?: 0L,
                        categoryId = selectedCategoryId,
                        period = period,
                        limitAmount = amount,
                        spentAmount = base?.spentAmount ?: 0.0,
                        isActive = base?.isActive ?: true,
                        startDate = if (base != null && base.period == period) base.startDate
                        else budgetPeriodStart(period, now),
                        endDate = base?.endDate,
                        createdAt = base?.createdAt ?: now,
                        updatedAt = now
                    )
                    onSave(saved)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Categoria") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (categories.isEmpty()) {
                        Text("Nenhuma categoria de gasto encontrada.")
                    }
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(category.color), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = category.id == selectedCategoryId,
                                onClick = {
                                    selectedCategoryId = category.id
                                    showCategoryPicker = false
                                }
                            )
                            Text(category.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) { Text("Fechar") }
            }
        )
    }

    if (showPeriodPicker) {
        AlertDialog(
            onDismissRequest = { showPeriodPicker = false },
            title = { Text("Período") },
            text = {
                Column {
                    BudgetPeriod.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = option == period,
                                onClick = {
                                    period = option
                                    showPeriodPicker = false
                                }
                            )
                            Text(budgetPeriodLabel(option))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPeriodPicker = false }) { Text("Fechar") }
            }
        )
    }
}
