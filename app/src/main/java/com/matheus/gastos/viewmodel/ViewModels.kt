package com.matheus.gastos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.repository.CategoryRepository
import com.matheus.gastos.data.repository.CreditCardRepository
import com.matheus.gastos.data.repository.TransactionRepository
import com.matheus.gastos.data.repository.BudgetLimitRepository
import com.matheus.gastos.data.repository.SettingsRepository
import com.matheus.gastos.data.dao.MonthlyBalance
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.ui.theme.AppearanceState
import com.matheus.gastos.util.addMonths
import com.matheus.gastos.util.isCurrentMonth
import com.matheus.gastos.util.monthStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(private val repo: CategoryRepository) : ViewModel() {
    val categories = repo.getAll().asLiveData(viewModelScope.coroutineContext)
    val defaultCategories = repo.getDefaults().asLiveData(viewModelScope.coroutineContext)
    val customCategories = repo.getCustom().asLiveData(viewModelScope.coroutineContext)

    suspend fun insert(category: Category) = repo.insert(category)
    suspend fun update(category: Category) = repo.update(category)
    suspend fun delete(category: Category) = repo.delete(category)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    suspend fun getById(id: Long) = repo.getById(id)
    suspend fun seedDefaultsIfEmpty() = repo.seedDefaultsIfEmpty()
}

@HiltViewModel
class CreditCardViewModel @Inject constructor(private val repo: CreditCardRepository) : ViewModel() {
    val activeCards = repo.getActive().asLiveData(viewModelScope.coroutineContext)
    val allCards = repo.getAll().asLiveData(viewModelScope.coroutineContext)

    suspend fun insert(card: CreditCard) = repo.insert(card)
    suspend fun update(card: CreditCard) = repo.update(card)
    suspend fun delete(card: CreditCard) = repo.delete(card)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    suspend fun getById(id: Long) = repo.getById(id)
}

@HiltViewModel
class TransactionViewModel @Inject constructor(private val repo: TransactionRepository) : ViewModel() {
    private val _selectedMonth = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedMonth: LiveData<Long> = _selectedMonth

    fun setSelectedMonth(timestamp: Long) {
        val month = monthStart(timestamp)
        if (_selectedMonth.value != month) {
            _selectedMonth.value = month
        }
    }

    /** Move a seleção [delta] meses (negativo = passado) a partir do mês selecionado. */
    fun shiftSelectedMonth(delta: Int) {
        val base = _selectedMonth.value ?: System.currentTimeMillis()
        setSelectedMonth(addMonths(base, delta))
    }

    /**
     * Se o mês selecionado deixou de ser o mês corrente (ex.: virou o dia 1),
     * seleciona o novo mês atual. Retorna true quando houve mudança.
     */
    fun refreshToCurrentMonthIfNeeded(): Boolean {
        val selected = _selectedMonth.value ?: return false
        if (!isCurrentMonth(selected)) {
            setSelectedMonth(System.currentTimeMillis())
            return true
        }
        return false
    }

    val currentMonthTransactions: LiveData<List<Transaction>> = _selectedMonth
        .asFlow()
        .distinctUntilChanged()
        .flatMapLatest { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getByDateRange(start, end)
        }
        .asLiveData(viewModelScope.coroutineContext)

    val totalExpenses: LiveData<Double> = _selectedMonth
        .asFlow()
        .distinctUntilChanged()
        .flatMapLatest { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getTotalExpenses(start, end)
        }
        .map { it: Double? -> it ?: 0.0 }
        .asLiveData(viewModelScope.coroutineContext)

    val totalIncome: LiveData<Double> = _selectedMonth
        .asFlow()
        .distinctUntilChanged()
        .flatMapLatest { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getTotalIncome(start, end)
        }
        .map { it: Double? -> it ?: 0.0 }
        .asLiveData(viewModelScope.coroutineContext)

    val balance: LiveData<Double> = combine(
        totalIncome.asFlow(),
        totalExpenses.asFlow()
    ) { income, expense ->
        income - expense
    }.asLiveData(viewModelScope.coroutineContext)

    val expensesByCategory: LiveData<List<TransactionDao.CategoryExpense>> = _selectedMonth
        .asFlow()
        .distinctUntilChanged()
        .flatMapLatest { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getExpensesByCategory(start, end)
        }
        .asLiveData(viewModelScope.coroutineContext)

    val expensesByCard: LiveData<List<TransactionDao.CardExpense>> = _selectedMonth
        .asFlow()
        .distinctUntilChanged()
        .flatMapLatest { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getExpensesByCard(start, end)
        }
        .asLiveData(viewModelScope.coroutineContext)

    /** Cartao selecionado no relatorio "Por cartao"; null = todos os cartoes. */
    private val _cardFilter = MutableStateFlow<Long?>(null)

    fun setCardFilter(cardId: Long?) {
        _cardFilter.value = cardId
    }

    /**
     * Gastos POR CATEGORIA de um cartao especifico (filtro do relatorio "Por cartao").
     * Reemite quando o mes ou o cartao selecionado muda.
     */
    val expensesByCategoryForCard: LiveData<List<TransactionDao.CategoryExpense>> =
        combine(
            _selectedMonth.asFlow(),
            _cardFilter
        ) { month, cardId -> month to cardId }
            .flatMapLatest { (month, cardId) ->
                if (cardId == null) {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                } else {
                    val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
                    repo.getExpensesByCategoryForCard(start, end, cardId)
                }
            }
            .asLiveData(viewModelScope.coroutineContext)

    suspend fun insert(transaction: Transaction) = repo.insert(transaction)
    suspend fun insertAll(transactions: List<Transaction>) = repo.insertAll(transactions)
    suspend fun update(transaction: Transaction) = repo.update(transaction)
    suspend fun delete(transaction: Transaction) = repo.delete(transaction)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    suspend fun deleteByInstallmentGroup(groupId: String) = repo.deleteByInstallmentGroup(groupId)
    suspend fun getById(id: Long) = repo.getById(id)

    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = repo.getByDateRange(startDate, endDate)
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = repo.getByCategoryAndDateRange(categoryId, startDate, endDate)
    fun getByCardAndDateRange(cardId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = repo.getByCardAndDateRange(cardId, startDate, endDate)
    fun getFutureInstallments(currentDate: Long): Flow<List<Transaction>> = repo.getFutureInstallments(currentDate)
    fun getInstallmentsByGroup(groupId: String): Flow<List<Transaction>> = repo.getInstallmentsByGroup(groupId)

    suspend fun createInstallments(
        baseTransaction: Transaction,
        totalInstallments: Int,
        categoryId: Long?,
        subcategoryId: Long?,
        creditCardId: Long?
    ) {
        repo.createInstallments(baseTransaction, totalInstallments, categoryId, subcategoryId, creditCardId)
    }

    val monthlyBalances: LiveData<List<MonthlyBalance>> = _selectedMonth
        .asFlow()
        .distinctUntilChanged()
        .flatMapLatest { repo.getMonthlyBalances() ?: kotlinx.coroutines.flow.flowOf(emptyList()) }
        .asLiveData(viewModelScope.coroutineContext)

    val currentMonthTransactionsWithBalance: LiveData<List<Pair<Transaction, Double>>> = _selectedMonth
        .asFlow()
        .distinctUntilChanged()
        .flatMapLatest { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getTransactionsWithRunningBalance(start, end) ?: kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .asLiveData(viewModelScope.coroutineContext)

    companion object {
        /**
         * Converte o texto digitado ("10", "10,50", "10.50", "1.234,56") em Double.
         * Retorna null quando o valor é inválido ou não positivo.
         */
        fun parseAmount(input: String): Double? {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return null

            val normalized = when {
                trimmed.contains(',') && trimmed.contains('.') ->
                    // "1.234,56" -> "1234.56"
                    trimmed.replace(".", "").replace(',', '.')
                trimmed.contains(',') -> trimmed.replace(',', '.')
                else -> trimmed
            }

            val value = normalized.toDoubleOrNull() ?: return null
            return if (value > 0.0) value else null
        }
    }
}

@HiltViewModel
class BudgetLimitViewModel @Inject constructor(private val repo: BudgetLimitRepository) : ViewModel() {
    val activeLimits = repo.getActiveWithCategories().asLiveData(viewModelScope.coroutineContext)

    suspend fun insert(limit: BudgetLimit) = repo.insert(limit)
    suspend fun update(limit: BudgetLimit) = repo.update(limit)
    suspend fun delete(limit: BudgetLimit) = repo.delete(limit)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    suspend fun getById(id: Long) = repo.getById(id)
}

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: SettingsRepository) : ViewModel() {
    private val _themeMode = MutableLiveData<Int>(0)
    val themeMode: LiveData<Int> = _themeMode

    private val _accentColor = MutableLiveData<Int>(0xFF6750A4.toInt())
    val accentColor: LiveData<Int> = _accentColor

    private val _notificationsEnabled = MutableLiveData<Boolean>(true)
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val mode = repo.getInt("theme_mode", 0)
            val color = repo.getInt("accent_color", 0xFF6750A4.toInt())
            _themeMode.value = mode
            _accentColor.value = color
            AppearanceState.apply(mode, color)
            _notificationsEnabled.value = repo.getBoolean("notifications_enabled", true)
        }
    }

    suspend fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        AppearanceState.updateThemeMode(mode)
        repo.putInt("theme_mode", mode)
    }

    suspend fun setAccentColor(color: Int) {
        _accentColor.value = color
        AppearanceState.updateAccentColor(color)
        repo.putInt("accent_color", color)
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        repo.putBoolean("notifications_enabled", enabled)
    }

    suspend fun setLastBackupTime(time: Long) = repo.putLong("last_backup_time", time)
    suspend fun getLastBackupTime(): Long = repo.getLong("last_backup_time", 0)
}
