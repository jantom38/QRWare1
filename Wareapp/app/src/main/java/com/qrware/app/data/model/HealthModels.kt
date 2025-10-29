package com.qrware.app.data.model

data class HealthStatus(
    val status: String,
    val message: String,
    val application: String,
    val version: String
)

data class SystemStatus(
    val application: String,
    val version: String,
    val uptime: String,
    val system: Map<String, String>,
    val memory: Map<String, String>,
    val database: Map<String, Any>,
    val status: String
)