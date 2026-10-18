package com.matheus.gastos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Campo "selecionador": mostra um valor e abre um diálogo ao toque.
 *
 * O `clickable` NUNCA pode ficar no modifier do OutlinedTextField: o Material3
 * consome o toque no próprio campo (mesmo com readOnly) e o clique se perde.
 * Aqui o Box transparente por cima recebe o toque, e o campo fica `enabled = false`
 * para não roubar o evento nem abrir teclado.
 *
 * Como `enabled = false` pinta o campo em cinza (parecia desativado), as cores
 * padrão são sobrescritas explicitamente para manter a aparência normal/ativa.
 */
@Composable
fun SelectorField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.ArrowDropDown,
    contentDescription: String? = null
) {
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            trailingIcon = {
                Icon(imageVector = icon, contentDescription = contentDescription)
            }
        )
        // Camada de toque por cima do campo desabilitado.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}
