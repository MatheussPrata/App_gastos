package com.matheus.gastos.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    categoryViewModel: CategoryViewModel,
    creditCardViewModel: CreditCardViewModel,
    onNavigateToCategories: () -> Unit
) {
    val themeMode = settingsViewModel.themeMode.observeAsState(initial = 0)
    val accentColor = settingsViewModel.accentColor.observeAsState(initial = 0xFF6750A4.toInt())
    val notificationsEnabled = settingsViewModel.notificationsEnabled.observeAsState(initial = true)

    var showColorPicker by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Configurações") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
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
                        onClick = { }
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
            }

            item {
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
            }

            item {
                SettingsSection(title = "Limites de Gasto") {
                    SettingsItem(
                        title = "Gerenciar Limites",
                        subtitle = "Defina limites por categoria e período",
                        icon = Icons.Filled.Speed,
                        onClick = { }
                    )
                }
            }

            item {
                SettingsSection(title = "Categorias") {
                    SettingsItem(
                        title = "Gerenciar Categorias",
                        subtitle = "Adicione, edite ou remova categorias e subcategorias",
                        icon = Icons.Filled.Category,
                        onClick = onNavigateToCategories
                    )
                }
            }

            item {
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
                        onClick = { }
                    )
                }
            }

            item {
                SettingsSection(title = "Sobre") {
                    SettingsItem(
                        title = "Versão",
                        subtitle = "1.0.0",
                        icon = Icons.Filled.Info,
                        onClick = { }
                    )
                }
            }
        }

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
            text = title.uppercase(Locale.getDefault()),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
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
    icon: ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        headlineContent = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = trailing
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    currentColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = remember {
        listOf(
            0xFF6750A4.toInt(), 0xFF2196F3.toInt(), 0xFF4CAF50.toInt(), 0xFFFF9800.toInt(),
            0xFFF44336.toInt(), 0xFF9C27B0.toInt(), 0xFF009688.toInt(), 0xFFE91E63.toInt(),
            0xFF3F51B5.toInt(), 0xFF00BCD4.toInt(), 0xFF8BC34A.toInt(), 0xFFFFC107.toInt(),
            0xFF795548.toInt(), 0xFF607D8B.toInt(), 0xFFFF5722.toInt(), 0xFF673AB7.toInt()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
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
                                .border(if (isSelected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(24.dp))
                                .clickable { onColorSelected(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selecionado",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

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
            TextButton(onClick = { onBackup(); onDismiss() }) { Text("Fazer Backup") }
        },
        dismissButton = {
            TextButton(onClick = { onRestore(); onDismiss() }) { Text("Restaurar") }
        },
        title = { Text("Backup no Google Drive") },
        text = { Text("Escolha uma ação:") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onExport(); onDismiss() }) { Text("Exportar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Exportar Dados") },
        text = { Text("Os dados serão salvos como arquivo JSON na pasta Documentos/GastosApp") }
    )
}
