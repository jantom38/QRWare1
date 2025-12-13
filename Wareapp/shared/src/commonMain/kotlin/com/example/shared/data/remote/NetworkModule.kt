package com.example.shared.data.remote

import com.example.shared.data.preferences.ServerConfigManager
import com.example.shared.security.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkModule {

    private lateinit var serverConfigManager: ServerConfigManager

    fun init(serverConfigManager: ServerConfigManager) {
        this.serverConfigManager = serverConfigManager
    }

    fun getBaseUrl(): String {
        return if (::serverConfigManager.isInitialized) {
            serverConfigManager.getServerUrl()
        } else {
            "http://192.168.0.178:8080"
        }
    }

    fun createClient(tokenManager: TokenManager): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            install(Logging) {
                level = LogLevel.BODY
            }

            defaultRequest {
                url(getBaseUrl())
                contentType(ContentType.Application.Json)

                tokenManager.getAccessToken()?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }
        }
    }
}