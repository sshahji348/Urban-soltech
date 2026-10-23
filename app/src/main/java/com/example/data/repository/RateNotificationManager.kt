package com.example.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RateNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "urban_soltech_rates_channel"
        const val CHANNEL_NAME = "Hourly Solar Rate Updates"
        const val CHANNEL_DESC = "Notifications for hourly solar panel market rates updates"
        const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "urban_soltech_notification_prefs"
        private const val KEY_OPTED_IN = "hourly_rates_notification_opted_in"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isOptedIn = MutableStateFlow(prefs.getBoolean(KEY_OPTED_IN, true))
    val isOptedIn: StateFlow<Boolean> = _isOptedIn.asStateFlow()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun setOptedIn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OPTED_IN, enabled).apply()
        _isOptedIn.value = enabled
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendRateUpdateNotification(
        benchmarkRate: Double,
        trendDelta: Double,
        topBrandName: String,
        topBrandRate: Double,
        isTest: Boolean = false
    ) {
        if (!_isOptedIn.value) return
        if (!hasNotificationPermission()) return

        val deltaSign = if (trendDelta >= 0) "+" else ""
        val trendText = if (trendDelta != 0.0) "($deltaSign$trendDelta PKR)" else "(Stable)"

        val contentTitle = if (isTest) {
            "⚡ Urban Soltech • Notification Test"
        } else {
            "⚡ Urban Soltech • Hourly Solar Rate Index"
        }

        val contentText = "Tier-1 solar rate: PKR $benchmarkRate / Watt $trendText. Tap to view market index."

        val detailedText = "Hourly Pakistan Solar Market Update:\n" +
                "• Benchmark Rate: PKR $benchmarkRate / Watt $trendText\n" +
                "• $topBrandName: PKR $topBrandRate / Watt\n" +
                "Tap to view Tier-1 panel prices, stock status, or calculate your system quotation."

        // Deep-link intent to open MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TARGET_TAB", 0) // Live Rates tab
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission revoked or not granted
        }
    }
}
