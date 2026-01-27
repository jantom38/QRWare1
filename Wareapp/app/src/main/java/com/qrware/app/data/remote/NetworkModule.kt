package com.qrware.app.data.remote

import com.qrware.app.data.preferences.ServerConfigManager
import com.qrware.app.security.AuthInterceptor
import com.qrware.app.security.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private lateinit var serverConfigManager: ServerConfigManager

    fun createClient(tokenManager: TokenManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

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

    fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}