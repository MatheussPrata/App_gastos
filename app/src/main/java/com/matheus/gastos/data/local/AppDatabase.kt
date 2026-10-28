package com.matheus.gastos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.AppSettings
import com.matheus.gastos.data.entity.Connection
import com.matheus.gastos.data.entity.RecurringTransaction
import com.matheus.gastos.data.entity.ShoppingItem
import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.dao.SubcategoryDao
import com.matheus.gastos.data.dao.CreditCardDao
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.dao.BudgetLimitDao
import com.matheus.gastos.data.dao.AppSettingsDao
import com.matheus.gastos.data.dao.ConnectionDao
import com.matheus.gastos.data.dao.RecurringTransactionDao
import com.matheus.gastos.data.dao.ShoppingItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Category::class,
        Subcategory::class,
        CreditCard::class,
        Transaction::class,
        BudgetLimit::class,
        AppSettings::class,
        Connection::class,
        RecurringTransaction::class,
        ShoppingItem::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun subcategoryDao(): SubcategoryDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetLimitDao(): BudgetLimitDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gastos.db"
                )
                    .addCallback(SeedDatabaseCallback)
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val SeedDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(db)
                }
            }
        }

        private suspend fun seedDatabase(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            // Default categories with colors and icons
            val defaultCategories = listOf(
                Category(0, "Alimentação", 0xFFFF6B6B, "restaurant", true, 0),
                Category(0, "Transporte", 0xFF4ECDC4, "directions_bus", true, 1),
                Category(0, "Compras", 0xFFFFD93D, "shopping_cart", true, 2),
                Category(0, "Lazer", 0xFF6BCB77, "movie", true, 3),
                Category(0, "Saúde", 0xFFFF8E8E, "local_hospital", true, 4),
                Category(0, "Educação", 0xFFA8E6CF, "school", true, 5),
                Category(0, "Contas", 0xFFFFB347, "receipt", true, 6),
                Category(0, "Outros", 0xFFB8B8B8, "category", true, 7)
            )

            // Subcategories for each default category
            val subcategoriesMap = mapOf(
                "Alimentação" to listOf("Restaurante", "Mercado", "Delivery", "Café", "Lanche"),
                "Transporte" to listOf("Combustível", "Uber/99", "Ônibus/Metrô", "Manutenção", "Estacionamento"),
                "Compras" to listOf("Roupas", "Eletrônicos", "Casa", "Presentes", "Outros"),
                "Lazer" to listOf("Cinema", "Shows", "Jogos", "Viagem", "Hobby"),
                "Saúde" to listOf("Médico", "Farmácia", "Exames", "Plano de Saúde", "Terapia"),
                "Educação" to listOf("Cursos", "Livros", "Certificações", "Idiomas", "Material"),
                "Contas" to listOf("Aluguel", "Energia", "Água", "Internet", "Celular", "Streaming"),
                "Outros" to listOf("Diversos")
            )

            // Insert categories and get their IDs
            val categoryIds = mutableMapOf<String, Long>()
            
            for ((index, category) in defaultCategories.withIndex()) {
                val sql = """
                    INSERT INTO categories (name, color, icon, isDefault, sortOrder)
                    VALUES ('${category.name}', ${category.color}, '${category.icon}', 1, ${category.sortOrder})
                """.trimIndent()
                db.execSQL(sql)
                
                // Get the inserted ID
                val cursor = db.query("SELECT last_insert_rowid()")
                cursor.moveToFirst()
                val id = cursor.getLong(0)
                cursor.close()
                categoryIds[category.name] = id
            }

            // Insert subcategories
            for ((categoryName, subcats) in subcategoriesMap) {
                val categoryId = categoryIds[categoryName] ?: continue
                for ((subIndex, subName) in subcats.withIndex()) {
                    val sql = """
                        INSERT INTO subcategories (categoryId, name, sortOrder)
                        VALUES ($categoryId, '$subName', $subIndex)
                    """.trimIndent()
                    db.execSQL(sql)
                }
            }
        }

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add ownerId and partnerSynced columns to transactions table
                database.execSQL("ALTER TABLE transactions ADD COLUMN ownerId TEXT")
                database.execSQL("ALTER TABLE transactions ADD COLUMN partnerSynced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_ownerId ON transactions(ownerId)")

                // Create connections table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS connections (
                        id TEXT PRIMARY KEY NOT NULL,
                        myUserId TEXT NOT NULL,
                        partnerUserId TEXT NOT NULL,
                        partnerUsername TEXT NOT NULL,
                        inviteCode TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        lastSyncedAt INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // Create recurring_transactions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS recurring_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        description TEXT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        categoryId INTEGER,
                        subcategoryId INTEGER,
                        creditCardId INTEGER,
                        frequency TEXT NOT NULL,
                        dayOfMonth INTEGER NOT NULL,
                        ownerId TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        nextDueDate INTEGER NOT NULL,
                        lastGeneratedAt INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL
                    )
                """)

                // Create shopping_items table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS shopping_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT,
                        isChecked INTEGER NOT NULL DEFAULT 0,
                        ownerId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        checkedAt INTEGER,
                        checkedBy TEXT
                    )
                """)

                // Create indexes for new tables
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_ownerId ON recurring_transactions(ownerId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_isActive ON recurring_transactions(isActive)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_nextDueDate ON recurring_transactions(nextDueDate)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_ownerId ON shopping_items(ownerId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_items_isChecked ON shopping_items(isChecked)")
            }
        }
    }
}

