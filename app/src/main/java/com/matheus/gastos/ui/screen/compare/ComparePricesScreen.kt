package com.matheus.gastos.ui.screen.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matheus.gastos.util.PriceOption
import com.matheus.gastos.util.PriceUnit
import com.matheus.gastos.util.comparePrices
import com.matheus.gastos.util.formatUnitPrice
import com.matheus.gastos.util.parsePriceInput
import com.matheus.gastos.util.parseQuantityInput

/** Linha editavel do comparador. Cada produto tem preco + quantidade + unidade. */
private class ComparableItem {
    var price by mutableStateOf("")
    var quantity by mutableStateOf("")
    var unit by mutableStateOf(PriceUnit.GRAM)
}

/**
 * Comparador de preco por unidade (item 36).
 *
 * Voce digita preco e quantidade de cada embalagem (g/kg/ml/L) e a tela
 * mostra quanto custa por kg ou por litro em cada uma, destacando a mais
 * economica e a economia em relacao a mais cara.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparePricesScreen(onOpenDrawer: (() -> Unit)? = null) {
    val items = remember {
        mutableStateListOf(
            ComparableItem(),
            ComparableItem()
        )
    }

    val options = remember(items.toList(), items.map { it.price + it.quantity + it.unit }) {
        items.mapIndexed { index, item ->
            val parsedPrice = parsePriceInput(item.price) ?: 0.0
            val parsedQty = parseQuantityInput(item.quantity) ?: 0.0
            PriceOption(
                label = "Produto ${index + 1}",
                price = parsedPrice,
                quantity = parsedQty,
                unit = item.unit
            )
        }
    }
    val comparison = comparePrices(options)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            navigationIcon = {
                if (onOpenDrawer != null) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                    }
                }
            },
            title = { Text("Comparador", fontWeight = FontWeight.SemiBold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Preço por kg ou por litro",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(items.size) { index ->
                val item = items[index]
                val option = options.getOrNull(index)
                val isBest = comparison != null &&
                    comparison.options.getOrNull(comparison.bestIndex) === option

                ComparableItemCard(
                    index = index,
                    item = item,
                    unitPriceText = option?.unitPriceLabel ?: "",
                    isValid = option != null && !option.unitPrice.isNaN(),
                    isBest = isBest,
                    onRemove = { if (items.size > 2) items.removeAt(index) }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { items.add(ComparableItem()) }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar produto", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (comparison != null) {
                item {
                    ResultCard(
                        bestLabel = comparison.best.label,
                        bestUnitPrice = comparison.best.unitPriceLabel,
                        savings = comparison.savings,
                        savingsPercent = comparison.savingsPercent
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparableItemCard(
    index: Int,
    item: ComparableItem,
    unitPriceText: String,
    isValid: Boolean,
    isBest: Boolean,
    onRemove: () -> Unit
) {
    val borderColor = if (isBest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isBest) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isBest) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Produto ${index + 1}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isBest) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Mais econômico",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(26.dp)
                    )
                }
                if (index >= 2) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Filled.Close, contentDescription = "Remover produto")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = item.price,
                    onValueChange = { item.price = it },
                    label = { Text("Preço (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = item.quantity,
                    onValueChange = { item.quantity = it },
                    label = { Text("Qtd.") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PriceUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = item.unit == unit,
                        onClick = { item.unit = unit },
                        label = { Text(unit.label, fontSize = 13.sp) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isValid) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Custo por unidade", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = unitPriceText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = "Informe preço e quantidade para comparar",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    bestLabel: String,
    bestUnitPrice: String,
    savings: Double,
    savingsPercent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mais econômico: $bestLabel",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = bestUnitPrice,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Diferença de R$ ${formatUnitPrice(savings)} nesta compra " +
                    "(${formatUnitPrice(savingsPercent)}% mais caro no outro produto)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}
