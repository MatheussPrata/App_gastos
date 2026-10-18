package com.matheus.gastos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.hypot

/** Entrada genérica de um gráfico: rótulo, valor e cor. */
data class ChartEntry(
    val label: String,
    val value: Double,
    val color: Color
)

private fun formatCurrency(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

@Composable
fun EmptyChartMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = "Sem dados no período",
            modifier = Modifier.padding(24.dp),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Gráfico de barras horizontais: nome à esquerda, barra proporcional ao maior valor,
 * valor em R$ à direita. Entradas são ordenadas de forma decrescente por valor.
 */
@Composable
fun HorizontalBarChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onBarClick: ((Int) -> Unit)? = null
) {
    val sorted = entries.filter { it.value > 0.0 }.sortedByDescending { it.value }
    if (sorted.isEmpty()) {
        EmptyChartMessage()
        return
    }
    val max = sorted.first().value
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            sorted.forEachIndexed { index, entry ->
                val isSelected = selectedIndex == index
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) entry.color.copy(alpha = 0.12f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .then(
                            if (onBarClick != null) {
                                Modifier.clickable { onBarClick(index) }
                            } else {
                                Modifier
                            }
                        )
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.label,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatCurrency(entry.value),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = entry.color
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(trackColor, RoundedCornerShape(6.dp))
                    ) {
                        val fraction = barFraction(entry.value, max)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(12.dp)
                                .background(entry.color, RoundedCornerShape(6.dp))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Gráfico de pizza (donut) em Canvas com legenda de cor, nome, percentual e valor.
 */
@Composable
fun PieChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onSliceClick: ((Int) -> Unit)? = null
) {
    val data = entries.filter { it.value > 0.0 }.sortedByDescending { it.value }
    if (data.isEmpty()) {
        EmptyChartMessage()
        return
    }
    val total = data.sumOf { it.value }
    val angles = sliceSweepAngles(data.map { it.value.toFloat() })
    val surfaceColor = MaterialTheme.colorScheme.surface
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .onSizeChanged { canvasSize = it }
                    .then(
                        if (onSliceClick != null) {
                            Modifier.pointerInput(angles, data.size) {
                                detectTapGestures { offset ->
                                    val radius = canvasSize.height.toFloat() / 2f
                                    if (radius > 0f) {
                                        val dx = offset.x - canvasSize.width / 2f
                                        val dy = offset.y - canvasSize.height / 2f
                                        val distance = hypot(dx, dy)
                                        // Ignora toques no furo central (donut) e fora do círculo.
                                        if (distance >= radius * 0.30f && distance <= radius) {
                                            var touchAngle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
                                            // Converte para o ângulo do desenho: 0 = topo, horário.
                                            touchAngle = (touchAngle + 90f + 360f) % 360f
                                            val tapped = sliceIndexAtAngle(angles, touchAngle)
                                            if (tapped >= 0) onSliceClick(tapped)
                                        }
                                    }
                                }
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                val diameter = size.minDimension
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)
                var startAngle = -90f
                data.forEachIndexed { index, entry ->
                    val sweep = angles.getOrElse(index) { 0f }
                    if (sweep > 0f) {
                        drawArc(
                            color = entry.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize
                        )
                    }
                    startAngle += sweep
                }
                // Furo central (donut)
                drawCircle(
                    color = surfaceColor,
                    radius = diameter * 0.30f,
                    center = center
                )
                // Contorno leve na fatia selecionada
                if (selectedIndex != null && selectedIndex in data.indices) {
                    var start = -90f
                    data.forEachIndexed { index, entry ->
                        val sweep = angles.getOrElse(index) { 0f }
                        if (index == selectedIndex) {
                            // Fatia destacada: traço branco grosso marcando a borda.
                            drawArc(
                                color = Color.White.copy(alpha = 0.9f),
                                startAngle = start,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = 4.dp.toPx())
                            )
                            drawArc(
                                color = entry.color,
                                startAngle = start,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                        start += sweep
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (selectedIndex == index) entry.color.copy(alpha = 0.12f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .then(
                                if (onSliceClick != null) {
                                    Modifier.clickable { onSliceClick(index) }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(entry.color, RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.label,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = percentLabel(entry.value, total),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatCurrency(entry.value),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = entry.color
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barras verticais simples por mês, desenhadas em Canvas.
 * Cada barra é proporcional a |saldo| em relação ao maior |saldo|;
 * barras positivas usam [positiveColor] e negativas [negativeColor].
 */
@Composable
fun VerticalMonthlyBars(
    entries: List<ChartEntry>,
    positiveColor: Color,
    negativeColor: Color,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        EmptyChartMessage()
        return
    }
    val maxAbs = entries.maxOf { kotlin.math.abs(it.value) }
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val chartHeight = size.height - 24.dp.toPx()
                val baseline = chartHeight / 2f
                val slotWidth = if (entries.isEmpty()) size.width else size.width / entries.size
                val barWidth = (slotWidth * 0.55f).coerceAtMost(32.dp.toPx())
                val halfHeight = chartHeight / 2f - 4.dp.toPx()

                drawLine(
                    color = axisColor,
                    start = androidx.compose.ui.geometry.Offset(0f, baseline),
                    end = androidx.compose.ui.geometry.Offset(size.width, baseline),
                    strokeWidth = 1.dp.toPx()
                )

                entries.forEachIndexed { index, entry ->
                    val fraction = barFraction(kotlin.math.abs(entry.value), maxAbs)
                    val barHeight = halfHeight * fraction
                    val centerX = slotWidth * index + slotWidth / 2f
                    val top = if (entry.value >= 0) baseline - barHeight else baseline
                    val color = if (entry.value >= 0) positiveColor else negativeColor
                    drawRoundRect(
                        color = color,
                        topLeft = androidx.compose.ui.geometry.Offset(centerX - barWidth / 2f, top),
                        size = Size(barWidth, barHeight.coerceAtLeast(0f)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                    if (entry.value == 0.0) {
                        drawLine(
                            color = color,
                            start = androidx.compose.ui.geometry.Offset(centerX - barWidth / 2f, baseline),
                            end = androidx.compose.ui.geometry.Offset(centerX + barWidth / 2f, baseline),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                entries.forEach { entry ->
                    val shortLabel = if (entry.label.length >= 7) entry.label.substring(5) else entry.label
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = shortLabel,
                            fontSize = 10.sp,
                            color = labelColor
                        )
                        Text(
                            text = formatCurrency(entry.value),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (entry.value >= 0) positiveColor else negativeColor
                        )
                    }
                }
            }
        }
    }
}
