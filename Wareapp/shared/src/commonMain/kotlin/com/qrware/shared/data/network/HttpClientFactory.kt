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
        tokenProvider: suspend () -> String? = { null },
        enableLogging: Boolean = true
    ): HttpClient {
        return HttpClient {
            // Base URL configuration
            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }

            // JSON Serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = false
                })
            }

            // Logging (tylko dla development)
            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                    filter { request ->
                        // Loguj tylko localhost requests w dev
                        request.url.host.contains("localhost") || 
                        request.url.host.contains("127.0.0.1") ||
                        request.url.host.contains("10.0.2.2") // Android emulator
                    }
                }
            }

            // Authentication z Bearer token
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
                    
                    sendWithoutRequest { request ->
                        // Nie wysyłaj tokena dla auth endpoints
                        !request.url.encodedPath.contains("/api/auth/login") &&
                        !request.url.encodedPath.contains("/api/auth/register") &&
                        !request.url.encodedPath.contains("/api/health")
                    }
                }
            }

            // Default headers
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.CacheControl, "no-cache")
                header("User-Agent", "QRWare-Desktop/1.0.0")
            }

            // Timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = 30000  // 30 sekund
                connectTimeoutMillis = 10000  // 10 sekund
                socketTimeoutMillis = 30000   // 30 sekund
            }

            // CORS handling dla web
            install(DefaultRequest) {
                header(HttpHeaders.Origin, baseUrl)
            }

            // Error handling i status code validation
            HttpResponseValidator {
                validateResponse { response ->
                    when (response.status.value) {
                        in 200..299 -> return@validateResponse
                        401 -> throw UnauthorizedException("Authentication required")
                        403 -> throw ForbiddenException("Access forbidden")
                        404 -> throw NotFoundException("Resource not found")
                        422 -> throw ValidationException("Validation error")
                        in 400..499 -> throw ClientException("Client error: ${response.status}")
                        in 500..599 -> throw ServerException("Server error: ${response.status}")
                        else -> throw NetworkException("Unknown error: ${response.status}")
                    }
                }
                
                handleResponseExceptionWithRequest { exception, request ->
                    when (exception) {
                        is java.net.ConnectException,
                        is java.net.UnknownHostException -> {
                            throw ConnectionException("Cannot connect to server. Check if backend is running at ${request.url.host}:${request.url.port}")
                        }
                        is java.net.SocketTimeoutException -> {
                            throw TimeoutException("Request timeout. Server may be overloaded.")
                        }
                        else -> throw exception
                    }
                }
            }
        }
    }
}

// Custom exceptions dla lepszego error handlingu
open class NetworkException(message: String) : Exception(message)
class UnauthorizedException(message: String) : NetworkException(message)
class ForbiddenException(message: String) : NetworkException(message)
class NotFoundException(message: String) : NetworkException(message)
class ValidationException(message: String) : NetworkException(message)
class ClientException(message: String) : NetworkException(message)
class ServerException(message: String) : NetworkException(message)
class ConnectionException(message: String) : NetworkException(message)
class TimeoutException(message: String) : NetworkException(message)