package com.matheus.gastos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.repository.TransactionRepository
import com.matheus.gastos.data.dao.TransactionDao
import kotlinx.coroutines.flow.flowOf
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

class TransactionViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var repository: TransactionRepository

    private lateinit var viewModel: TransactionViewModel

    @Before
    fun setup() {
        viewModel = TransactionViewModel(androidx.test.core.app.ApplicationProvider.getApplicationContext(), repository)
    }

    @Test
    fun `totalExpenses returns 0 when no transactions` = runBlocking {
        val dao = mock<TransactionDao>()
        when(dao.getTotalExpenses(anyLong(), anyLong())).thenReturn(flowOf(0.0))
        when(dao.getTotalIncome(anyLong(), anyLong())).thenReturn(flowOf(0.0))
        
        val repo = TransactionRepository(dao)
        val viewModel = TransactionViewModel(androidx.test.core.app.ApplicationProvider.getApplicationContext(), repo)
        
        val totalExpenses = viewModel.totalExpenses.getValue()
        assertEquals(0.0, totalExpenses, 0.001)
    }
}
