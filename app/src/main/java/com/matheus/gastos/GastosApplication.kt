package com.matheus.gastos

import android.app.Application
import com.matheus.gastos.data.repository.CategoryRepository
import com.matheus.gastos.data.repository.CreditCardRepository
import com.matheus.gastos.data.repository.TransactionRepository
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

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var creditCardRepository: CreditCardRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Ordem importa: primeiro categorias padrão, depois lançamentos de demonstração.
        // Ambos rodam no mesmo escopo para o seed de transações ver as categorias já criadas.
        appScope.launch {
            runCatching {
                categoryRepository.seedDefaultsIfEmpty()
                val categories = categoryRepository.getAllOnce()
                transactionRepository.seedDemoIfEmpty(categories)
                creditCardRepository.seedDemoCardsIfEmpty()
            }
        }
    }
}
