package com.matheus.gastos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.Connection
import com.matheus.gastos.data.entity.RecurringTransaction
import com.matheus.gastos.data.entity.RecurringFrequency
import com.matheus.gastos.data.entity.ShoppingItem
import com.matheus.gastos.data.repository.CategoryRepository
import com.matheus.gastos.data.repository.CreditCardRepository
import com.matheus.gastos.data.repository.TransactionRepository
import com.matheus.gastos.data.repository.BudgetLimitRepository
import com.matheus.gastos.data.repository.SettingsRepository
import com.matheus.gastos.data.repository.ConnectionRepository
import com.matheus.gastos.data.repository.RecurringTransactionRepository
import com.matheus.gastos.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sharingStarted

class CategoryViewModel(application: Application, private val repo: CategoryRepository) : AndroidViewModel(application) {
    val categories: LiveData<List<Category>> = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val defaultCategories: LiveData<List<Category>> = repo.getDefaults().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val customCategories: LiveData<List<Category>> = repo.getCustom().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    suspend fun insert(category: Category) = repo.insert(category)
    suspend fun update(category: Category) = repo.update(category)
    suspend fun delete(category: Category) = repo.delete(category)
    suspend fun deleteById(id: Long) = repo.deleteById(id)

    // Subcategories
    fun getSubcategories(categoryId: Long): Flow<List<Subcategory>> = repo.getSubcategories(categoryId)
    suspend fun insertSubcategory(sub: Subcategory) = repo.insertSubcategory(sub)
    suspend fun updateSubcategory(sub: Subcategory) = repo.updateSubcategory(sub)
    suspend fun deleteSubcategory(sub: Subcategory) = repo.deleteSubcategory(sub)
    suspend fun deleteSubcategoryById(id: Long) = repo.deleteSubcategoryById(id)
    suspend fun deleteByCategoryId(categoryId: Long) = repo.deleteByCategoryId(categoryId)
    suspend fun getSubcategoryById(id: Long) = repo.getSubcategoryById(id)
}

class CreditCardViewModel(application: Application, private val repo: CreditCardRepository) : AndroidViewModel(application) {
    val activeCards: LiveData<List<CreditCard>> = repo.getActive().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val allCards: LiveData<List<CreditCard>> = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    suspend fun insert(card: CreditCard) = repo.insert(card)
    suspend fun update(card: CreditCard) = repo.update(card)
    suspend fun delete(card: CreditCard) = repo.delete(card)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    suspend fun getById(id: Long) = repo.getById(id)
}

class TransactionViewModel(application: Application, private val repo: TransactionRepository) : AndroidViewModel(application) {
    private val _selectedMonth = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedMonth: LiveData<Long> = _selectedMonth

    fun setSelectedMonth(timestamp: Long) {
        _selectedMonth.value = timestamp
    }

    val currentMonthTransactions: LiveData<List<Transaction>> = kotlinx.coroutines.flow
        .combine(
            selectedMonth.asFlow().distinctUntilChanged(),
            repo.getByDateRange(0, Long.MAX_VALUE) // We'll filter in the combine
        ) { month, allTransactions ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            allTransactions.filter { it.date >= start && it.date <= end }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val totalExpenses: LiveData<Double> = kotlinx.coroutines.flow
        .combine(selectedMonth.asFlow().distinctUntilChanged()) { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getTotalExpenses(start, end)
        }
        .flattenMerge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val totalIncome: LiveData<Double> = kotlinx.coroutines.flow
        .combine(selectedMonth.asFlow().distinctUntilChanged()) { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getTotalIncome(start, end)
        }
        .flattenMerge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val balance: LiveData<Double> = kotlinx.coroutines.flow
        .combine(totalIncome.asFlow(), totalExpenses.asFlow()) { income, expense -> income - expense }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val expensesByCategory: LiveData<List<TransactionRepository.CategoryExpense>> = kotlinx.coroutines.flow
        .combine(selectedMonth.asFlow().distinctUntilChanged()) { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getExpensesByCategory(start, end)
        }
        .flattenMerge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val expensesByCard: LiveData<List<TransactionRepository.CardExpense>> = kotlinx.coroutines.flow
        .combine(selectedMonth.asFlow().distinctUntilChanged()) { month ->
            val (start, end) = TransactionRepository.getMonthBounds(java.util.Date(month))
            repo.getExpensesByCard(start, end)
        }
        .flattenMerge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

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

    // Helper to create installments
    suspend fun createInstallments(
        baseTransaction: Transaction,
        totalInstallments: Int,
        categoryId: Long?,
        subcategoryId: Long?,
        creditCardId: Long?
    ) {
        val groupId = UUID.randomUUID().toString()
        val installments = mutableListOf<Transaction>()
        
        for (i in 1..totalInstallments) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = baseTransaction.date
            cal.add(java.util.Calendar.MONTH, i - 1)
            
            val installment = baseTransaction.copy(
                id = 0,
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
        repo.insertAll(installments)
    }
}

class BudgetLimitViewModel(application: Application, private val repo: BudgetLimitRepository) : AndroidViewModel(application) {
    val activeLimits: LiveData<List<BudgetLimitRepository.BudgetLimitWithCategory>> = 
        repo.getActiveWithCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    suspend fun insert(limit: BudgetLimit) = repo.insert(limit)
    suspend fun update(limit: BudgetLimit) = repo.update(limit)
    suspend fun delete(limit: BudgetLimit) = repo.delete(limit)
    suspend fun deleteById(id: Long) = repo.deleteById(id)
    fun getByCategory(categoryId: Long): Flow<List<BudgetLimit>> = repo.getByCategory(categoryId)
    suspend fun getById(id: Long) = repo.getById(id)
}

class SettingsViewModel(application: Application, private val repo: SettingsRepository) : AndroidViewModel(application) {
    // Theme settings
    var themeMode: Int by mutableStateOf(0) // 0=system, 1=light, 2=dark
        private set
    
    var accentColor: Int by mutableStateOf(0xFF6750A4) // Default Material purple
        private set

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            themeMode = repo.getInt("theme_mode", 0)
            val colorInt = repo.getInt("accent_color", 0xFF6750A4)
            accentColor = colorInt
        }
    }

    suspend fun setThemeMode(mode: Int) {
        themeMode = mode
        repo.putInt("theme_mode", mode)
    }

    suspend fun setAccentColor(color: Int) {
        accentColor = color
        repo.putInt("accent_color", color)
    }

    // Notifications
    var notificationsEnabled: Boolean by mutableStateOf(true)
        private set

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled = enabled
        repo.putBoolean("notifications_enabled", enabled)
    }

    // Backup
    suspend fun setLastBackupTime(time: Long) = repo.putLong("last_backup_time", time)
    fun getLastBackupTime(): Long = repo.getLong("last_backup_time", 0)
}

// ==================== NEW VIEWMODELS FOR SHARING ====================

sealed interface ConnectionState {
    data class Disconnected(val inviteCode: String?) : ConnectionState
    data class Connected(val connection: Connection) : ConnectionState
    object Loading : ConnectionState
    data class Error(val message: String) : ConnectionState
}

class ConnectionViewModel(
    application: Application,
    private val repo: ConnectionRepository,
    private val settingsRepo: SettingsRepository
) : AndroidViewModel(application) {

    private val _state = MutableLiveData<ConnectionState>()
    val state: LiveData<ConnectionState> = _state

    init {
        loadConnection()
    }

    private fun loadConnection() {
        _state.value = ConnectionState.Loading
        viewModelScope.launch {
            repo.getActive().first().let { conn ->
                _state.value = if (conn != null)
                    ConnectionState.Connected(conn)
                else
                    ConnectionState.Disconnected(settingsRepo.getString("my_invite_code"))
            }
        }
    }

    suspend fun generateInviteCode(): String {
        val code = repo.generateInviteCode()
        settingsRepo.putString("my_invite_code", code)
        loadConnection()
        return code
    }

    suspend fun acceptInvite(code: String): Boolean {
        val myId = settingsRepo.getString("user_id", "")
        val myName = settingsRepo.getString("username", "")
        val success = repo.acceptInvite(code, myId, myName) != null
        loadConnection()
        return success
    }

    suspend fun disconnect() {
        repo.disconnect()
        settingsRepo.delete("my_invite_code")
        loadConnection()
    }
}

class RecurringTransactionViewModel(
    application: Application,
    private val repo: RecurringTransactionRepository,
    private val settingsRepo: SettingsRepository
) : AndroidViewModel(application) {

    private val _userId = settingsRepo.getString("user_id", "")
    
    val recurringTransactions: LiveData<List<RecurringTransaction>> = 
        repo.getByOwner(_userId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    suspend fun add(
        description: String,
        amount: Double,
        type: String,
        categoryId: Long?,
        frequency: RecurringFrequency,
        dayOfMonth: Int,
        startDate: Long,
        endDate: Long?
    ) {
        val nextDue = calculateNextDue(startDate, frequency, dayOfMonth)
        val rt = RecurringTransaction(
            description = description,
            amount = amount,
            type = type,
            categoryId = categoryId,
            frequency = frequency,
            dayOfMonth = dayOfMonth,
            ownerId = _userId,
            startDate = startDate,
            endDate = endDate,
            nextDueDate = nextDue
        )
        repo.insert(rt)
    }

    suspend fun delete(id: Long) = repo.deleteById(id)
    suspend fun toggleActive(id: Long) = repo.toggleActive(id)

    private fun calculateNextDue(startDate: Long, frequency: RecurringFrequency, dayOfMonth: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = startDate
        when (frequency) {
            RecurringFrequency.MONTHLY -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)))
                if (cal.timeInMillis <= startDate) cal.add(java.util.Calendar.MONTH, 1)
            }
            RecurringFrequency.WEEKLY -> {
                val targetDay = (dayOfMonth - 1 + 7) % 7 + 1 // 1=Sunday...7=Saturday
                val diff = (targetDay - cal.get(java.util.Calendar.DAY_OF_WEEK) + 7) % 7
                if (diff == 0) cal.add(java.util.Calendar.WEEK_OF_YEAR, 1) else cal.add(java.util.Calendar.DAY_OF_YEAR, diff)
            }
            RecurringFrequency.DAILY -> cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.YEARLY -> {
                cal.add(java.util.Calendar.YEAR, 1)
            }
        }
        return cal.timeInMillis
    }
}

class ShoppingViewModel(
    application: Application,
    private val repo: ShoppingRepository,
    private val settingsRepo: SettingsRepository
) : AndroidViewModel(application) {

    private val _userId = settingsRepo.getString("user_id", "")
    
    val items: LiveData<List<ShoppingItem>> = 
        repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val pendingItems: LiveData<List<ShoppingItem>> = androidx.lifecycle.Transformations.map(items) { 
        it.filter { !it.isChecked } 
    }
    val checkedItems: LiveData<List<ShoppingItem>> = androidx.lifecycle.Transformations.map(items) { 
        it.filter { it.isChecked } 
    }
    
    val progress: LiveData<Float> = androidx.lifecycle.Transformations.map(items) { all ->
        if (all.isEmpty()) 0f else all.count { it.isChecked }.toFloat() / all.size
    }

    suspend fun add(name: String, category: String?) {
        val item = ShoppingItem(
            name = name,
            category = category,
            ownerId = settingsRepo.getString("user_id", "")
        )
        repo.insert(item)
    }
    
    suspend fun toggleCheck(id: Long) = repo.toggleCheck(id, settingsRepo.getString("user_id", ""))
    suspend fun delete(id: Long) = repo.deleteById(id)
}