package com.matheus.gastos.ui.screen.add_card

/**
 * Parsers puros usados pelo formulário de cartão (Adicionar/Editar cartão).
 *
 * Ficam fora de [AddCardScreen.kt] para poderem ser testados em JVM (JUnit)
 * sem instanciar Compose. A tela deve usar estas funções como fonte única.
 */

/**
 * Converte o texto do campo "Limite (R$)" em Double.
 *
 * Regras:
 *  - vazio/branco -> 0.0
 *  - "1.234,56" (ponto de milhar + vírgula decimal) -> 1234.56
 *  - "10,50" -> 10.5
 *  - "10.50" / "10" -> 10.5 / 10.0
 *  - valores inválidos, negativos ou zero -> 0.0
 */
fun parseLimit(input: String): Double {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return 0.0

    val normalized = when {
        trimmed.contains(',') && trimmed.contains('.') ->
            if (trimmed.lastIndexOf(',') > trimmed.lastIndexOf('.')) {
                // pt-BR: "1.234,56"
                trimmed.replace(".", "").replace(',', '.')
            } else {
                // en-US: "1,234.56"
                trimmed.replace(",", "")
            }
        trimmed.contains(',') -> trimmed.replace(',', '.')
        else -> trimmed
    }

    val value = normalized.toDoubleOrNull() ?: return 0.0
    return if (value.isFinite() && value > 0.0) value else 0.0
}

/**
 * Converte o texto do dia (fechamento/vencimento) em Int dentro de 1..31.
 * Entrada inválida/vazia cai em [defaultValue] (também já coercido para 1..31).
 */
fun coerceDay(value: String, defaultValue: Int = 1): Int {
    val fallback = defaultValue.coerceIn(1, 31)
    val parsed = value.trim().toIntOrNull() ?: return fallback
    return parsed.coerceIn(1, 31)
}

/**
 * True quando o texto do limite é aceitável para salvar:
 * vazio (sem limite definido), ou um número > 0 (aceita vírgula ou ponto).
 */
fun isValidLimitInput(input: String): Boolean {
    if (input.isBlank()) return true
    return parseLimit(input) > 0.0
}

/**
 * True quando o texto do dia é aceitável: vazio, ou um inteiro em 1..31.
 */
fun isValidDayInput(input: String): Boolean {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return true
    val parsed = trimmed.toIntOrNull() ?: return false
    return parsed in 1..31
}
