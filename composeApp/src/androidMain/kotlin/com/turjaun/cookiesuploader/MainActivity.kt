package com.turjaun.cookiesuploader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.turjaun.cookiesuploader.ui.theme.CookiesUploaderTheme
import com.turjaun.cookiesuploader.ui.MainScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Save token to preferences
                CoroutineScope(Dispatchers.IO).launch {
                    App.container.dataStoreRepository.saveDeviceToken(token)
                }
            }
        }
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = view.context.window
                    window.statusBarColor = Color(0xFF0f172a).toArgb()
                    window.navigationBarColor = Color(0xFF0f172a).toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                }
            }
            
            CookiesUploaderTheme {
                MainScreen()
            }
        }
    }
}