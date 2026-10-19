package com.matheus.gastos.ui.screen.add_expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.PaymentMethod
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.ui.components.SelectorField
import com.matheus.gastos.ui.components.WalletCardView
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
                // Sobe junto com o teclado: o conteudo continua rolavel ate o fim
                // em vez de ficar escondido atras do teclado.
                .imePadding()
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

            SelectorField(
                value = selectedCategoryId
                    ?.let { id -> filteredCategories.firstOrNull { it.id == id }?.name }
                    ?: "Selecionar",
                label = "Categoria",
                onClick = { showCategoryDialog = true },
                contentDescription = "Categoria"
            )

            // Método de pagamento — sempre visível.
            SelectorField(
                value = paymentMethod.label,
                label = "Método de pagamento",
                onClick = { showPaymentMethodDialog = true },
                contentDescription = "Método"
            )

            // Cartão: só para GASTO e quando o método for Cartão.
            if (isExpense && paymentMethod == PaymentMethod.CARTAO) {
                SelectorField(
                    value = selectedCardId
                        ?.let { id -> cards.firstOrNull { it.id == id }?.name }
                        ?: "Nenhum",
                    label = "Cartão",
                    onClick = { showCardDialog = true },
                    contentDescription = "Cartão"
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
                            // Sem categoria escolhida, usa "Outros" (gasto) ou "Salário" (entrada)
                            // para o lançamento nunca ficar sem categoria e sumir de
                            // relatórios/limites.
                            val resolvedCategoryId = selectedCategoryId
                                ?: fallbackCategoryId(
                                    isExpense = isExpense,
                                    categories = categories
                                )
                            // Compra no cartao apos o fechamento cai na fatura do mes seguinte.
                            // So no lancamento novo: ao editar, a data ja foi ajustada.
                            val effectiveDate = if (
                                !editing && isExpense &&
                                paymentMethod == PaymentMethod.CARTAO && selectedCardId != null
                            ) {
                                val closing = cards.firstOrNull { it.id == selectedCardId }
                                    ?.closingDay ?: 0
                                adjustDateForCardClosing(date, closing)
                            } else {
                                date
                            }
                            scope.launch {
                                saveTransaction(
                                    editing = editing,
                                    expenseId = expenseId,
                                    description = description.trim(),
                                    amount = parsedAmount,
                                    date = effectiveDate,
                                    isExpense = isExpense,
                                    categoryId = resolvedCategoryId,
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
        WalletCardPickerDialog(
            cards = cards,
            selectedCardId = selectedCardId,
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

/**
 * Seletor de cartao no estilo carteira (Google Wallet):
 * cada cartao aparece renderizado como um cartao de verdade,
 * empilhados verticalmente, e o selecionado fica destacado.
 */
@Composable
private fun WalletCardPickerDialog(
    cards: List<CreditCard>,
    selectedCardId: Long?,
    onDismiss: () -> Unit,
    onSelected: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolha o cartão", fontWeight = FontWeight.SemiBold) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        text = {
            if (cards.isEmpty()) {
                Text("Nenhum cartão cadastrado.")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    cards.forEach { card ->
                        WalletCardView(
                            card = card,
                            selected = card.id == selectedCardId,
                            onClick = { onSelected(card.id) }
                        )
                    }
                }
            }
        }
    )
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

/**
 * Primeira categoria de "Outros" do tipo informado, ou null se nao existir.
 * Usada quando o usuario nao escolhe categoria no lancamento.
 */
internal fun fallbackCategoryId(isExpense: Boolean, categories: List<Category>): Long? =
    categories.firstOrNull { it.isExpenseDefault == isExpense && it.name == "Outros" }?.id

/**
 * Compra no cartao depois do dia de fechamento pertence a fatura do mes seguinte.
 *
 * Mantem o dia original e avanca um mes (ex.: fechamento dia 20 + compra dia 25 -> 25 do mes
 * seguinte), para que a compra caia no mes/relatorio correto.
 */
internal fun adjustDateForCardClosing(date: Long, closingDay: Int): Long {
    if (closingDay !in 1..31) return date
    val cal = Calendar.getInstance().apply { timeInMillis = date }
    return if (cal.get(Calendar.DAY_OF_MONTH) > closingDay) {
        cal.add(Calendar.MONTH, 1)
        cal.timeInMillis
    } else {
        date
    }
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
