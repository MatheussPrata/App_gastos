package com.matheus.gastos.ui.screen.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import com.matheus.gastos.data.entity.ShoppingItem
import com.matheus.gastos.viewmodel.ShoppingViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShoppingScreen(
    shoppingViewModel: ShoppingViewModel,
    onNavigateBack: () -> Unit
) {
    val items by shoppingViewModel.items.collectAsStateWithLifecycle()
    val pendingItems = items.filter { !it.isChecked }
    val checkedItems = items.filter { it.isChecked }
    val progress = if (items.isEmpty()) 0f else checkedItems.size.toFloat() / items.size

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem: ShoppingItem? by remember { mutableStateOf(null) }
    var showClearCheckedDialog by remember { mutableStateOf(false) }

    // Form state
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Lista de Compras") },
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = { onNavigateBack() }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            },
            actions = {
                if (checkedItems.isNotEmpty()) {
                    IconButton(onClick = { showClearCheckedDialog = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.DeleteSweep,
                            contentDescription = "Limpar comprados"
                        )
                    }
                }
                IconButton(onClick = {
                    editingItem = null
                    name = ""
                    category = ""
                    showAddDialog = true
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.AddShoppingCart,
                        contentDescription = "Adicionar item"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        if (items.isEmpty()) {
            EmptyShoppingState(onAddClick = {
                name = ""
                category = ""
                showAddDialog = true
            })
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest,
                        indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${checkedItems.size} de ${items.size} comprados",
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, 8.dp, 0.dp, 100.dp)
                ) {
                    if (pendingItems.isNotEmpty()) {
                        item {
                            SectionHeader(title = "A comprar (${pendingItems.size})")
                        }
                        items(pendingItems) { item ->
                            ShoppingItemTile(
                                item = item,
                                onToggle = { shoppingViewModel.toggleCheck(item.id) },
                                onEdit = {
                                    editingItem = item
                                    name = item.name
                                    category = item.category ?: ""
                                    showAddDialog = true
                                },
                                onDelete = { shoppingViewModel.delete(item.id) }
                            )
                        }
                    }
                    if (checkedItems.isNotEmpty()) {
                        item {
                            Divider(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                            SectionHeader(title = "Comprados (${checkedItems.size})")
                        }
                        items(checkedItems) { item ->
                            ShoppingItemTile(
                                item = item,
                                onToggle = { shoppingViewModel.toggleCheck(item.id) },
                                onEdit = {
                                    editingItem = item
                                    name = item.name
                                    category = item.category ?: ""
                                    showAddDialog = true
                                },
                                onDelete = { shoppingViewModel.delete(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog) {
        ShoppingItemDialog(
            isEditing = editingItem != null,
            item = editingItem,
            name = name,
            onNameChange = { name = it },
            category = category,
            onCategoryChange = { category = it },
            onSave = {
                if (name.isNotBlank()) {
                    if (editingItem != null) {
                        shoppingViewModel.updateItem(editingItem!!.copy(name = name, category = if (category.isBlank()) null else category))
                    } else {
                        shoppingViewModel.addItem(
                            name = name,
                            category = if (category.isBlank()) null else category
                        )
                    }
                    showAddDialog = false
                    editingItem = null
                    name = ""
                    category = ""
                }
            },
            onCancel = {
                showAddDialog = false
                editingItem = null
                name = ""
                category = ""
            }
        )
    }

    // Clear Checked Dialog
    if (showClearCheckedDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearCheckedDialog = false },
            title = { Text("Limpar itens comprados?") },
            text = {
                Text("Isso removerá ${checkedItems.size} item(ns) marcados como comprados.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        checkedItems.forEach { shoppingViewModel.delete(it.id) }
                        showClearCheckedDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Limpar") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCheckedDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun EmptyShoppingState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.filled.Icons.Default.ShoppingCartOutline,
                contentDescription = "Lista vazia",
                modifier = Modifier.size(72.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "Lista de compras vazia",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Toque no botão + para adicionar itens.\nItens marcados ficam no final da lista.",
                textAlign = androidx.compose.ui.text.TextAlign.Center,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
fun ShoppingItemTile(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    DismissibleItem(
        onDismiss = onDelete
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (item.isChecked)
                    androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
                else
                    androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            androidx.compose.material3.CheckboxListTile(
                modifier = Modifier.fillMaxWidth(),
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                headlineContent = {
                    Text(
                        text = item.name,
                        fontSize = 16.sp,
                        textDecoration = if (item.isChecked) androidx.compose.ui.text.TextDecoration.LineThrough else null,
                        color = if (item.isChecked)
                            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        else null
                    )
                },
                supportingContent = {
                    item.category?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = if (item.isChecked)
                                androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else
                                androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                secondaryContent = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = if (item.isChecked)
                                androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else
                                androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = onToggle
            )
        }
    }
}

@Composable
fun ShoppingItemDialog(
    isEditing: Boolean,
    item: ShoppingItem?,
    name: String,
    onNameChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (isEditing) "Editar item" else "Novo item de compra") },
        text = {
            Column(
                modifier = Modifier.width(320.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.TextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nome do item") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
                    )
                )

                androidx.compose.material3.TextField(
                    value = category,
                    onValueChange = onCategoryChange,
                    label = { Text("Categoria (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isEditing) "Salvar" else "Adicionar") }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}

// Dismissible Item wrapper for swipe-to-delete
@Composable
fun DismissibleItem(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.SwipeToDismissBox(
        state = rememberDismissState(confirmStateChange = { it == DismissValue.DismissedToEnd })
    ) { dismissState ->
        content()
    }
}