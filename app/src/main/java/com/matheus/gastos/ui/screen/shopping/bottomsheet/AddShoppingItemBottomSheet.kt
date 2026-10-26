package com.matheus.gastos.ui.screen.shopping.bottomsheet

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
import com.matheus.gastos.data.entity.ShoppingItem
import com.matheus.gastos.viewmodel.ShoppingViewModel
import com.matheus.gastos.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AddShoppingItemBottomSheet(
    shoppingViewModel: ShoppingViewModel,
    settingsViewModel: SettingsViewModel,
    item: ShoppingItem? = null,
    onSave: (ShoppingItem) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = item != null

    // Form state
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEditing) "Editar Item" else "Novo Item de Compra",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Name
        androidx.compose.material3.TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome do item") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
            )
        )

        // Category (optional)
        androidx.compose.material3.TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoria (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
            )
        )

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }

            FilledButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val newItem = ShoppingItem(
                            id = item?.id ?: 0,
                            name = name.trim(),
                            category = if (category.isBlank()) null else category.trim(),
                            isChecked = item?.isChecked ?: false,
                            ownerId = item?.ownerId ?: "",
                            createdAt = item?.createdAt ?: System.currentTimeMillis(),
                            checkedAt = item?.checkedAt,
                            checkedBy = item?.checkedBy
                        )
                        onSave(newItem)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank()
            ) { Text(if (isEditing) "Salvar" else "Adicionar") }
        }
    }
}