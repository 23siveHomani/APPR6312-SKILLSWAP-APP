package com.skillswap.app.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skillswap.app.data.model.SettingsUpdateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives FCM pushes for new swap requests, status changes and chat
 * messages (Part 1 section 5, "Notification Service"). Real display of the
 * notification (building a Notification + NotificationManager.notify) is
 * deferred to the final PoE per the assignment brief; this class covers the
 * required plumbing - token registration and message receipt logging - for
 * the Part 2 prototype.
 */
class SkillSwapMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM token received, registering with API")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = ServiceLocator.sessionManager(applicationContext)
                if (sessionManager.isLoggedIn()) {
                    val api = ServiceLocator.apiService(applicationContext)
                    api.updateSettings(SettingsUpdateRequest(fcmToken = token))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register FCM token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.i(TAG, "Push received: ${message.notification?.title} - ${message.notification?.body}")
    }

    companion object {
        private const val TAG = "SkillSwapFCM"
    }
}
