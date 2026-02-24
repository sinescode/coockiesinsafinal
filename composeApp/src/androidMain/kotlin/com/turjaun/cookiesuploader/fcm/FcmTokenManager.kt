package com.turjaun.cookiesuploader.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FcmTokenManager {
    companion object {
        suspend fun getToken(context: android.content.Context): String? {
            return try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCM", "Token retrieved: $token")
                
                // Save to DataStore
                val repo = com.turjaun.cookiesuploader.data.AccountRepository(context)
                repo.saveFcmToken(token)
                
                token
            } catch (e: Exception) {
                Log.e("FCM", "Failed to get token", e)
                null
            }
        }

        fun init(context: android.content.Context) {
            CoroutineScope(Dispatchers.IO).launch {
                getToken(context)
            }
        }
    }
}