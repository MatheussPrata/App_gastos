package com.matheus.gastos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.repository.SettingsRepository
import com.matheus.gastos.ui.screen.add_card.coerceDay
import com.matheus.gastos.ui.screen.add_card.parseLimit
import com.matheus.gastos.viewmodel.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CreditCardValidationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        DispatchersHolder.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        DispatchersHolder.resetMain()
    }

    // --- parseLimit ---

    @Test
    fun `parseLimit accepts pt-BR thousands and decimals`() {
        assertEquals(1234.56, parseLimit("1.234,56"), 0.001)
    }

    @Test
    fun `parseLimit accepts comma decimal`() {
        assertEquals(10.5, parseLimit("10,50"), 0.001)
    }

    @Test
    fun `parseLimit accepts dot decimal and integer`() {
        assertEquals(10.5, parseLimit("10.50"), 0.001)
        assertEquals(500.0, parseLimit("500"), 0.001)
    }

    @Test
    fun `parseLimit trims spaces`() {
        assertEquals(42.0, parseLimit("  42  "), 0.001)
    }

    @Test
    fun `parseLimit returns zero for empty and invalid input`() {
        assertEquals(0.0, parseLimit(""), 0.0)
        assertEquals(0.0, parseLimit("   "), 0.0)
        assertEquals(0.0, parseLimit("abc"), 0.0)
        assertEquals(0.0, parseLimit("1,2,3"), 0.0)
        assertEquals(0.0, parseLimit("0"), 0.0)
        assertEquals(0.0, parseLimit("-10"), 0.0)
    }

    // --- coerceDay ---

    @Test
    fun `coerceDay keeps valid days`() {
        assertEquals(1, coerceDay("1"))
        assertEquals(15, coerceDay("15"))
        assertEquals(31, coerceDay("31"))
    }

    @Test
    fun `coerceDay clamps out of range`() {
        assertEquals(31, coerceDay("32"))
        assertEquals(31, coerceDay("99"))
        assertEquals(1, coerceDay("0"))
        assertEquals(1, coerceDay("-5"))
    }

    @Test
    fun `coerceDay falls back on invalid input`() {
        assertEquals(1, coerceDay(""))
        assertEquals(1, coerceDay("abc"))
        assertEquals(10, coerceDay("", 10))
        assertEquals(10, coerceDay("xyz", 10))
        // default também é saneado
        assertEquals(31, coerceDay("", 40))
    }

    // --- modelo CreditCard ---

    @Test
    fun `credit card defaults are sane`() {
        val card = CreditCard(name = "Nubank")
        assertEquals("", card.lastFourDigits)
        assertEquals(0.0, card.limitAmount, 0.0)
        assertEquals(1, card.closingDay)
        assertEquals(10, card.dueDay)
        assertEquals(true, card.isActive)
    }

    @Test
    fun `credit card built from parsed form fields`() {
        val card = CreditCard(
            name = "Cartão principal",
            limitAmount = parseLimit("2.500,00"),
            closingDay = coerceDay("31"),
            dueDay = coerceDay("99", 10)
        )
        assertEquals(2500.0, card.limitAmount, 0.001)
        assertEquals(31, card.closingDay)
        assertEquals(31, card.dueDay)
    }



    private object DispatchersHolder {
        fun setMain(d: kotlinx.coroutines.CoroutineDispatcher) = kotlinx.coroutines.Dispatchers.setMain(d)
        fun resetMain() = kotlinx.coroutines.Dispatchers.resetMain()
    }
}
