package com.matheus.gastos

import android.app.Application
import com.matheus.gastos.data.repository.CategoryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GastosApplication : Application() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Popula as categorias padrão somente no primeiro uso (banco vazio).
        appScope.launch {
            runCatching { categoryRepository.seedDefaultsIfEmpty() }
        }
    }
}
