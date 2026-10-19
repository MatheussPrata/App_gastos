package com.matheus.gastos.util

import java.util.Locale
import kotlin.math.round

/**
 * Comparador de preco por unidade (item 36).
 *
 * Normaliza embalagens diferentes para uma base comum:
 *  - peso (g / kg)  -> preco por kg
 *  - volume (ml / L) -> preco por litro
 *
 * Assim "500 g por R$ 8,00" e "1 kg por R$ 14,00" ficam comparaveis,
 * mesmo tendo unidades de digitacao diferentes.
 */
enum class PriceUnit(val label: String, val baseLabel: String) {
    GRAM("g", "kg"),
    KILOGRAM("kg", "kg"),
    MILLILITER("ml", "L"),
    LITER("L", "L")
}

/** Quantos gramas/ml existem em 1 unidade desta medida (base de calculo). */
private val PriceUnit.baseAmount: Double
    get() = when (this) {
        PriceUnit.GRAM -> 1.0
        PriceUnit.KILOGRAM -> 1000.0
        PriceUnit.MILLILITER -> 1.0
        PriceUnit.LITER -> 1000.0
    }

/** Multiplicador para chegar na base (kg ou L). */
private val PriceUnit.toBaseFactor: Double
    get() = when (this) {
        PriceUnit.GRAM, PriceUnit.MILLILITER -> 1000.0
        PriceUnit.KILOGRAM, PriceUnit.LITER -> 1.0
    }

/** Uma opcao sendo comparada: preco total e quantidade da embalagem. */
data class PriceOption(
    val label: String = "",
    val price: Double,
    val quantity: Double,
    val unit: PriceUnit
) {
    /** Preco por kg ou por litro (dependendo do tipo de medida). */
    val unitPrice: Double
        get() = if (quantity <= 0.0) Double.NaN else (price / quantity) * unit.toBaseFactor

    val unitPriceLabel: String
        get() = "R$ ${formatUnitPrice(unitPrice)} / ${unit.baseLabel}"
}

/** Resultado da comparacao: quem ganha e quanto se economiza. */
data class PriceComparison(
    val options: List<PriceOption>,
    val bestIndex: Int,
    val savings: Double,
    val savingsPercent: Double
) {
    val best: PriceOption get() = options[bestIndex]
}

/**
 * Compara as opcoes validas e retorna o resultado.
 * Retorna null quando ha menos de 2 opcoes uteis.
 */
fun comparePrices(options: List<PriceOption>): PriceComparison? {
    val valid = options.filter { it.price > 0.0 && it.quantity > 0.0 }
    if (valid.size < 2) return null

    val unitPrices = valid.map { it.unitPrice }
    val bestIndex = unitPrices.indices.minByOrNull { unitPrices[it] } ?: return null
    val worst = unitPrices.maxOrNull() ?: return null
    val best = unitPrices[bestIndex]

    // Economia = diferenca por kg/L multiplicada pela quantidade comprada da melhor opcao.
    val savings = (worst - best) * (valid[bestIndex].quantity / valid[bestIndex].unit.toBaseFactor)
    val percent = if (worst > 0.0) ((worst - best) / worst) * 100.0 else 0.0

    return PriceComparison(
        options = valid,
        bestIndex = bestIndex,
        savings = savings,
        savingsPercent = percent
    )
}

/** Formata preco unitario com 2 casas (ex.: 16,90). */
fun formatUnitPrice(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "-"
    return String.format(Locale("pt", "BR"), "%.2f", value)
}

/** Le "12,50" ou "12.50" como double. Retorna null se invalido. */
fun parsePriceInput(text: String): Double? {
    val clean = text.trim().replace("R$", "").replace(" ", "").replace(",", ".")
    return clean.toDoubleOrNull()?.takeIf { it > 0.0 }
}

/** Le "500", "1,5" ou "1.5" como double. Retorna null se invalido. */
fun parseQuantityInput(text: String): Double? {
    val clean = text.trim().replace(",", ".").replace(" ", "")
    return clean.toDoubleOrNull()?.takeIf { it > 0.0 }
}

/** Arredonda para 2 casas - util em testes e exibicao. */
fun round2(value: Double): Double = round(value * 100.0) / 100.0
