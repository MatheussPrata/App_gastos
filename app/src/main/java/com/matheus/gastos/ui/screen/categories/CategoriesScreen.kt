package com.matheus.gastos.ui.screen.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.CategoryViewModel
import java.util.Locale

@Composable
fun CategoriesScreen(
    categoryViewModel: CategoryViewModel,
    onClose: () -> Unit = {},
    categoryId: Long = 0L
) {
    val categories = categoryViewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList()).value
    val customCategories = categoryViewModel.customCategories.collectAsStateWithLifecycle(initialValue = emptyList()).value

    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryIcon by remember { mutableStateOf("category") }
    var newCategoryColor by remember { mutableStateOf(0xFF6750A4) }
    var editingCategoryId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Categorias") },
            navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") } },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(customCategories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(category.color), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Category,
                                contentDescription = category.name,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(horizontal = 16.dp))
                        Text(
                            text = category.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { 
                                editingCategoryId = category.id
                                // Show edit dialog
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { /* Delete */ }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    androidx.compose.material3.IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        content = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Adicionar",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(start = 8.dp))
                                Text(
                                    text = "Nova categoria",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
