package com.matheus.gastos.data.local

import androidx.room.TypeConverter
import com.matheus.gastos.data.entity.BudgetPeriod
import com.matheus.gastos.data.entity.PaymentMethod
import com.matheus.gastos.data.entity.RecurringFrequency

class Converters {
    @TypeConverter
    fun fromBudgetPeriod(period: BudgetPeriod): String = period.name

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod = BudgetPeriod.valueOf(value)

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod =
        runCatching { PaymentMethod.valueOf(value) }.getOrDefault(PaymentMethod.DINHEIRO)

    @TypeConverter
    fun fromRecurringFrequency(value: String?): RecurringFrequency? {
        return value?.let { RecurringFrequency.valueOf(it.uppercase()) }
    }

    @TypeConverter
    fun toRecurringFrequency(freq: RecurringFrequency?): String? {
        return freq?.value
    }
}
