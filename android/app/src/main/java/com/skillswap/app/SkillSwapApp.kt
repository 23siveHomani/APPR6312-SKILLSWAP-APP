package com.skillswap.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import com.skillswap.app.util.LocaleHelper

/**
 * Application entry point. Applies language preferences and initializes
 * notification channels for real-time notifications.
 */
class SkillSwapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "SkillSwap application started")
        
        // Apply user's language choice (English, isiZulu, Afrikaans)
        LocaleHelper.applySavedLocale(this)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val TAG = "SkillSwapApp"
        const val CHANNEL_ID = "skillswap_notifications"
    }
}
