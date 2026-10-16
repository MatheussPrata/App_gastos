package com.matheus.gastos.ui.screen.add_expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matheus.gastos.data.entity.PaymentMethod
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var isExpense by remember { mutableStateOf(true) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.DINHEIRO) }
    var isInstallment by remember { mutableStateOf(false) }
    var installments by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialogs de seleção (dropdowns continuam como campos readOnly, mas abrem estes dialogs).
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }
    var showPaymentMethodDialog by remember { mutableStateOf(false) }

    val editing = expenseId != 0L
    val typeOptions = listOf("Gasto", "Entrada")

    // Categorias filtradas pelo tipo (gasto vs entrada).
    val filteredCategories = categories.filter { it.isExpenseDefault == isExpense }

    // Carrega a transação para edição.
    LaunchedEffect(expenseId) {
        if (editing) {
            val existing = transactionViewModel.getById(expenseId)
            if (existing != null) {
                description = existing.description
                amount = formatAmountInput(existing.amount)
                selectedCategoryId = existing.categoryId
                selectedCardId = existing.creditCardId
                date = existing.date
                isExpense = existing.isExpense
                paymentMethod = existing.paymentMethod
                isInstallment = existing.isInstallment && existing.isExpense
                installments = existing.totalInstallments.toString()
                notes = existing.notes.orEmpty()
            }
        }
    }

    // Ao trocar o tipo, garante que os selecionados pertençam ao tipo e
    // que entrada nunca fique com cartão/parcelamento.
    LaunchedEffect(isExpense) {
        if (filteredCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = null
        }
        if (!isExpense) {
            isInstallment = false
            installments = "1"
            selectedCardId = null
            if (paymentMethod == PaymentMethod.CARTAO) {
                paymentMethod = PaymentMethod.DINHEIRO
            }
        }
    }

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
                    ?.let { id -> filteredCategories.firstOrNull { it.id == id }?.name }
                    ?: "Selecionar",
                onValueChange = {},
                label = { Text("Categoria") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDialog = true },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Categoria") },
                readOnly = true
            )

            // Método de pagamento — sempre visível.
            OutlinedTextField(
                value = paymentMethod.label,
                onValueChange = {},
                label = { Text("Método de pagamento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPaymentMethodDialog = true },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Método") },
                readOnly = true
            )

            // Cartão: só para GASTO e quando o método for Cartão.
            if (isExpense && paymentMethod == PaymentMethod.CARTAO) {
                OutlinedTextField(
                    value = selectedCardId
                        ?.let { id -> cards.firstOrNull { it.id == id }?.name }
                        ?: "Nenhum",
                    onValueChange = {},
                    label = { Text("Cartão") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCardDialog = true },
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

            // Parcelamento: SOMENTE para gasto.
            if (isExpense) {
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
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val parsedAmount = TransactionViewModel.parseAmount(amount)
                    when {
                        description.isBlank() -> {
                            errorMessage = "Informe uma descrição."
                        }
                        parsedAmount == null -> {
                            errorMessage = "Informe um valor válido (maior que zero)."
                        }
                        else -> {
                            errorMessage = null
                            scope.launch {
                                saveTransaction(
                                    editing = editing,
                                    expenseId = expenseId,
                                    description = description.trim(),
                                    amount = parsedAmount,
                                    date = date,
                                    isExpense = isExpense,
                                    categoryId = selectedCategoryId,
                                    cardId = if (isExpense && paymentMethod == PaymentMethod.CARTAO) selectedCardId else null,
                                    paymentMethod = paymentMethod,
                                    isInstallment = isExpense && isInstallment,
                                    totalInstallments = installments.toIntOrNull() ?: 1,
                                    notes = notes.trim().ifBlank { null },
                                    transactionViewModel = transactionViewModel
                                )
                                onSave()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Salvar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            if (editing) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                TextButton(
                    onClick = {
                        scope.launch {
                            transactionViewModel.deleteById(expenseId)
                            onSave()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Excluir transação",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showCategoryDialog) {
        SelectionDialog(
            title = "Categoria",
            options = filteredCategories.map { it.id to it.name },
            onDismiss = { showCategoryDialog = false },
            onSelected = { id ->
                selectedCategoryId = id
                showCategoryDialog = false
            }
        )
    }

    if (showCardDialog) {
        SelectionDialog(
            title = "Cartão",
            options = cards.map { it.id to it.name },
            onDismiss = { showCardDialog = false },
            onSelected = { id ->
                selectedCardId = id
                showCardDialog = false
            }
        )
    }

    if (showPaymentMethodDialog) {
        PaymentMethodDialog(
            onDismiss = { showPaymentMethodDialog = false },
            onSelected = { method ->
                paymentMethod = method
                showPaymentMethodDialog = false
            }
        )
    }
}

@Composable
private fun PaymentMethodDialog(
    onDismiss: () -> Unit,
    onSelected: (PaymentMethod) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Método de pagamento", fontWeight = FontWeight.SemiBold) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                PaymentMethod.entries.forEach { method ->
                    Text(
                        text = method.label,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(method) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    )
}

private suspend fun saveTransaction(
    editing: Boolean,
    expenseId: Long,
    description: String,
    amount: Double,
    date: Long,
    isExpense: Boolean,
    categoryId: Long?,
    cardId: Long?,
    paymentMethod: PaymentMethod,
    isInstallment: Boolean,
    totalInstallments: Int,
    notes: String?,
    transactionViewModel: TransactionViewModel
) {
    val transaction = Transaction(
        id = if (editing) expenseId else 0L,
        description = description,
        amount = amount,
        date = date,
        isExpense = isExpense,
        categoryId = categoryId,
        creditCardId = cardId,
        paymentMethod = paymentMethod,
        isInstallment = isInstallment && totalInstallments > 1,
        installmentNumber = 1,
        totalInstallments = if (isInstallment) totalInstallments.coerceAtLeast(1) else 1,
        notes = notes
    )

    if (editing) {
        transactionViewModel.update(transaction)
    } else if (isInstallment && totalInstallments > 1) {
        transactionViewModel.createInstallments(
            baseTransaction = transaction,
            totalInstallments = totalInstallments,
            categoryId = categoryId,
            subcategoryId = null,
            creditCardId = cardId
        )
    } else {
        transactionViewModel.insert(transaction)
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<Pair<Long, String>>,
    onDismiss: () -> Unit,
    onSelected: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (options.isEmpty()) {
                    Text("Nenhuma opção disponível", fontSize = 14.sp)
                } else {
                    options.forEach { (id, label) ->
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelected(id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    )
}

/** Formata um Double para edição (sem separador de milhar, ponto decimal). */
private fun formatAmountInput(value: Double): String {
    return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}
