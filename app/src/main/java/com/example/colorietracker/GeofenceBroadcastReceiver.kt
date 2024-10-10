

package com.example.colorietracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        if (geofenceEvent != null) {
            if (geofenceEvent.hasError()) {
                Log.e("GeofenceBroadcastReceiver", "Geofence error: ${geofenceEvent.errorCode}")
                return
            }
        }

        if (geofenceEvent != null) {
            if (geofenceEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                mainScope.launch {
                    handleGeofenceEnter(context)
                    Log.e("GeofenceBroadcastReceiver", "Geofence Entered")
                }
            }
        }
    }

    private suspend fun handleGeofenceEnter(context: Context) {
        val preferencesManager = PreferencesManager(context)
        val motivationFlow = preferencesManager.motivationFlow
        val abilityFlow = preferencesManager.abilityFlow

        val motivationEnabled = motivationFlow.firstOrNull() ?: false
        val abilityModeEnabled = abilityFlow.firstOrNull() ?: false

        createNotificationChannel(context)

        when {
            motivationEnabled && abilityModeEnabled -> {
                displaySignalNotification(context)
            }
            motivationEnabled && !abilityModeEnabled -> {
                displayFacilitatorNotification(context)
            }
            !motivationEnabled && abilityModeEnabled -> {
                displaySparkNotification(context)
            }
            else -> {
                displayLowNotification(context)
            }
        }
    }

    private fun displaySignalNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dayStatusPendingIntent = getMainActivityPendingIntent(context)
        val title = "Your Healthy Choice Awaits!"
        val contentText = "You're by your favorite place! How about a fruit or veggie option for your 5 A Day? Empowered choices keep you on track towards your goals."


        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(contentText)

        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText.substring(0, Math.min(contentText.length, 40)) + "...")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_launcher_foreground, "Day Status", dayStatusPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(0, builder.build())
    }


    private fun displaySparkNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dayStatusPendingIntent = getMainActivityPendingIntent(context)
        val title = "A Little Nudge Towards Your Goal"
        val contentText = "Close to a temptation? Why not go for a refreshing water with lemon instead of a sugary drink? Every small, healthy choice adds up"

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(contentText)
        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText.substring(0, Math.min(contentText.length, 40)) + "...")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_launcher_foreground, "Day Status", dayStatusPendingIntent)
        notificationManager.notify(1, builder.build())
    }

    private fun displayFacilitatorNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dayStatusPendingIntent = getMainActivityPendingIntent(context)
        val title = "Make It Easy, Make It Healthy"
        val contentText = "Considering a snack? Choose wholegrain over sugary options. It’s an easy swap that makes a big difference. Remember, every healthy choice counts."

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(contentText)

        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText.substring(0, Math.min(contentText.length, 40)) + "...")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_launcher_foreground, "Day Status", dayStatusPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(2, builder.build())
    }

    private fun displayLowNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dayStatusPendingIntent = getMainActivityPendingIntent(context)
        val title = "Every Choice Counts!"
        val contentText = "Right by a favorite spot? Opt for a meal with more greens. Don’t finish your plate if you’re full - it's okay to save some for later. Every choice is a step towards your goal."

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(contentText)

        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText.substring(0, Math.min(contentText.length, 40)) + "...")
            .setStyle(bigTextStyle)
            .addAction(R.drawable.ic_launcher_foreground, "Day Status", dayStatusPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(3, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Channel Name"
        val descriptionText = "Channel Description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("channel_id", name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    private fun getMainActivityPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

}