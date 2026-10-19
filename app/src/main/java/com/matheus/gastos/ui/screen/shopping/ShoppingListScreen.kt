package com.matheus.gastos.ui.screen.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import com.matheus.gastos.data.entity.ShoppingCategory
import com.matheus.gastos.data.entity.ShoppingItem
import com.matheus.gastos.viewmodel.ShoppingViewModel

/**
 * Lista de compras (item 37).
 *
 * Pratica por desenho:
 *  - campo unico no topo: digite, escolha a categoria e aperte "adicionar"
 *    (ou o botao de confirmar do teclado) - o item ja cai no grupo certo;
 *  - chips horizontais para escolher a categoria da proxima adicao;
 *  - itens agrupados por categoria (Açougue, Padaria, Limpeza...);
 *  - marcar/desmarcar com um toque; "limpar marcados" no fim.
 *
 * Tudo persistido no Room (tabela shopping_items, migracao 3->4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onOpenDrawer: (() -> Unit)? = null,
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    val items by viewModel.items.observeAsState(initial = emptyList())
    val focusManager = LocalFocusManager.current

    var newItem by remember { mutableStateOf("") }
    var newQuantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ShoppingCategory.MERCEARIA) }

    val allItems: List<ShoppingItem> = items
    val checkedCount = allItems.count { it.isChecked }
    val grouped: List<Pair<String, List<ShoppingItem>>> = remember(allItems) {
        allItems.filter { !it.isChecked }
            .groupBy { it.category }
            .toList()
            .sortedBy { (category, _) -> ShoppingCategory.all.indexOf(category).let { i -> if (i < 0) 99 else i } }
    }
    val checkedItems: List<ShoppingItem> = remember(allItems) { allItems.filter { it.isChecked } }

    fun addCurrent() {
        val clean = newItem.trim()
        if (clean.isEmpty()) return
        viewModel.add(clean, selectedCategory, newQuantity)
        newItem = ""
        newQuantity = ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            navigationIcon = {
                if (onOpenDrawer != null) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                    }
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Lista de compras", fontWeight = FontWeight.SemiBold)
                    if (allItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${allItems.count { !it.isChecked }}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            actions = {
                if (checkedCount > 0) {
                    IconButton(onClick = { viewModel.clearChecked() }) {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = "Limpar comprados",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        // Entrada do item: nome + quantidade (opcional) + botao adicionar.
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = newItem,
                onValueChange = { newItem = it },
                label = { Text("O que falta?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newQuantity,
                    onValueChange = { newQuantity = it },
                    label = { Text("Qtd.") },
                    placeholder = { Text("ex.: 2 kg") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        addCurrent()
                        focusManager.clearFocus()
                    })
                )
                Surface(
                    onClick = {
                        addCurrent()
                        focusManager.clearFocus()
                    },
                    enabled = newItem.isNotBlank(),
                    shape = CircleShape,
                    color = if (newItem.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Adicionar item",
                            tint = if (newItem.isNotBlank()) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        // Categoria da proxima adicao.
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(count = ShoppingCategory.all.size, key = { i -> ShoppingCategory.all[i] }) { i ->
                val category = ShoppingCategory.all[i]
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 13.sp) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (allItems.isEmpty()) {
                item {
                    EmptyShoppingState()
                }
            }

            grouped.forEach { (category, groupItems) ->
                item(key = "header_$category") {
                    CategoryHeader(category = category, count = groupItems.size)
                }
                items(count = groupItems.size, key = { i -> groupItems[i].id }) { i ->
                    val item = groupItems[i]
                    ShoppingRow(
                        item = item,
                        onToggle = { viewModel.toggle(item) },
                        onDelete = { viewModel.delete(item) }
                    )
                }
            }

            if (checkedItems.isNotEmpty()) {
                item(key = "header_checked") {
                    CategoryHeader(
                        category = "Comprados",
                        count = checkedItems.size,
                        muted = true
                    )
                }
                items(count = checkedItems.size, key = { i -> "checked_${checkedItems[i].id}" }) { i ->
                    val item = checkedItems[i]
                    ShoppingRow(
                        item = item,
                        onToggle = { viewModel.toggle(item) },
                        onDelete = { viewModel.delete(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: String, count: Int, muted: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (muted) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "($count)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.isChecked, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (item.quantity.isNotBlank()) {
                    Text(
                        text = item.quantity,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Remover ${item.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyShoppingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ShoppingBasket,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lista vazia",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Digite acima e escolha a categoria.\nA lista fica salva mesmo se você fechar o app.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
