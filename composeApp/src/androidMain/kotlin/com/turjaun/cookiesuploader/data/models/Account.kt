package com.turjaun.cookiesuploader.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val email: String = "",
    val username: String,
    val password: String,
    val auth_code: String
)