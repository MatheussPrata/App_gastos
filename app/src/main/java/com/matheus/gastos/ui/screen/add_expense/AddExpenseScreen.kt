package com.matheus.gastos.ui.screen.add_expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val categoriesState = categoryViewModel.categories.observeAsState(initial = emptyList())
    val cardsState = creditCardViewModel.allCards.observeAsState(initial = emptyList())
    val categories = categoriesState.value
    val cards = cardsState.value

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    val date = remember { System.currentTimeMillis() }
    var isExpense by remember { mutableStateOf(true) }
    var isInstallment by remember { mutableStateOf(false) }
    var installments by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    val editing = expenseId != 0L
    val typeOptions = listOf("Gasto", "Entrada")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editing) "Editar transação" else "Nova transação",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                typeOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = if (index == 0) isExpense else !isExpense,
                        onClick = { isExpense = index == 0 },
                        shape = SegmentedButtonDefaults.itemShape(index, typeOptions.size),
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { input -> amount = input.filter { it.isDigit() || it == '.' || it == ',' } },
                label = { Text("Valor (R$)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = selectedCategoryId
                    ?.let { id -> categories.firstOrNull { it.id == id }?.name }
                    ?: "Selecionar",
                onValueChange = {},
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Categoria") },
                readOnly = true
            )

            if (isExpense) {
                OutlinedTextField(
                    value = selectedCardId
                        ?.let { id -> cards.firstOrNull { it.id == id }?.name }
                        ?: "Nenhum",
                    onValueChange = {},
                    label = { Text("Cartão (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Cartão") },
                    readOnly = true
                )
            }

            OutlinedTextField(
                value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date)),
                onValueChange = {},
                label = { Text("Data") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Data") },
                readOnly = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Parcelado", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Checkbox(
                    checked = isInstallment,
                    onCheckedChange = { isInstallment = it }
                )
            }

            if (isInstallment) {
                OutlinedTextField(
                    value = installments,
                    onValueChange = { input -> installments = input.filter { it.isDigit() } },
                    label = { Text("Parcelas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Salvar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            if (editing) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "Excluir transação",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}
