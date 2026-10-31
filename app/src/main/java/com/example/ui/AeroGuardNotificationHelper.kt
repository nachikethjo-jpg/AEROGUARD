package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object AeroGuardNotificationHelper {

    private const val CHANNEL_ID = "aqi_threshold_alerts"
    private const val CHANNEL_NAME = "AQI Threshold Alerts (>150)"
    private const val NOTIFICATION_ID = 1001

    private const val PROXIMITY_CHANNEL_ID = "hotspot_proximity_alerts"
    private const val PROXIMITY_CHANNEL_NAME = "Pollution Hotspot Proximity (<500m)"
    private const val PROXIMITY_NOTIFICATION_ID = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Dispatches immediate notifications when local air quality exceeds the 150 AQI safety threshold."
                enableVibration(true)
            }
            val proximityChannel = NotificationChannel(PROXIMITY_CHANNEL_ID, PROXIMITY_CHANNEL_NAME, importance).apply {
                description = "Alerts the user when they enter within 500m of a confirmed pollution hotspot."
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            manager.createNotificationChannel(proximityChannel)
        }
    }

    fun sendAqiThresholdNotification(context: Context, locationName: String, aqi: Int) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("⚠️ High Pollution Alert: AQI Exceeded 150!")
            .setContentText("Warning: $locationName is experiencing AQI $aqi (Unhealthy). High PM2.5 detected.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Warning: $locationName sensor indicates AQI $aqi (Unhealthy/Poor). Fine PM2.5 levels exceed safe limits. Sensitive groups must wear N95 respirators outdoors and run HEPA air purifiers indoors."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    fun sendHotspotProximityNotification(
        context: Context,
        hotspotName: String,
        distanceMeters: Int,
        hazardType: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, PROXIMITY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 Hotspot Nearby: ${distanceMeters}m to $hazardType")
            .setContentText("You are within ${distanceMeters}m of pollution hotspot: $hotspotName.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "You are within ${distanceMeters}m of a verified high-density pollution hotspot at $hotspotName ($hazardType). Immediate exposure risk: Put on your certified N95 respirator and avoid vigorous outdoor exertion."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(PROXIMITY_NOTIFICATION_ID, builder.build())
    }
}
