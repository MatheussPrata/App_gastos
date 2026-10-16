package com.matheus.gastos.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.matheus.gastos.R
import com.matheus.gastos.data.local.AppDatabase
import com.matheus.gastos.data.repository.BudgetLimitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BudgetAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val db = AppDatabase.getInstance(applicationContext)
        val repo = com.matheus.gastos.data.repository.BudgetLimitRepository(db.budgetLimitDao())
        
        val activeLimits = repo.getActive().first()
        
        for (limit in activeLimits) {
            if (limit.spentAmount >= limit.limitAmount * 0.9) {
                showNotification(limit)
            }
        }
        
        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }

    private fun showNotification(limit: com.matheus.gastos.data.entity.BudgetLimit) {
        val channelId = "budget_alerts"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Orçamento",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Limite de gasto próximo")
            .setContentText("Você atingiu 90% do limite para a categoria ${limit.categoryId}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        manager.notify(limit.id.toInt(), notification)
    }
}
