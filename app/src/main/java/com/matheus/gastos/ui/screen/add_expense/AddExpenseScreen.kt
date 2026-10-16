package com.matheus.gastos.ui.screen.add_expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matheus.gastos.R
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseId: Long = 0L,
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val categories = categoryViewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList()).value
    val cards = creditCardViewModel.allCards.collectAsStateWithLifecycle(initialValue = emptyList()).value
    val expenseCategories = categories.filter { it.isDefault || it.customCategories.contains(it) }.filter { it.isExpenseDefault }
    
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var isExpense by remember { mutableStateOf(true) }
    var isInstallment by remember { mutableStateOf(false) }
    var installments by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }

    val editing = expenseId != 0L

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (editing) "Editar Gasto" else "Novo Gasto") },
            navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") } },
            actions = {
                Button(onClick = {
                    // Save logic here
                    onSave()
                }) {
                    Text("Salvar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Category dropdown
                OutlinedTextField(
                    value = selectedCategoryId?.let { expenseCategories.find { it.id == it }?.name } ?: "Categoria",
                    onValueChange = {},
                    label = { Text("Categoria") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDialog = true },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Categoria") },
                    readOnly = true
                )

                // Card dropdown (only for expenses)
                if (isExpense) {
                    OutlinedTextField(
                        value = selectedCardId?.let { cards.find { it.id == it }?.name } ?: "Cartão (opcional)",
                        onValueChange = {},
                        label = { Text("Cartão") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCardDialog = true },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Cartão") },
                        readOnly = true
                    )
                }

                // Date picker
                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date)),
                    onValueChange = {},
                    label = { Text("Data") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Data") },
                    readOnly = true
                )

                // Expense/Income toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tipo", fontSize = 16.sp)
                    androidx.compose.material3.SegmentedButton(
                        selected = if (isExpense) "expense" else "income",
                        onClick = { label -> isExpense = label == "expense" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Segment("expense") { Text("Despesa") }
                        androidx.compose.material3.Segment("income") { Text("Receita") }
                    }
                }

                // Installments
                Checkbox(
                    checked = isInstallment,
                    onCheckedChange = { isInstallment = it },
                    modifier = Modifier.fillMaxWidth().wrapContentSize()
                )
                if (isInstallment) {
                    OutlinedTextField(
                        value = installments.toString(),
                        onValueChange = { installments = it.toIntOrNull() ?: 1 },
                        label = { Text("Parcelas") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    minLines = 3
                )

                if (editing) {
                    Button(
                        onClick = { /* Delete action */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Excluir")
                    }
                }
            }
        }
    }
}
