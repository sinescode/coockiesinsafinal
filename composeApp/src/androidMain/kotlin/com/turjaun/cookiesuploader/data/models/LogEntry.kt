package com.turjaun.cookiesuploader.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    val status: String,
    val message: String,
    val time: String,
    val style: String = "normal"
)