package com.example.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthStatus(
    val status: String,
    val message: String,
    val application: String,
    val version: String
)

@Serializable
data class SystemStatus(
    val application: String,
    val version: String,
    val uptime: String,
    val system: Map<String, String>,
    val memory: Map<String, String>,

    val database: Map<String, String>,
    val status: String
)