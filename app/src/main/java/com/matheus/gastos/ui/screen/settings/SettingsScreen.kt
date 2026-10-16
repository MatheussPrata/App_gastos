package com.matheus.gastos.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matheus.gastos.R
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.SettingsViewModel
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel,
    onNavigateToCategories: () -> Unit
) {
    val themeMode = settingsViewModel.themeMode.asFlow().collectAsStateWithLifecycle(initialValue = 0)
    val accentColor = settingsViewModel.accentColor.asFlow().collectAsStateWithLifecycle(initialValue = 0xFF6750A4.toInt())
    val notificationsEnabled = settingsViewModel.notificationsEnabled.asFlow().collectAsStateWithLifecycle(initialValue = true)

    var showColorPicker by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Configurações") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            // Theme Section
            SettingsSection(title = "Aparência") {
                SettingsItem(
                    title = "Tema",
                    subtitle = when (themeMode.value) {
                        0 -> "Sistema"
                        1 -> "Claro"
                        2 -> "Escuro"
                        else -> "Sistema"
                    },
                    icon = Icons.Filled.Palette,
                    onClick = { /* Show theme dialog */ }
                )

                SettingsItem(
                    title = "Cor de Destaque",
                    subtitle = "Toque para escolher",
                    icon = Icons.Filled.ColorLens,
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(accentColor.value), RoundedCornerShape(12.dp))
                        )
                    },
                    onClick = { showColorPicker = true }
                )
            }

            // Notifications Section
            SettingsSection(title = "Notificações") {
                SettingsItem(
                    title = "Avisos de Limite de Gasto",
                    subtitle = "Receba notificações quando atingir limites definidos",
                    icon = Icons.Filled.Notifications,
                    trailing = {
                        Checkbox(
                            checked = notificationsEnabled.value,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch { settingsViewModel.setNotificationsEnabled(enabled) }
                            }
                        )
                    },
                    onClick = {
                        coroutineScope.launch { settingsViewModel.setNotificationsEnabled(!notificationsEnabled.value) }
                    }
                )
            }

            // Budget Limits Section
            SettingsSection(title = "Limites de Gasto") {
                SettingsItem(
                    title = "Gerenciar Limites",
                    subtitle = "Defina limites por categoria e período",
                    icon = Icons.Filled.Speed,
                    onClick = { /* Navigate to budget limits screen */ }
                )
            }

            // Categories Section
            SettingsSection(title = "Categorias") {
                SettingsItem(
                    title = "Gerenciar Categorias",
                    subtitle = "Adicione, edite ou remova categorias e subcategorias",
                    icon = Icons.Filled.Category,
                    onClick = onNavigateToCategories
                )
            }

            // Backup Section
            SettingsSection(title = "Backup e Restauração") {
                SettingsItem(
                    title = "Backup no Google Drive",
                    subtitle = "Faça backup automático dos seus dados na nuvem",
                    icon = Icons.Filled.CloudUpload,
                    onClick = { showBackupDialog = true }
                )

                SettingsItem(
                    title = "Exportar Dados (JSON)",
                    subtitle = "Salve um arquivo local com todos os seus dados",
                    icon = Icons.Filled.Download,
                    onClick = { showExportDialog = true }
                )

                SettingsItem(
                    title = "Importar Dados (JSON)",
                    subtitle = "Restaure dados a partir de um arquivo exportado",
                    icon = Icons.Filled.Upload,
                    onClick = { /* Show file picker for import */ }
                )
            }

            // About Section
            SettingsSection(title = "Sobre") {
                SettingsItem(
                    title = "Versão",
                    subtitle = "1.0.0",
                    icon = Icons.Filled.Info,
                    onClick = { /* Show about dialog */ }
                )

                SettingsItem(
                    title = "Código Aberto",
                    subtitle = "Ver no GitHub",
                    icon = Icons.Filled.Code,
                    onClick = { /* Open GitHub */ }
                )
            }
        }

        // Dialogs - render conditionally inside Column (composable context)
        if (showColorPicker) {
            ColorPickerDialog(
                currentColor = accentColor.value,
                onColorSelected = { color ->
                    coroutineScope.launch { settingsViewModel.setAccentColor(color) }
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
        }

        if (showBackupDialog) {
            BackupDialog(
                onBackup = { showBackupDialog = false },
                onRestore = { showBackupDialog = false },
                onDismiss = { showBackupDialog = false }
            )
        }

        if (showExportDialog) {
            ExportDialog(
                onExport = { showExportDialog = false },
                onDismiss = { showExportDialog = false }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title.uppercase(java.util.Locale.getDefault()),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        headlineContent = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = trailing
    )
}

// Color Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    currentColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = remember {
        listOf<Int>(
            0xFF6750A4.toInt(), 0xFF2196F3.toInt(), 0xFF4CAF50.toInt(), 0xFFFF9800.toInt(),
            0xFFF44336.toInt(), 0xFF9C27B0.toInt(), 0xFF009688.toInt(), 0xFFE91E63.toInt(),
            0xFF3F51B5.toInt(), 0xFF00BCD4.toInt(), 0xFF8BC34A.toInt(), 0xFFFFC107.toInt(),
            0xFF795548.toInt(), 0xFF607D8B.toInt(), 0xFFFF5722.toInt(), 0xFF673AB7.toInt()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("OK") }
        },
        title = { Text("Escolher Cor de Destaque") },
        text = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Esta cor será usada como cor principal do app")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(colors) { color ->
                        val isSelected = color == currentColor
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 56.dp else 48.dp)
                                .background(Color(color), RoundedCornerShape(24.dp))
                                .border(if (isSelected) 3.dp else 0.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurface, RoundedCornerShape(24.dp))
                                .padding(if (isSelected) 4.dp else 0.dp)
                                .clickable { onColorSelected(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selecionado",
                                    tint = if (color == 0xFFFFFFFF.toInt() || color == 0xFF000000.toInt()) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

// Backup Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupDialog(
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onBackup(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Fazer Backup") }
        },
        dismissButton = {
            TextButton(
                onClick = { onRestore(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Restaurar") }
        },
        title = { Text("Backup no Google Drive") },
        text = { Text("Escolha uma ação:") }
    )
}

// Export Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onExport(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Exportar") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        },
        title = { Text("Exportar Dados") },
        text = { Text("Os dados serão salvos como arquivo JSON na pasta Documentos/GastosApp") }
    )
}