package com.matheus.gastos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.repository.TransactionRepository
import com.matheus.gastos.data.dao.TransactionDao
import com.matheus.gastos.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.util.Date

class TransactionRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var dao: TransactionDao

    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        repository = TransactionRepository(dao)
    }

    @Test
    fun `getMonthBounds returns correct start and end of month`() = runBlocking {
        val date = Date(1704067200000) // Jan 1, 2024
        val (start, end) = TransactionRepository.getMonthBounds(date)
        
        assertEquals(1704067200000, start) // Jan 1, 2024 00:00:00
        assertEquals(1706745599999, end)   // Jan 31, 2024 23:59:59
    }
    
    @Test
    fun `insert transaction calls dao`() = runBlocking {
        val transaction = Transaction(
            description = "Test",
            amount = 100.0,
            date = System.currentTimeMillis(),
            isExpense = true
        )
        
        repository.insert(transaction)
        
        verify(dao).insert(transaction)
        Unit
    }
}
