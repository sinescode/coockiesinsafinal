package com.turjaun.cookiesuploader

import android.app.Application
import com.turjaun.cookiesuploader.fcm.FcmTokenManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize FCM
        FcmTokenManager.init(this)
    }
}