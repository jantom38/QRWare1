package com.example.shared.data.preferences

import com.russhwolf.settings.Settings

class ServerConfigManager(private val settings: Settings) {

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_USE_HTTPS = "use_https"
        private const val DEFAULT_IP = "10.0.2.2"
        private const val DEFAULT_PORT = "8080"
        private const val DEFAULT_USE_HTTPS = false
    }

    fun getServerUrl(): String {
        val ip = getServerIp()
        val port = getServerPort()
        val protocol = if (getUseHttps()) "https" else "http"
        return "$protocol://$ip:$port"
    }

    fun getServerIp(): String {
        return settings.getString(KEY_SERVER_URL, DEFAULT_IP)
    }

    fun setServerIp(ip: String) {
        settings.putString(KEY_SERVER_URL, ip)
    }

    fun getServerPort(): String {
        return settings.getString(KEY_SERVER_PORT, DEFAULT_PORT)
    }

    fun setServerPort(port: String) {
        settings.putString(KEY_SERVER_PORT, port)
    }

    fun getUseHttps(): Boolean {
        return settings.getBoolean(KEY_USE_HTTPS, DEFAULT_USE_HTTPS)
    }

    fun setUseHttps(useHttps: Boolean) {
        settings.putBoolean(KEY_USE_HTTPS, useHttps)
    }

    fun resetToDefaults() {
        settings.putString(KEY_SERVER_URL, DEFAULT_IP)
        settings.putString(KEY_SERVER_PORT, DEFAULT_PORT)
        settings.putBoolean(KEY_USE_HTTPS, DEFAULT_USE_HTTPS)
    }

    fun isCustomConfig(): Boolean {
        return getServerIp() != DEFAULT_IP ||
                getServerPort() != DEFAULT_PORT ||
                getUseHttps() != DEFAULT_USE_HTTPS
    }

    fun getCommonDevIPs(): List<String> {
        return listOf(
            "10.0.2.2",
            "192.168.1.100",
            "192.168.0.100",
            "10.95.124.18",
            "localhost",
            "127.0.0.1"
        )
    }
}