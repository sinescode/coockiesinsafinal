package com.turjaun.cookiesuploader.data

import android.content.Context
import com.turjaun.cookiesuploader.data.repository.DataStoreRepository
import com.turjaun.cookiesuploader.network.WebhookClient
import com.turjaun.cookiesuploader.security.SecureVault

class AppContainer(private val context: Context) {
    
    val dataStoreRepository: DataStoreRepository by lazy {
        DataStoreRepository(context)
    }
    
    val webhookClient: WebhookClient by lazy {
        WebhookClient()
    }
    
    val secureVault: SecureVault by lazy {
        SecureVault()
    }
}