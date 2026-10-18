package com.matheus.gastos.data.repository

import com.matheus.gastos.data.dao.CategoryDao
import com.matheus.gastos.data.dao.SubcategoryDao
import com.matheus.gastos.data.dao.CreditCardDao
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.dao.MonthlyBalance
import com.matheus.gastos.data.dao.BudgetLimitDao
import com.matheus.gastos.data.dao.AppSettingsDao
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.AppSettings
import com.matheus.gastos.data.entity.BudgetPeriod
import com.matheus.gastos.data.entity.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Início do período corrente (dia/semana/mês/ano) em epoch millis, à meia-noite.
 * Usado para saber quanto já foi gasto dentro do ciclo do limite.
 */
internal fun budgetPeriodStartMillis(period: BudgetPeriod, now: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        when (period) {
            BudgetPeriod.DAILY -> Unit
            BudgetPeriod.WEEKLY -> set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            BudgetPeriod.MONTHLY -> set(Calendar.DAY_OF_MONTH, 1)
            BudgetPeriod.YEARLY -> set(Calendar.DAY_OF_YEAR, 1)
        }
    }
    return cal.timeInMillis
}

/** Fim (inclusive) do período corrente em epoch millis. */
internal fun budgetPeriodEndMillis(period: BudgetPeriod, now: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = budgetPeriodStartMillis(period, now)
        when (period) {
            BudgetPeriod.DAILY -> add(Calendar.DAY_OF_MONTH, 1)
            BudgetPeriod.WEEKLY -> add(Calendar.WEEK_OF_YEAR, 1)
            BudgetPeriod.MONTHLY -> add(Calendar.MONTH, 1)
            BudgetPeriod.YEARLY -> add(Calendar.YEAR, 1)
        }
        add(Calendar.MILLISECOND, -1)
    }
    return cal.timeInMillis
}

class CategoryRepository(private val dao: CategoryDao) {
    suspend fun insert(category: Category) = dao.insert(category)
    suspend fun update(category: Category) = dao.update(category)
    suspend fun delete(category: Category) = dao.delete(category)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun getAll(): Flow<List<Category>> = dao.getAll()
    fun getDefaults(): Flow<List<Category>> = dao.getDefaults()
    fun getCustom(): Flow<List<Category>> = dao.getCustom()
    fun getByType(isExpense: Boolean): Flow<List<Category>> = dao.getByType(isExpense)
    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun count(): Int = dao.count()

    /** Lê as categorias uma única vez (fora de Flow), útil para seeds síncronos. */
    suspend fun getAllOnce(): List<Category> = dao.getAllOnce()

    /**
     * Garante que as categorias padrão existam, mesmo em bancos antigos/parciais.
     *
     * É IDEMPOTENTE: insere apenas os pares (nome, tipo) que ainda não existem.
     * Antes abortava quando o banco tinha qualquer categoria, o que impedia o seed
     * em instalações atualizadas (usuário ficava só com categorias vazias).
     *
     * Categorias de GASTO têm [Category.isExpenseDefault] = true e as de ENTRADA false.
     */
    suspend fun seedDefaultsIfEmpty() {
        val existing = dao.getAllOnce()
        val existingKeys = existing.map { it.name.trim().lowercase() to it.isExpenseDefault }.toSet()
        val missing = defaultCategories().filter {
            (it.name.trim().lowercase() to it.isExpenseDefault) !in existingKeys
        }
        if (missing.isNotEmpty()) {
            dao.insertAll(missing)
        }
    }

    companion object {
        fun defaultCategories(): List<Category> {
            val expenses = listOf(
                "Alimentação" to "restaurant",
                "Transporte" to "directions_car",
                "Moradia" to "home",
                "Saúde" to "local_hospital",
                "Lazer" to "sports_esports",
                "Compras" to "shopping_bag",
                "Contas" to "receipt_long",
                "Educação" to "school",
                "Outros" to "category"
            )
            val incomes = listOf(
                "Salário" to "payments",
                "Freelance" to "work",
                "Investimentos" to "trending_up",
                "Presente" to "card_giftcard",
                "Reembolso" to "undo",
                "Outros" to "category"
            )
            val expenseCategories = expenses.mapIndexed { index, (name, icon) ->
                Category(
                    name = name,
                    icon = icon,
                    isDefault = true,
                    isExpenseDefault = true,
                    sortOrder = index
                )
            }
            val incomeCategories = incomes.mapIndexed { index, (name, icon) ->
                Category(
                    name = name,
                    icon = icon,
                    isDefault = true,
                    isExpenseDefault = false,
                    sortOrder = index
                )
            }
            return expenseCategories + incomeCategories
        }
    }
}

class SubcategoryRepository(private val dao: SubcategoryDao) {
    suspend fun insert(subcategory: Subcategory) = dao.insert(subcategory)
    suspend fun update(subcategory: Subcategory) = dao.update(subcategory)
    suspend fun delete(subcategory: Subcategory) = dao.delete(subcategory)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun deleteByCategoryId(categoryId: Long) = dao.deleteByCategoryId(categoryId)
    fun getByCategoryId(categoryId: Long): Flow<List<Subcategory>> = dao.getByCategoryId(categoryId)
    suspend fun getById(id: Long) = dao.getById(id)
}

class CreditCardRepository(private val dao: CreditCardDao) {
    suspend fun insert(card: CreditCard) = dao.insert(card)
    suspend fun update(card: CreditCard) = dao.update(card)
    suspend fun delete(card: CreditCard) = dao.delete(card)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun getActive(): Flow<List<CreditCard>> = dao.getActive()
    fun getAll(): Flow<List<CreditCard>> = dao.getAll()
    suspend fun getById(id: Long) = dao.getById(id)
    /**
     * Cria dois cartões de exemplo quando ainda não existe nenhum, para testar
     * a tela de cartões e o relatório "Por cartão" sem cadastro manual.
     * Idempotente: se já houver qualquer cartão, não faz nada.
     */
    suspend fun seedDemoCardsIfEmpty() {
        if (dao.getAllOnce().isNotEmpty()) return

        dao.insertAll(
            listOf(
                CreditCard(
                    name = "Cartão Principal",
                    lastFourDigits = "1234",
                    brand = "VISA",
                    color = 0xFF1E88E5.toInt(),
                    limitAmount = 3000.0,
                    closingDay = 5,
                    dueDay = 15,
                    sortOrder = 0
                ),
                CreditCard(
                    name = "Cartão Secundário",
                    lastFourDigits = "5678",
                    brand = "MASTERCARD",
                    color = 0xFF43A047.toInt(),
                    limitAmount = 1500.0,
                    closingDay = 20,
                    dueDay = 28,
                    sortOrder = 1
                )
            )
        )
    }
}

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun insert(transaction: Transaction) = dao.insert(transaction)
    suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions)
    suspend fun update(transaction: Transaction) = dao.update(transaction)
    suspend fun delete(transaction: Transaction) = dao.delete(transaction)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun deleteByInstallmentGroup(groupId: String) = dao.deleteByInstallmentGroup(groupId)
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByDateRange(startDate, endDate)
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByCategoryAndDateRange(categoryId, startDate, endDate)
    fun getByCardAndDateRange(cardId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = dao.getByCardAndDateRange(cardId, startDate, endDate)
    fun getInstallmentsByGroup(groupId: String): Flow<List<Transaction>> = dao.getInstallmentsByGroup(groupId)
    fun getFutureInstallments(currentDate: Long): Flow<List<Transaction>> = dao.getFutureInstallments(currentDate)
    fun getTotalExpenses(startDate: Long, endDate: Long): Flow<Double?> = dao.getTotalExpenses(startDate, endDate)
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double?> = dao.getTotalIncome(startDate, endDate)
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<TransactionDao.CategoryExpense>> = dao.getExpensesByCategory(startDate, endDate)
    fun getExpensesByCard(startDate: Long, endDate: Long): Flow<List<TransactionDao.CardExpense>> = dao.getExpensesByCard(startDate, endDate)

    fun getExpensesByCategoryForCard(startDate: Long, endDate: Long, cardId: Long): Flow<List<TransactionDao.CategoryExpense>> =
        dao.getExpensesByCategoryForCard(startDate, endDate, cardId)
    suspend fun getById(id: Long) = dao.getById(id)

    companion object {
        fun getMonthBounds(date: Date): Pair<Long, Long> {            val cal = java.util.Calendar.getInstance()
            cal.time = date
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(java.util.Calendar.MONTH, 1)
            cal.add(java.util.Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis
            return start to end
        }

        /**
         * Divide [total] em [count] parcelas em centavos, sem perder nem sobrar 1 centavo.
         *
         * Ex.: 1000.0 em 10x -> 10 x 100.0
         *      100.0 em 3x  -> 33.33, 33.33, 33.34 (a ultima absorve a sobra)
         *
         * Entradas invalidas (count <= 0) devolvem lista vazia. total <= 0 gera zeros.
         */
        fun splitInstallmentAmounts(total: Double, count: Int): List<Double> {
            if (count <= 0) return emptyList()
            // Trabalha em centavos inteiros para evitar erro de ponto flutuante.
            val totalCents = Math.round(total * 100.0)
            val base = totalCents / count
            val remainder = totalCents - base * count

            return (1..count).map { index ->
                val cents = if (index == count) base + remainder else base
                cents / 100.0
            }
        }
    }

    suspend fun createInstallments(
        baseTransaction: Transaction,
        totalInstallments: Int,
        categoryId: Long?,
        subcategoryId: Long?,
        creditCardId: Long?
    ) {
        val groupId = UUID.randomUUID().toString()
        val installments = mutableListOf<Transaction>()
        val amounts = splitInstallmentAmounts(baseTransaction.amount, totalInstallments)

        for (i in 1..totalInstallments) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = baseTransaction.date
            // Avanca mes a mes a partir da data da compra.
            cal.add(java.util.Calendar.MONTH, i - 1)

            val installment = baseTransaction.copy(
                id = 0,
                amount = amounts[i - 1],
                installmentNumber = i,
                totalInstallments = totalInstallments,
                installmentGroupId = groupId,
                isInstallment = totalInstallments > 1,
                date = cal.timeInMillis,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            installments.add(installment)
        }
        dao.insertAll(installments)
    }

    /**
     * Popula lançamentos de demonstração APENAS quando não há nenhuma transação.
     *
     * É IDEMPOTENTE: se já existir qualquer transação, não faz nada.
     * Deve ser chamado depois de [CategoryRepository.seedDefaultsIfEmpty] e com as
     * categorias já carregadas, para não depender de novas dependências no construtor.
     */
    suspend fun seedDemoIfEmpty(categories: List<Category>) {
        if (dao.countAll() > 0) return

        val now = System.currentTimeMillis()
        val expenseCategories = categories.filter { it.isExpenseDefault }
        val incomeCategories = categories.filter { !it.isExpenseDefault }

        val salaryCategory = incomeCategories.firstOrNull {
            it.name.trim().equals("Salário", ignoreCase = true)
        } ?: incomeCategories.firstOrNull()
        val housingCategory = expenseCategories.firstOrNull {
            it.name.trim().equals("Moradia", ignoreCase = true)
        } ?: expenseCategories.firstOrNull()
        val leisureCategory = expenseCategories.firstOrNull {
            it.name.trim().equals("Lazer", ignoreCase = true)
        } ?: expenseCategories.firstOrNull()

        // 1. Salário (entrada)
        insert(
            Transaction(
                description = "Salário",
                amount = 2300.0,
                date = now,
                isExpense = false,
                categoryId = salaryCategory?.id,
                paymentMethod = PaymentMethod.DINHEIRO,
                isInstallment = false,
                installmentNumber = 1,
                totalInstallments = 1
            )
        )

        // 2. Casa (gasto)
        insert(
            Transaction(
                description = "Casa",
                amount = 900.0,
                date = now,
                isExpense = true,
                categoryId = housingCategory?.id,
                paymentMethod = PaymentMethod.DINHEIRO,
                isInstallment = false,
                installmentNumber = 1,
                totalInstallments = 1
            )
        )

        // 3. Drone (gasto parcelado em 6x -> 6 x R$ 100,00)
        createInstallments(
            baseTransaction = Transaction(
                description = "Drone",
                amount = 600.0,
                date = now,
                isExpense = true,
                categoryId = leisureCategory?.id,
                paymentMethod = PaymentMethod.DINHEIRO
            ),
            totalInstallments = 6,
            categoryId = leisureCategory?.id,
            subcategoryId = null,
            creditCardId = null
        )
    }

    fun getMonthlyBalances(): Flow<List<MonthlyBalance>> = dao.getMonthlyBalances()

    fun getMonthlyBalancesInRange(startDate: Long, endDate: Long): Flow<List<MonthlyBalance>> = dao.getMonthlyBalancesInRange(startDate, endDate)

    fun getTransactionsWithRunningBalance(startDate: Long, endDate: Long): Flow<List<Pair<Transaction, Double>>> {
        return dao.getByDateRange(startDate, endDate).map { transactions ->
            val sorted = transactions.sortedBy { it.date }
            var runningBalance = 0.0
            sorted.map { tx ->
                runningBalance += if (tx.isExpense) -tx.amount else tx.amount
                tx to runningBalance
            }.reversed() // back to DESC order for UI
        }
    }
}

class BudgetLimitRepository(
    private val dao: BudgetLimitDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    suspend fun insert(limit: BudgetLimit) = dao.insert(limit)
    suspend fun update(limit: BudgetLimit) = dao.update(limit)
    suspend fun delete(limit: BudgetLimit) = dao.delete(limit)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    fun getActive(): Flow<List<BudgetLimit>> = dao.getActive()
    fun getByCategory(categoryId: Long): Flow<List<BudgetLimit>> = dao.getByCategory(categoryId)
    suspend fun getById(id: Long) = dao.getById(id)

    /** Limite ja estourado em um mes futuro (ex.: parcelas agendadas para o mes que vem). */
    data class NextMonthLimitAlert(
        val categoryName: String,
        val limitAmount: Double,
        val spentAmount: Double
    )

    /**
     * Limites mensais cujo gasto JA AGENDADO para o mes seguinte passa do limite.
     *
     * Serve para avisar na tela inicial que o proximo mes ja nasce estourado —
     * hoje isso só aparecia se o usuario abrisse a tela de limites.
     */
    fun nextMonthOverLimit(): Flow<List<NextMonthLimitAlert>> = dao.getActive().map { limits ->
        if (limits.isEmpty()) return@map emptyList()

        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.timeInMillis

        val categories = categoryDao.getAllOnce().associateBy { it.id }
        val byCategory = transactionDao.getExpensesByCategory(start, end).firstOrNull().orEmpty()
            .associate { it.categoryId to it.total }

        limits.mapNotNull { limit ->
            if (limit.period != BudgetPeriod.MONTHLY) return@mapNotNull null
            val spent = byCategory[limit.categoryId] ?: 0.0
            if (limit.limitAmount > 0.0 && spent > limit.limitAmount) {
                NextMonthLimitAlert(
                    categoryName = categories[limit.categoryId]?.name ?: "Categoria ${limit.categoryId}",
                    limitAmount = limit.limitAmount,
                    spentAmount = spent
                )
            } else {
                null
            }
        }
    }

    data class BudgetLimitWithCategory(
        val budgetLimit: BudgetLimit,
        val categoryName: String?,
        val categoryColor: Int,
        /** Gasto real da categoria dentro do período do limite. */
        val spentAmount: Double
    )

    /**
     * Limites ativos com o nome/cor da categoria e o gasto REAL do período.
     *
     * Antes o mapeamento devolvia `null`/`0` sempre e ninguém calculava o gasto,
     * por isso o limite nunca contava transação nenhuma (nem nova nem antiga).
     */
    fun getActiveWithCategories(): Flow<List<BudgetLimitWithCategory>> {
        return dao.getActive().map { limits ->
            if (limits.isEmpty()) return@map emptyList()

            val categories = categoryDao.getAllOnce().associateBy { it.id }
            val now = System.currentTimeMillis()

            limits.map { limit ->
                val category = categories[limit.categoryId]
                val start = budgetPeriodStartMillis(limit.period, now)
                val end = budgetPeriodEndMillis(limit.period, now)
                val spent = transactionDao
                    .getExpensesByCategory(start, end)
                    .firstOrNull()
                    ?.firstOrNull { it.categoryId == limit.categoryId }
                    ?.total
                    ?: 0.0

                BudgetLimitWithCategory(
                    budgetLimit = limit,
                    categoryName = category?.name,
                    categoryColor = category?.color ?: 0xFF6750A4.toInt(),
                    spentAmount = spent
                )
            }
        }
    }

    /**
     * Gasto real de cada limite, consultado sob demanda pela tela.
     * Separado do fluxo de limites para não recalcular tudo a cada emissão.
     */
    suspend fun spentFor(limit: BudgetLimit, now: Long = System.currentTimeMillis()): Double {
        val start = budgetPeriodStartMillis(limit.period, now)
        val end = budgetPeriodEndMillis(limit.period, now)
        return transactionDao
            .getExpensesByCategory(start, end)
            .firstOrNull()
            ?.firstOrNull { it.categoryId == limit.categoryId }
            ?.total
            ?: 0.0
    }
}

class SettingsRepository(private val dao: AppSettingsDao) {
    suspend fun putString(key: String, value: String) = dao.insert(AppSettings(key, value))
    suspend fun putInt(key: String, value: Int) = dao.insert(AppSettings(key, value.toString()))
    suspend fun putLong(key: String, value: Long) = dao.insert(AppSettings(key, value.toString()))
    suspend fun putBoolean(key: String, value: Boolean) = dao.insert(AppSettings(key, value.toString()))

    suspend fun getString(key: String, default: String): String = dao.getValue(key) ?: default
    suspend fun getInt(key: String, default: Int): Int = dao.getValue(key)?.toIntOrNull() ?: default
    suspend fun getLong(key: String, default: Long): Long = dao.getValue(key)?.toLongOrNull() ?: default
    suspend fun getBoolean(key: String, default: Boolean): Boolean = dao.getValue(key)?.toBoolean() ?: default
}
