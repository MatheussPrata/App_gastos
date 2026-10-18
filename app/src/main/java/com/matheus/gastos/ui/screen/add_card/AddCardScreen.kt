package com.matheus.gastos.ui.screen.add_card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.viewmodel.CreditCardViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

private val CardColorOptions = listOf(
    0xFF6750A4.toInt(), 0xFF2196F3.toInt(), 0xFF4CAF50.toInt(), 0xFFFF9800.toInt(),
    0xFFF44336.toInt(), 0xFF9C27B0.toInt(), 0xFF009688.toInt(), 0xFFE91E63.toInt(),
    0xFF3F51B5.toInt(), 0xFF00BCD4.toInt(), 0xFF8BC34A.toInt(), 0xFF795548.toInt()
)

/** Exibe o limite no padrão pt-BR ("1234,56"); vazio quando não há limite. */
private fun formatLimitForInput(value: Double): String {
    if (value <= 0.0) return ""
    val format = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault())
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    format.isGroupingUsed = false
    return format.format(value)
}

@Composable
fun AddCardDialog(
    cardId: Long = 0L,
    onDismiss: () -> Unit,
    creditCardViewModel: CreditCardViewModel = hiltViewModel()
) {
    val editing = cardId != 0L
    val scope = rememberCoroutineScope()

    var original by remember { mutableStateOf<CreditCard?>(null) }
    var name by remember { mutableStateOf("") }
    val color = remember { mutableIntStateOf(0xFF6750A4.toInt()) }
    var limitAmount by remember { mutableStateOf("") }
    var closingDay by remember { mutableStateOf("1") }
    var dueDay by remember { mutableStateOf("10") }
    var isActive by remember { mutableStateOf(true) }

    LaunchedEffect(cardId) {
        if (editing) {
            val card = creditCardViewModel.getById(cardId)
            if (card != null) {
                original = card
                name = card.name
                color.intValue = card.color
                limitAmount = formatLimitForInput(card.limitAmount)
                closingDay = card.closingDay.toString()
                dueDay = card.dueDay.toString()
                isActive = card.isActive
            }
        }
    }

    // Em edição, só valida depois que o cartão carregou (evita piscar erro no nome).
    val loaded = !editing || original != null
    val nameError = loaded && name.isBlank()
    val limitError = loaded && !isValidLimitInput(limitAmount)
    val closingError = loaded && !isValidDayInput(closingDay)
    val dueError = loaded && !isValidDayInput(dueDay)
    val canSave = loaded && !limitError && !closingError && !dueError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editing) "Editar cartão" else "Novo cartão",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Informe um nome para o cartão") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default
                )

                OutlinedTextField(
                    value = limitAmount,
                    onValueChange = { input ->
                        limitAmount = input.filter { it.isDigit() || it == '.' || it == ',' }
                    },
                    label = { Text("Limite (R$)") },
                    placeholder = { Text("0,00") },
                    singleLine = true,
                    isError = limitError,
                    supportingText = {
                        Text(
                            if (limitError) "Limite inválido"
                            else "Opcional — deixe em branco se não houver limite"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = closingDay,
                        onValueChange = { input ->
                            closingDay = input.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("Fechamento") },
                        suffix = { Text("dia") },
                        singleLine = true,
                        isError = closingError,
                        supportingText = if (closingError) {
                            { Text("1 a 31") }
                        } else null,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    OutlinedTextField(
                        value = dueDay,
                        onValueChange = { input ->
                            dueDay = input.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("Vencimento") },
                        suffix = { Text("dia") },
                        singleLine = true,
                        isError = dueError,
                        supportingText = if (dueError) {
                            { Text("1 a 31") }
                        } else null,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cor",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CardColorOptions.take(6).forEach { option ->
                            ColorDot(
                                color = option,
                                selected = option == color.intValue,
                                onClick = { color.intValue = option }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CardColorOptions.drop(6).forEach { option ->
                            ColorDot(
                                color = option,
                                selected = option == color.intValue,
                                onClick = { color.intValue = option }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ativo", fontSize = 16.sp)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val limit = parseLimit(limitAmount)
                    val closing = coerceDay(closingDay, defaultValue = 1)
                    val due = coerceDay(dueDay, defaultValue = 10)
                    scope.launch {
                    // Nunca atualizar com id 0: se o load falhou, insere como novo.
                    val base = original ?: if (editing) {
                        creditCardViewModel.getById(cardId)
                    } else null
                    val card = if (base != null) {
                        base.copy(
                            name = name.trim(),
                            color = color.intValue,
                            limitAmount = limit,
                            closingDay = closing,
                            dueDay = due,
                            isActive = isActive,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else {
                        CreditCard(
                            id = 0,
                            name = name.trim(),
                            color = color.intValue,
                            limitAmount = limit,
                            closingDay = closing,
                            dueDay = due,
                            isActive = isActive
                        )
                    }
                    if (base != null) creditCardViewModel.update(card)
                    else creditCardViewModel.insert(card)
                    onDismiss()
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ColorDot(
    color: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (selected) 40.dp else 34.dp)
            .background(Color(color), CircleShape)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selecionado",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

