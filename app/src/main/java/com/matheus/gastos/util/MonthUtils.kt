package com.matheus.gastos.util

import java.util.Calendar

/**
 * Utilitários puros para navegação entre meses.
 * Todos usam o timezone default do device, igual a TransactionRepository.getMonthBounds.
 */

/** Primeiro instante (00:00:00.000) do mês que contém [millis]. */
fun monthStart(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/** Chave do mês no formato "yyyy-MM". */
fun monthKey(millis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    return String.format(java.util.Locale.US, "%04d-%02d", year, month)
}

/**
 * Soma [delta] meses ao mês de [millis], retornando o primeiro instante do mês resultado.
 * O dia é fixado em 1 antes de somar, então 31/01 + 1 mês cai em fevereiro (nunca estoura para março).
 */
fun addMonths(millis: Long, delta: Int): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = monthStart(millis)
    cal.add(Calendar.MONTH, delta)
    return cal.timeInMillis
}

/** Verdadeiro se [a] e [b] estão no mesmo mês/ano. */
fun isSameMonth(a: Long, b: Long): Boolean = monthKey(a) == monthKey(b)

/** Verdadeiro se o mês de [millis] é o mês corrente do device. */
fun isCurrentMonth(millis: Long): Boolean = isSameMonth(millis, System.currentTimeMillis())

/** Milissegundos até a próxima meia-noite local (nunca zero; mínimo de 1s). */
fun millisUntilNextMidnight(now: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.add(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return (cal.timeInMillis - now).coerceAtLeast(1_000L)
}
