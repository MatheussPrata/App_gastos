package com.matheus.gastos.ui.screen.add_card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matheus.gastos.R
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.CreditCardViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    cardId: Long = 0L,
    creditCardViewModel: CreditCardViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val cards = creditCardViewModel.allCards.collectAsStateWithLifecycle(initialValue = emptyList()).value
    
    var name by remember { mutableStateOf("") }
    var lastFourDigits by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("VISA") }
    var color by remember { mutableStateOf(0xFF2196F3) }
    var limitAmount by remember { mutableStateOf("") }
    var closingDay by remember { mutableStateOf(1) }
    var dueDay by remember { mutableStateOf(10) }
    var isActive by remember { mutableStateOf(true) }
    var showBrandDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    val editing = cardId != 0L

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (editing) "Editar Cartão" else "Novo Cartão") },
            navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            actions = {
                Button(onClick = { onSave() }) {
                    Text("Salvar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do cartão") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                OutlinedTextField(
                    value = lastFourDigits,
                    onValueChange = { lastFourDigits = it },
                    label = { Text("Últimos 4 dígitos") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    maxLines = 1
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = {},
                    label = { Text("Bandeira") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBrandDialog = true },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Bandeira") },
                    readOnly = true
                )

                OutlinedTextField(
                    value = color.toString(),
                    onValueChange = {},
                    label = { Text("Cor") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showColorDialog = true },
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(color), RoundedCornerShape(12.dp))
                        )
                    },
                    readOnly = true
                )

                OutlinedTextField(
                    value = limitAmount,
                    onValueChange = { limitAmount = it },
                    label = { Text("Limite") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = closingDay.toString(),
                    onValueChange = { closingDay = it.toIntOrNull() ?: 1 },
                    label = { Text("Dia do fechamento") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dueDay.toString(),
                    onValueChange = { dueDay = it.toIntOrNull() ?: 10 },
                    label = { Text("Dia do vencimento") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ativo", fontSize = 16.sp)
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

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