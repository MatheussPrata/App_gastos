package com.matheus.gastos.data.entity

import androidx.room.Embedded
import androidx.room.Relation

// Query result classes for Room raw queries
// These are NOT @Entity - they're just data classes for query results

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
