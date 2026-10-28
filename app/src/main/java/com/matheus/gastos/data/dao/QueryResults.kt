package com.matheus.gastos.data.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.Category

// Query result classes for Room queries
// Separate file ensures KSP processes them before DAOs that use them

data class CategoryExpense(
    val categoryId: Long,
    val total: Double
)

data class CardExpense(
    val creditCardId: Long,
    val total: Double
)

data class BudgetLimitWithCategory(
    @Embedded val budgetLimit: BudgetLimit,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    ) val category: Category
)
