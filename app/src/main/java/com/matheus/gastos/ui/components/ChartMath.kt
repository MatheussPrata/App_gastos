package com.matheus.gastos.ui.components

import java.util.Locale
import kotlin.math.abs

/**
 * Funções puras usadas pelos gráficos de Relatórios.
 * Sem dependência de Android para permitir teste em JVM puro.
 */

/**
 * Calcula o ângulo (em graus) de cada fatia de um gráfico de pizza.
 * A soma dos ângulos é sempre exatamente 360f quando há ao menos um valor positivo;
 * o último valor positivo absorve o arredondamento para fechar o círculo.
 * Valores <= 0 ou NaN recebem 0f. Lista vazia (ou sem valores positivos) retorna lista vazia.
 */
fun sliceSweepAngles(values: List<Float>): List<Float> {
    if (values.isEmpty()) return emptyList()
    val positive = values.map { if (it.isNaN() || it <= 0f) 0f else it }
    val total = positive.sum()
    if (total <= 0f) return emptyList()

    val angles = MutableList(values.size) { 0f }
    var accumulated = 0f
    var lastPositiveIndex = -1
    positive.forEachIndexed { index, value ->
        if (value > 0f) lastPositiveIndex = index
    }
    for (i in positive.indices) {
        if (positive[i] > 0f && i != lastPositiveIndex) {
            val angle = positive[i] / total * 360f
            angles[i] = angle
            accumulated += angle
        }
    }
    angles[lastPositiveIndex] = 360f - accumulated
    return angles
}

/**
 * Índice da fatia de pizza que contém [angleDeg], considerando os ângulos acumulados de [sweeps].
 * O ângulo 0f corresponde ao início da primeira fatia; 360f equivale a 0f.
 * Fatias com sweep 0 são ignoradas. Retorna -1 se a lista estiver vazia, sem ângulos positivos
 * ou se o ângulo for inválido (fora de 0f..360f ou NaN).
 */
fun sliceIndexAtAngle(sweeps: List<Float>, angleDeg: Float): Int {
    if (sweeps.isEmpty() || angleDeg.isNaN() || angleDeg < 0f || angleDeg > 360f) return -1
    val totalPositive = sweeps.filter { it > 0f }.sum()
    if (totalPositive <= 0f) return -1
    val normalized = if (angleDeg == 360f) 0f else angleDeg
    var accumulated = 0f
    for (index in sweeps.indices) {
        val sweep = sweeps[index]
        if (sweep > 0f) {
            accumulated += sweep
            if (normalized < accumulated) return index
        }
    }
    // 359,99... com arredondamento acumulado: atribui à última fatia positiva.
    return sweeps.indexOfLast { it > 0f }
}

/**
 * Fração 0f..1f de uma barra em relação ao maior valor.
 * Seguro para max <= 0, value <= 0 e NaN; valores acima do max são limitados a 1f.
 */
fun barFraction(value: Double, max: Double): Float {
    if (value.isNaN() || max.isNaN()) return 0f
    if (max <= 0.0 || value <= 0.0) return 0f
    val fraction = value / max
    if (fraction.isNaN()) return 0f
    return fraction.coerceIn(0.0, 1.0).toFloat()
}

/**
 * Percentual formatado em pt-BR com uma casa decimal, ex.: "12,3%".
 * Total 0 (ou não positivo) resulta em "0,0%".
 */
fun percentLabel(part: Double, total: Double): String {
    val pct = if (total > 0.0 && !total.isNaN() && !part.isNaN()) part / total * 100.0 else 0.0
    // Arredonda em duas etapas para garantir o mesmo resultado do formatador
    // (ex.: 12.345 -> 12,3 e não 12,4 por imprecisão binária).
    val scaled = abs(pct) * 10.0
    val roundedTenths = kotlin.math.floor(scaled + 0.5 + 1e-9)
    val rounded = roundedTenths / 10.0
    return String.format(Locale("pt", "BR"), "%.1f%%", rounded)
}
