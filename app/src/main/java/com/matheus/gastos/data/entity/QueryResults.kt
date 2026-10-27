package com.matheus.gastos.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

// Query result classes for Room raw queries
// Marked as @Entity with dummy tables so KSP/Room can resolve them as return types

@Entity(tableName = "dummy_category_expense")
data class CategoryExpense(
    @PrimaryKey val categoryId: Long,
    val total: Double
)

@Entity(tableName = "dummy_card_expense")
data class CardExpense(
    @PrimaryKey val creditCardId: Long,
    val total: Double
)

@Entity(tableName = "dummy_budget_limit_with_category")
data class BudgetLimitWithCategory(
    @Embedded val budgetLimit: BudgetLimit,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    ) val category: Category
) {
    @PrimaryKey
    var dummyId: Long = 0
        @Suppress("UNUSED_PARAMETER")
        @Suppress("UNUSED_PARAMETER")
        private set
}
