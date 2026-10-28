package com.matheus.gastos.data.entity

import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Encoding
import kotlinx.serialization.Decoding

@Entity(tableName = "categories")
@Serializable
data class Category(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val color: Int, // ARGB color int
    val icon: String, // Material icon name
    val isDefault: Boolean = false,
    val isExpenseDefault: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "subcategories",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
@Serializable
data class Subcategory(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val categoryId: Long,
    val name: String,
    val sortOrder: Int = 0
)

@Entity(tableName = "credit_cards")
@Serializable
data class CreditCard(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val color: Int, // ARGB color int
    val lastFourDigits: String = "",
    val brand: String = "",
    val limitAmount: Double = 0.0,
    val closingDay: Int = 1, // 1-28
    val dueDay: Int = 1, // 1-28
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Subcategory::class,
            parentColumns = ["id"],
            childColumns = ["subcategoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CreditCard::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("date"),
        Index("categoryId"),
        Index("creditCardId"),
        Index("isInstallment"),
        Index("installmentGroupId"),
        Index("ownerId")
    ]
)
@Serializable
data class Transaction(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val amount: Double,
    val description: String,
    val date: Long, // timestamp in milliseconds
    val categoryId: Long?,
    val subcategoryId: Long?,
    val creditCardId: Long?,
    val paymentMethod: PaymentMethod = PaymentMethod.DINHEIRO,
    val isExpense: Boolean = true, // true = expense, false = income
    val isInstallment: Boolean = false,
    val installmentNumber: Int = 1, // 1-based
    val totalInstallments: Int = 1,
    val installmentGroupId: String? = null, // UUID to group installments
    val ownerId: String? = null, // ID of user who created this transaction
    val partnerSynced: Boolean = false, // Whether synced with partner
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_limits")
@Serializable
data class BudgetLimit(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val categoryId: Long,
    val amount: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val isActive: Boolean = true,
    val warningThreshold: Double = 0.8 // 80% warning
) {
    @Transient
    var category: Category? = null
}

@Entity(tableName = "app_settings")
@Serializable
data class AppSettings(
    @PrimaryKey val key: String,
    val value: String
)

enum class PaymentMethod(@Serializable(with = PaymentMethodSerializer::class) val value: String) {
    DINHEIRO("dinheiro"),
    CARTAO("cartao"),
    PIX("pix"),
    DEBITO("debito"),
    BOLETO("boleto"),
    TRANSFERENCIA("transferencia");

    val label: String
        get() = when (this) {
            DINHEIRO -> "Dinheiro"
            CARTAO -> "Cartão"
            PIX -> "Pix"
            DEBITO -> "Débito"
            BOLETO -> "Boleto"
            TRANSFERENCIA -> "Transferência"
        }
}

class PaymentMethodSerializer : kotlinx.serialization.KSerializer<PaymentMethod> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("PaymentMethod", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: PaymentMethod) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): PaymentMethod {
        return PaymentMethod.valueOf(decoder.decodeString().uppercase())
    }
}

enum class BudgetPeriod(@Serializable(with = BudgetPeriodSerializer::class) val value: String) {
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly")
}

class BudgetPeriodSerializer : kotlinx.serialization.KSerializer<BudgetPeriod> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("BudgetPeriod", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: BudgetPeriod) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): BudgetPeriod {
        return BudgetPeriod.valueOf(decoder.decodeString().uppercase())
    }
}

// ==================== NEW ENTITIES FOR SHARING ====================

@Entity(tableName = "connections")
@Serializable
data class Connection(
    @PrimaryKey val id: String, // UUID
    val myUserId: String, // My local user ID
    val partnerUserId: String, // Partner's user ID
    val partnerUsername: String, // Partner's display name
    val inviteCode: String, // 8-char invite code
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val lastSyncedAt: Long = 0
)

enum class RecurringFrequency(@Serializable(with = RecurringFrequencySerializer::class) val value: String) {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly")
}

class RecurringFrequencySerializer : kotlinx.serialization.KSerializer<RecurringFrequency> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("RecurringFrequency", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: RecurringFrequency) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): RecurringFrequency {
        return RecurringFrequency.valueOf(decoder.decodeString().uppercase())
    }
}

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("ownerId"),
        Index("isActive"),
        Index("nextDueDate")
    ]
)
@Serializable
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val description: String,
    val amount: Double,
    val type: String, // "income" or "expense"
    val categoryId: Long?,
    val subcategoryId: Long?,
    val creditCardId: Long?,
    val frequency: RecurringFrequency,
    val dayOfMonth: Int, // 1-31 (monthly/yearly), 1-7 (weekly)
    val ownerId: String, // Who created this
    val startDate: Long, // timestamp
    val endDate: Long? = null, // null = indefinite
    val isActive: Boolean = true,
    val nextDueDate: Long, // Next generation date
    val lastGeneratedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_items",
    indices = [
        Index("ownerId"),
        Index("isChecked")
    ]
)
@Serializable
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val category: String? = null,
    val isChecked: Boolean = false,
    val ownerId: String, // Who added this
    val createdAt: Long = System.currentTimeMillis(),
    val checkedAt: Long? = null,
    val checkedBy: String? = null // Who marked as purchased
)