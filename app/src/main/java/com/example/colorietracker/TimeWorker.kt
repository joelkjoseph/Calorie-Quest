package com.example.colorietracker

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.colorietracker.database.CalorieDao
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first

class TimeWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("TimeWorker", "Work is starting.")
        try {
            if (!isWithinTimeWindow()) {
                Log.d("TimeWorker", "Not within the time window.")
                return Result.success()
            }

            val appDatabase = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "calorie-tracker_database"
            ).build()

            val calorieDao = appDatabase.calorieDao()
            val preferencesManager = PreferencesManager(applicationContext)

            val totalCalories = getTotalCaloriesForToday(calorieDao)
            val dailyGoal = preferencesManager.getGoal.first()
            val percentageConsumed = (totalCalories.toDouble() / dailyGoal) * 100

            val ability = preferencesManager.abilityFlow.first()
            val motivation = preferencesManager.motivationFlow.first()

            val notificationType = decideNotificationType(ability, motivation, totalCalories)
            showNotification(notificationType, applicationContext, percentageConsumed)

            return Result.success()
        } catch (e: Exception) {
            Log.e("TimeWorker", "Exception in doWork", e)
            return Result.failure()
        }
    }

    private suspend fun getTotalCaloriesForToday(calorieDao: CalorieDao): Int {
        return calorieDao.getTotalCaloriesForDate(LocalDate.now()).first()
    }

    private fun isWithinTimeWindow(): Boolean {
        val currentTime = LocalTime.now()
        val startTime = LocalTime.of(7, 0)
        val endTime = LocalTime.of(22, 0)
        return currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
    }

    private fun decideNotificationType(
        ability: Boolean,
        motivation: Boolean,
        totalCalories: Int
    ): String {
        return when {
            ability && motivation -> "Signal"
            !ability && motivation -> "Facilitator"
            ability && !motivation -> "Spark"
            else -> "Low"
        }
    }

    private fun showNotification(notificationType: String, context: Context, percentageConsumed: Double) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "calorieNotifications",
                "Calorie Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val (title, contentText) = getNotificationMessage(notificationType, percentageConsumed)


        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(contentText)

        val notificationBuilder = NotificationCompat.Builder(context, "calorieNotifications")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText.substring(0, Math.min(contentText.length, 40)) + "...")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_foreground, "Day Status", pendingIntent)

        notificationManager.notify(notificationType.hashCode(), notificationBuilder.build())
    }


    private fun getNotificationMessage(notificationType: String, percentageConsumed: Double): Pair<String, String> {
        return when (notificationType) {
            "Signal" -> "Great Job!" to "Impressive progress! Maintaining a healthy weight is key, and you're doing it. Keep going! You've attained ${String.format("%.1f", percentageConsumed)}% of your goal."
            "Spark" -> "A Little Nudge" to "Choose a healthy snack today, like fruit or nuts, to stay on track. You've attained ${String.format("%.1f", percentageConsumed)}% of your goal."
            "Facilitator" -> "Easy Choices" to "Opt for a simple, healthy snack like yogurt or carrot sticks. Small choices, big wins. You've attained ${String.format("%.1f", percentageConsumed)}% of your goal."
            "Low" -> "Every Step Counts" to "Every small healthy choice counts. Try swapping soda for water today. You've attained ${String.format("%.1f", percentageConsumed)}% of your goal."
            else -> "" to ""
        }
    }

}
