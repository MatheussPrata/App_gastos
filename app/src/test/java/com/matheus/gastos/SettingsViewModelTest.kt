package com.matheus.gastos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.matheus.gastos.data.repository.SettingsRepository
import com.matheus.gastos.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repo: SettingsRepository = mock()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load reads persisted values`() = runTest(testDispatcher) {
        whenever(repo.getInt(org.mockito.kotlin.eq("theme_mode"), org.mockito.kotlin.any())).thenReturn(0)
        whenever(repo.getInt(org.mockito.kotlin.eq("accent_color"), org.mockito.kotlin.any())).thenReturn(0xFF6750A4.toInt())
        whenever(repo.getBoolean(org.mockito.kotlin.eq("notifications_enabled"), org.mockito.kotlin.any())).thenReturn(true)
        whenever(repo.getInt("theme_mode", 0)).thenReturn(1)
        whenever(repo.getInt("accent_color", 0xFF6750A4.toInt())).thenReturn(0xFF2196F3.toInt())
        whenever(repo.getBoolean("notifications_enabled", true)).thenReturn(false)

        val vm = SettingsViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.themeMode.value)
        assertEquals(0xFF2196F3.toInt(), vm.accentColor.value)
        assertEquals(false, vm.notificationsEnabled.value)
    }

    @Test
    fun `defaults when repository has no values`() = runTest(testDispatcher) {
        whenever(repo.getInt(org.mockito.kotlin.eq("theme_mode"), org.mockito.kotlin.any())).thenReturn(0)
        whenever(repo.getInt(org.mockito.kotlin.eq("accent_color"), org.mockito.kotlin.any())).thenReturn(0xFF6750A4.toInt())
        whenever(repo.getBoolean(org.mockito.kotlin.eq("notifications_enabled"), org.mockito.kotlin.any())).thenReturn(true)
        val vm = SettingsViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, vm.themeMode.value)
        assertEquals(0xFF6750A4.toInt(), vm.accentColor.value)
        assertEquals(true, vm.notificationsEnabled.value)
    }

    @Test
    fun `setThemeMode persists and updates live data`() = runTest(testDispatcher) {
        whenever(repo.getInt(org.mockito.kotlin.eq("theme_mode"), org.mockito.kotlin.any())).thenReturn(0)
        whenever(repo.getInt(org.mockito.kotlin.eq("accent_color"), org.mockito.kotlin.any())).thenReturn(0xFF6750A4.toInt())
        whenever(repo.getBoolean(org.mockito.kotlin.eq("notifications_enabled"), org.mockito.kotlin.any())).thenReturn(true)
        val vm = SettingsViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.setThemeMode(2)

        verify(repo).putInt("theme_mode", 2)
        assertEquals(2, vm.themeMode.value)
    }

    @Test
    fun `setThemeMode does not change other keys`() = runTest(testDispatcher) {
        whenever(repo.getInt(org.mockito.kotlin.eq("theme_mode"), org.mockito.kotlin.any())).thenReturn(0)
        whenever(repo.getInt(org.mockito.kotlin.eq("accent_color"), org.mockito.kotlin.any())).thenReturn(0xFF6750A4.toInt())
        whenever(repo.getBoolean(org.mockito.kotlin.eq("notifications_enabled"), org.mockito.kotlin.any())).thenReturn(true)
        val vm = SettingsViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.setThemeMode(1)

        verify(repo, org.mockito.kotlin.never()).putInt(org.mockito.kotlin.eq("accent_color"), org.mockito.kotlin.any())
        verify(repo, org.mockito.kotlin.never()).putBoolean(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    @Test
    fun `setAccentColor persists and updates live data`() = runTest(testDispatcher) {
        whenever(repo.getInt(org.mockito.kotlin.eq("theme_mode"), org.mockito.kotlin.any())).thenReturn(0)
        whenever(repo.getInt(org.mockito.kotlin.eq("accent_color"), org.mockito.kotlin.any())).thenReturn(0xFF6750A4.toInt())
        whenever(repo.getBoolean(org.mockito.kotlin.eq("notifications_enabled"), org.mockito.kotlin.any())).thenReturn(true)
        val vm = SettingsViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        val color = 0xFF4CAF50.toInt()
        vm.setAccentColor(color)

        verify(repo).putInt("accent_color", color)
        assertEquals(color, vm.accentColor.value)
    }

    @Test
    fun `setNotificationsEnabled persists boolean`() = runTest(testDispatcher) {
        whenever(repo.getInt(org.mockito.kotlin.eq("theme_mode"), org.mockito.kotlin.any())).thenReturn(0)
        whenever(repo.getInt(org.mockito.kotlin.eq("accent_color"), org.mockito.kotlin.any())).thenReturn(0xFF6750A4.toInt())
        whenever(repo.getBoolean(org.mockito.kotlin.eq("notifications_enabled"), org.mockito.kotlin.any())).thenReturn(true)
        val vm = SettingsViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.setNotificationsEnabled(false)

        verify(repo).putBoolean("notifications_enabled", false)
        assertEquals(false, vm.notificationsEnabled.value)
    }

    @Test
    fun `setLastBackupTime uses putLong`() = runTest(testDispatcher) {
        whenever(repo.getInt(org.mockito.kotlin.eq("theme_mode"), org.mockito.kotlin.any())).thenReturn(0)
        whenever(repo.getInt(org.mockito.kotlin.eq("accent_color"), org.mockito.kotlin.any())).thenReturn(0xFF6750A4.toInt())
        whenever(repo.getBoolean(org.mockito.kotlin.eq("notifications_enabled"), org.mockito.kotlin.any())).thenReturn(true)
        val vm = SettingsViewModel(repo)

        vm.setLastBackupTime(123456789L)

        verify(repo).putLong("last_backup_time", 123456789L)
        assertFalse(vm.themeMode.value == null)
    }
}
