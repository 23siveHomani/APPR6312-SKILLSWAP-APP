package com.skillswap.app.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skillswap.app.R
import com.skillswap.app.SkillSwapApp
import com.skillswap.app.data.model.SettingsUpdateRequest
import com.skillswap.app.ui.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives FCM pushes for new swap requests, status changes and chat messages.
 * Shows real-time push notifications in the Android system notification tray.
 */
class SkillSwapMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM token received: $token. Registering with backend API...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = ServiceLocator.sessionManager(applicationContext)
                if (sessionManager.isLoggedIn()) {
                    val api = ServiceLocator.apiService(applicationContext)
                    api.updateSettings(SettingsUpdateRequest(fcmToken = token))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register FCM token with API", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "SkillSwap Notification"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new skill swap alert!"

        Log.i(TAG, "FCM Push received: '$title' - '$body'")
        displaySystemNotification(applicationContext, title, body)
    }

    companion object {
        private const val TAG = "SkillSwapFCM"

        fun displaySystemNotification(context: Context, title: String, body: String) {
            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, SkillSwapApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_back)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Notification permission missing on device", e)
            }
        }
    }
}
