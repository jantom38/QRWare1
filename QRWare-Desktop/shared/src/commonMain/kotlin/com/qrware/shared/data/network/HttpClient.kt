package com.qrware.shared.data.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class HttpClientFactory {
    fun create(
        baseUrl: String = "http://localhost:8080",
        tokenProvider: () -> String? = { null }
    ): HttpClient {
        return HttpClient {
            // Base URL configuration
            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
            }

            // JSON Serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // Logging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
                filter { request ->
                    request.url.host.contains("localhost")
                }
            }

            // Authentication
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = tokenProvider()
                        token?.let {
                            BearerTokens(accessToken = it, refreshToken = "")
                        }
                    }
                    
                    refreshTokens {
                        val token = tokenProvider()
                        token?.let {
                            BearerTokens(accessToken = it, refreshToken = "")
                        }
                    }
                }
            }

            // Default headers
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.CacheControl, "no-cache")
            }

            // Timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 30000
            }

            // Error handling
            HttpResponseValidator {
                validateResponse { response ->
                    when (response.status.value) {
                        in 300..399 -> return@validateResponse
                        401 -> throw UnauthorizedException("Unauthorized access")
                        403 -> throw ForbiddenException("Access forbidden")
                        404 -> throw NotFoundException("Resource not found")
                        in 400..499 -> throw ClientRequestException(response, "Client error: ${response.status}")
                        in 500..599 -> throw ServerResponseException(response, "Server error: ${response.status}")
                    }
                }
            }
        }
    }
}

// Custom exceptions
class UnauthorizedException(message: String) : Exception(message)
class ForbiddenException(message: String) : Exception(message)
class NotFoundException(message: String) : Exception(message)