package com.matheus.gastos.ui.screen.add_expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseId: Long = 0L,
    transactionViewModel: TransactionViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    categoryViewModel: CategoryViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    creditCardViewModel: CreditCardViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val categoriesState = categoryViewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val cardsState = creditCardViewModel.allCards.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories = categoriesState.value
    val cards = cardsState.value

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    val date by remember { mutableStateOf(System.currentTimeMillis()) }
    var isExpense by remember { mutableStateOf(true) }
    var isInstallment by remember { mutableStateOf(false) }
    var installments by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }

    val editing = expenseId != 0L

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (editing) "Editar Gasto" else "Novo Gasto") },
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            },
            actions = {
                Button(onClick = onSave) {
                    Text("Salvar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
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

                OutlinedTextField(
                    value = selectedCategoryId
                        ?.let { id -> categories.firstOrNull { it.id == id }?.name }
                        ?: "Categoria",
                    onValueChange = {},
                    label = { Text("Categoria") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Categoria") },
                    readOnly = true
                )

                if (isExpense) {
                    OutlinedTextField(
                        value = selectedCardId
                            ?.let { id -> cards.firstOrNull { it.id == id }?.name }
                            ?: "Cartão (opcional)",
                        onValueChange = {},
                        label = { Text("Cartão") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Cartão") },
                        readOnly = true
                    )
                }

                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date)),
                    onValueChange = {},
                    label = { Text("Data") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Data") },
                    readOnly = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tipo", fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isExpense,
                            onClick = { isExpense = true },
                            label = { Text("Despesa") }
                        )
                        FilterChip(
                            selected = !isExpense,
                            onClick = { isExpense = false },
                            label = { Text("Receita") }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Parcelado", fontSize = 16.sp)
                    Checkbox(
                        checked = isInstallment,
                        onCheckedChange = { isInstallment = it }
                    )
                }

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
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Excluir")
                    }
                }
            }
        }
    }
}
