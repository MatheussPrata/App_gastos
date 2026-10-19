package com.matheus.gastos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.matheus.gastos.data.local.Converters
import java.util.Date

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val icon: String = "category",
    val color: Int = 0xFF6750A4.toInt(),
    val isDefault: Boolean = false,
    /**
     * true  -> categoria de GASTO
     * false -> categoria de ENTRADA
     * Mantém compatibilidade com o app 0.1.0 (categorias antigas ficavam em gasto).
     */
    val isExpenseDefault: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Forma de pagamento da transação. Persistido como String (nome do enum)
 * via [com.matheus.gastos.data.local.Converters].
 */
enum class PaymentMethod(val label: String) {
    DINHEIRO("Dinheiro"),
    CARTAO("Cartão"),
    PIX("Pix"),
    DEBITO("Débito"),
    BOLETO("Boleto"),
    TRANSFERENCIA("Transferência")
}

@Entity(tableName = "subcategories")
data class Subcategory(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val categoryId: Long,
    val name: String,
    val icon: String = "subcategory",
    val color: Int = 0xFF6750A4.toInt(),
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val lastFourDigits: String = "",
    val brand: String = "VISA",
    val color: Int = 0xFF2196F3.toInt(),
    val limitAmount: Double = 0.0,
    val closingDay: Int = 1,
    val dueDay: Int = 10,
    val isActive: Boolean = true,
    /**
     * URI de foto/imagem personalizada do cartao (estilo carteira).
     * Preenchida pelo seletor de imagem da galeria; null = usa apenas a cor.
     */
    val imageUri: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    indices = [
        androidx.room.Index(value = ["date"]),
        androidx.room.Index(value = ["categoryId"]),
        androidx.room.Index(value = ["creditCardId"]),
        androidx.room.Index(value = ["installmentGroupId"])
    ]
)
@TypeConverters(Converters::class)
data class Transaction(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val description: String,
    val amount: Double,
    val date: Long,
    val isExpense: Boolean = true,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val creditCardId: Long? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.DINHEIRO,
    val isInstallment: Boolean = false,
    val installmentNumber: Int = 1,
    val totalInstallments: Int = 1,
    val installmentGroupId: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_limits")
data class BudgetLimit(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val categoryId: Long,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val limitAmount: Double,
    val spentAmount: Double = 0.0,
    val isActive: Boolean = true,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BudgetPeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

/**
 * Item da lista de compras (item 37).
 *
 * Persistido localmente (Room) para sobreviver a fechamento do app.
 * Separado por categoria de compra (Açougue, Padaria, Limpeza...) - uma
 * dimensao propria, independente das categorias financeiras.
 */
@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val quantity: String = "",
    val category: String = ShoppingCategory.OUTROS,
    val isChecked: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/** Categorias praticas de supermercado, na ordem em que aparecem na lista. */
object ShoppingCategory {
    const val ACOUGUE = "Açougue"
    const val PADARIA = "Padaria"
    const val HORTIFRUTI = "Hortifruti"
    const val MERCEARIA = "Mercearia"
    const val FRIOS = "Frios e Laticínios"
    const val BEBIDAS = "Bebidas"
    const val LIMPEZA = "Limpeza"
    const val HIGIENE = "Higiene"
    const val CONGELADOS = "Congelados"
    const val OUTROS = "Outros"

    val all = listOf(
        ACOUGUE, PADARIA, HORTIFRUTI, MERCEARIA, FRIOS,
        BEBIDAS, LIMPEZA, HIGIENE, CONGELADOS, OUTROS
    )
}
