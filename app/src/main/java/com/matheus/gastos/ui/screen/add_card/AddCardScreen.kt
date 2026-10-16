package com.matheus.gastos.ui.screen.add_card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    cardId: Long = 0L,
    creditCardViewModel: CreditCardViewModel = hiltViewModel(),
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastFourDigits by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("VISA") }
    val color = remember { mutableIntStateOf(0xFF2196F3.toInt()) }
    var limitAmount by remember { mutableStateOf("") }
    var closingDay by remember { mutableStateOf(1) }
    var dueDay by remember { mutableStateOf(10) }
    var isActive by remember { mutableStateOf(true) }

    val editing = cardId != 0L

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (editing) "Editar Cartão" else "Novo Cartão") },
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
                    singleLine = true
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Bandeira") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Bandeira") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = color.intValue.toString(),
                    onValueChange = {},
                    label = { Text("Cor") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(color.intValue), RoundedCornerShape(12.dp))
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
