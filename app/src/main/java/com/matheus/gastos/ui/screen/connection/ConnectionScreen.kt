package com.matheus.gastos.ui.screen.connection

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matheus.gastos.R
import com.matheus.gastos.ui.components.AmountText
import com.matheus.gastos.ui.components.CategoryChip
import com.matheus.gastos.ui.components.DateText
import com.matheus.gastos.ui.components.EmptyState
import com.matheus.gastos.ui.components.TransactionItem
import com.matheus.gastos.ui.theme.GastosTheme
import com.matheus.gastos.viewmodel.SettingsViewModel
import com.matheus.gastos.viewmodel.CategoryViewModel
import com.matheus.gastos.viewmodel.CreditCardViewModel
import com.matheus.gastos.viewmodel.ConnectionViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConnectionScreen(
    connectionViewModel: ConnectionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by connectionViewModel.state.collectAsStateWithLifecycle()
    var showGenerateDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var inviteCodeInput by remember { mutableStateOf("") }

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.TopAppBar(
                title = { Text("Conexão Financeira") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                )
            )

            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    when (state) {
                        is ConnectionViewModel.ConnectionState.Loading -> {
                            item {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator()
                                }
                            }
                        }
                        is ConnectionViewModel.ConnectionState.Error -> {
                            item {
                                ErrorState(message = it.message, onRetry = { connectionViewModel.loadConnection() })
                            }
                        }
                        is ConnectionViewModel.ConnectionState.Disconnected -> {
                            item {
                                DisconnectedState(
                                    inviteCode = it.inviteCode,
                                    onGenerateClick = { showGenerateDialog = true },
                                    onAcceptClick = { showAcceptDialog = true }
                                )
                            }
                        }
                        is ConnectionViewModel.ConnectionState.Connected -> {
                            item {
                                ConnectedState(
                                    connection = it.connection,
                                    onDisconnectClick = {
                                        androidx.compose.material3.AlertDialog(
                                            onDismissRequest = {},
                                            title = { Text("Desconectar?") },
                                            text = { Text("Tem certeza que deseja se desconectar? As finanças e lista de compras compartilhadas não serão mais sincronizadas.") },
                                            confirmButton = {
                                                androidx.compose.material3.TextButton(
                                                    onClick = {
                                                        connectionViewModel.disconnect()
                                                    }
                                                ) { Text("Desconectar") }
                                            },
                                            dismissButton = {
                                                androidx.compose.material3.TextButton(onClick = {}) { Text("Cancelar") }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Generate Invite Code Dialog
                if (showGenerateDialog) {
                    GenerateInviteCodeDialog(
                        onDismiss = { showGenerateDialog = false },
                        onCodeGenerated = { code ->
                            showGenerateDialog = false
                        }
                    )
                }

                // Accept Invite Dialog
                if (showAcceptDialog) {
                    AcceptInviteDialog(
                        codeInput = inviteCodeInput,
                        onCodeChange = { inviteCodeInput = it },
                        onDismiss = { showAcceptDialog = false; inviteCodeInput = "" },
                        onAccept = { code ->
                            connectionViewModel.acceptInvite(code)
                            showAcceptDialog = false
                            inviteCodeInput = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DisconnectedState(
    inviteCode: String?,
    onGenerateClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.filled.Icons.Default.PeopleOutline,
                contentDescription = "Sem conexão",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
        }

        Text(
            text = "Nenhuma conexão ativa",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.TextAlign.Center
        )

        Text(
            text = "Conecte-se com outra pessoa para compartilhar finanças e lista de compras.",
            fontSize = 16.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        androidx.compose.material3.FilledButton(
            onClick = onGenerateClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.filled.Icons.Default.QrCode,
                contentDescription = "QR Code",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Gerar meu código de convite")
        }

        androidx.compose.material3.OutlinedButton(
            onClick = onAcceptClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.filled.Icons.Default.PersonAdd,
                contentDescription = "Adicionar pessoa",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Tenho um código de convite")
        }
    }
}

@Composable
fun ConnectedState(
    connection: com.matheus.gastos.data.entity.Connection,
    onDisconnectClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.filled.Icons.Default.Link,
                    contentDescription = "Conectado",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Conectado com ${connection.partnerUsername}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )

                Text(
                    text = "Vocês compartilham transações, lista de compras e transações recorrentes.",
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.filled.Icons.Default.Shield,
                        contentDescription = "Seguro",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Sincronização ativa",
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Invite Code Section
        androidx.compose.material3.Text(
            text = "Seu código de convite",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = connection.inviteCode,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Compartilhe este código com seu parceiro para conectar.",
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { /* Copy to clipboard */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.ContentCopy,
                            contentDescription = "Copiar",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Copiar")
                    }

                    androidx.compose.material3.FilledButton(
                        onClick = { /* Share */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.filled.Icons.Default.Share,
                            contentDescription = "Compartilhar",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Compartilhar")
                    }
                }
            }
        }
    }
}

@Composable
fun GenerateInviteCodeDialog(
    onDismiss: () -> Unit,
    onCodeGenerated: (String) -> Unit
) {
    val connectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.matheus.gastos.viewmodel.ConnectionViewModel>()
    val code by remember { mutableStateOf("") }
    val isGenerating by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gerando código...") },
        text = {
            if (isGenerating) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Text("Gerando código de convite único...")
                }
            } else if (code.isNotBlank()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seu código de convite:",
                        fontSize = 16.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = code,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Envie este código para a pessoa que deseja conectar.",
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            if (code.isBlank() && !isGenerating) {
                androidx.compose.material3.TextButton(
                    onClick = {
                        // Trigger generation
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Gerar") }
            } else if (code.isNotBlank()) {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onCodeGenerated(code)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Copiei") }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Fechar") }
        }
    ) {
        // Generate code on dialog show
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (code.isBlank()) {
                // Trigger generation via viewmodel
            }
        }
    }
}

@Composable
fun AcceptInviteDialog(
    codeInput: String,
    onCodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAccept: (String) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aceitar Convite") },
        text = {
            androidx.compose.material3.TextField(
                value = codeInput,
                onValueChange = onCodeChange,
                label = { Text("Código de convite") },
                placeholder = { Text("ABC123XY") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                ),
                visualTransformation = androidx.compose.ui.text.input.VisualTransformation { text ->
                    androidx.compose.ui.text.AnnotatedString(text.text.uppercase())
                }
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    if (codeInput.length == 8) onAccept(codeInput.uppercase())
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Conectar") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    )
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.filled.Icons.Default.ErrorOutline,
            contentDescription = "Erro",
            tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "Erro",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        androidx.compose.material3.FilledButton(
            onClick = onRetry
        ) { Text("Tentar novamente") }
    }
}