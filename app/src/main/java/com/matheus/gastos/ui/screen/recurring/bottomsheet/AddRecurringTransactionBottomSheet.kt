package com.matheus.gastos.ui.screen.recurring.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.RecurringFrequency
import com.matheus.gastos.data.entity.RecurringTransaction
import com.matheus.gastos.viewmodel.RecurringTransactionViewModel
import com.matheus.gastos.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddRecurringTransactionBottomSheet(
    recurringTransactionViewModel: RecurringTransactionViewModel,
    categoryViewModel: CategoryViewModel,
    transaction: RecurringTransaction? = null,
    onSave: (RecurringTransaction) -> Unit,
    onCancel: () -> Unit
) {
    val categories = categoryViewModel.categories.value ?: emptyList()
    val isEditing = transaction != null

    // Form state
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var amount by remember { mutableStateOf(transaction?.amount.toString() ?: "") }
    var type by remember { mutableStateOf(transaction?.type ?: "expense") }
    var categoryId by remember { mutableStateOf<Long?>(transaction?.categoryId) }
    var frequency by remember { mutableStateOf(transaction?.frequency ?: RecurringFrequency.MONTHLY) }
    var dayOfMonth by remember { mutableStateOf(transaction?.dayOfMonth ?: 1) }
    var startDate by remember { mutableStateOf(transaction?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf<Long?>(transaction?.endDate) }
    var isActive by remember { mutableStateOf(transaction?.isActive ?: true) }

    val categories = categoryViewModel.categories.value ?: emptyList()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Type Selector
        Text(
            text = if (isEditing) "Editar Recorrente" else "Nova Transação Recorrente",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        androidx.compose.material3.SegmentedButton(
            selected = { type },
            onSelectionChange = { newSelection ->
                // Update type
            }
        ) {
            androidx.compose.material3.ButtonSegment(
                onClick = { /* onTypeChange("expense") */ },
                label = { Row(verticalArrangement = Arrangement.Center) {
                    Icon(imageVector = androidx.compose.material.icons.filled.Icons.Default.ArrowDownward, contentDescription = "")
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.Modifier.width(8.dp))
                    Text("Despesa")
                }},
                selected = type == "expense"
            )
            androidx.compose.material3.ButtonSegment(
                onClick = { /* onTypeChange("income") */ },
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
            onValueChange = { description = it },
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
            onValueChange = { amount = it },
            label = { Text("Valor") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next
            ),
            visualTransformation = androidx.compose.material3.NumberVisualTransformation()
        )

        // Category
        androidx.compose.material3.ExposedDropdownMenuBox(
            expanded = false,
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
        }

        // Frequency
        androidx.compose.material3.ExposedDropdownMenuBox(
            expanded = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.TextField(
                value = formatFrequency(frequency, dayOfMonth),
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
            onValueChange = { v -> /* onDayOfMonthChange(v.toIntOrNull() ?: 1) */ },
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
                checked = isActive,
                onCheckedChange = { isActive = it }
            )
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { /* onCancel */ },
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }

            FilledButton(
                onClick = { /* onSave */ },
                modifier = Modifier.weight(1f)
            ) { Text(if (transaction != null) "Salvar" else "Adicionar") }
        }
    }
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