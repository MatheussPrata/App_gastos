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

/**
 * Popup reutilizável para adicionar/editar um "cartão".
 * O cartão aqui serve apenas para ORGANIZAR as contas: por isso a UI expõe
 * somente nome, cor, limite e ativo/inativo (sem bandeira/número).
 */
@Composable
fun AddCardDialog(
    cardId: Long = 0L,
    onDismiss: () -> Unit,
    creditCardViewModel: CreditCardViewModel = hiltViewModel()
) {
    val editing = cardId != 0L
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    val color = remember { mutableIntStateOf(0xFF6750A4.toInt()) }
    var limitAmount by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    LaunchedEffect(cardId) {
        if (editing) {
            val card = creditCardViewModel.getById(cardId)
            if (card != null) {
                name = card.name
                color.intValue = card.color
                limitAmount = if (card.limitAmount > 0.0) card.limitAmount.toString() else ""
                isActive = card.isActive
            }
        }
    }

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
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default
                )

                OutlinedTextField(
                    value = limitAmount,
                    onValueChange = { input ->
                        limitAmount = input.filter { it.isDigit() || it == '.' || it == ',' }
                    },
                    label = { Text("Limite (R$, opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default
                )

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
                enabled = name.isNotBlank(),
                onClick = {
                    val limit = limitAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val card = CreditCard(
                        id = if (editing) cardId else 0,
                        name = name.trim(),
                        color = color.intValue,
                        limitAmount = limit,
                        isActive = isActive
                    )
                    scope.launch {
                        if (editing) creditCardViewModel.update(card)
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

