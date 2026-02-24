package com.turjaun.cookiesuploader.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.turjaun.cookiesuploader.MainActivity
import com.turjaun.cookiesuploader.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "cookies_channel"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle foreground notification
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body, remoteMessage.data)
        }
        
        // Handle data payload even if no notification
        if (remoteMessage.data.isNotEmpty()) {
            handleDataPayload(remoteMessage.data)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save new token to preferences
        CoroutineScope(Dispatchers.IO).launch {
            App.container.dataStoreRepository.saveDeviceToken(token)
        }
    }
    
    private fun sendNotification(title: String?, message: String?, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data?.let {
                putExtra("notification_data", it.toString())
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: getString(R.string.app_name))
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    
    private fun handleDataPayload(data: Map<String, String>) {
        // Process data payload for background handling
        // Example: update accounts, sync data, etc.
        CoroutineScope(Dispatchers.IO).launch {
            // Your background logic here
        }
    }
}