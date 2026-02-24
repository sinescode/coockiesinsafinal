package com.turjaun.cookiesuploader.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val email: String = "",
    val username: String,
    val password: String,
    val authCode: String = ""
)