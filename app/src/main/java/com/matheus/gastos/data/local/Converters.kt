package com.matheus.gastos.data.local

import androidx.room.TypeConverter
import com.matheus.gastos.data.entity.BudgetPeriod

class Converters {
    @TypeConverter
    fun fromBudgetPeriod(period: BudgetPeriod): String = period.name

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod = BudgetPeriod.valueOf(value)
}
