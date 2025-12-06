package com.qrware.app.data.preferences

import android.content.Context
import android.content.SharedPreferences

class ServerConfigManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "server_config", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_USE_HTTPS = "use_https"
        private const val DEFAULT_IP = "10.0.2.2"
        private const val DEFAULT_PORT = "8080"
        private const val DEFAULT_USE_HTTPS = false
    }
    
    /**
     * Pobiera pełny URL serwera
     */
    fun getServerUrl(): String {
        val ip = getServerIp()
        val port = getServerPort()
        val protocol = if (getUseHttps()) "https" else "http"
        return "$protocol://$ip:$port"
    }
    
    /**
     * Pobiera IP serwera
     */
    fun getServerIp(): String {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_IP) ?: DEFAULT_IP
    }
    
    /**
     * Ustawia IP serwera
     */
    fun setServerIp(ip: String) {
        prefs.edit().putString(KEY_SERVER_URL, ip).apply()
    }
    
    /**
     * Pobiera port serwera
     */
    fun getServerPort(): String {
        return prefs.getString(KEY_SERVER_PORT, DEFAULT_PORT) ?: DEFAULT_PORT
    }
    
    /**
     * Ustawia port serwera
     */
    fun setServerPort(port: String) {
        prefs.edit().putString(KEY_SERVER_PORT, port).apply()
    }
    
    /**
     * Sprawdza czy używa HTTPS
     */
    fun getUseHttps(): Boolean {
        return prefs.getBoolean(KEY_USE_HTTPS, DEFAULT_USE_HTTPS)
    }
    
    /**
     * Ustawia czy używać HTTPS
     */
    fun setUseHttps(useHttps: Boolean) {
        prefs.edit().putBoolean(KEY_USE_HTTPS, useHttps).apply()
    }
    
    /**
     * Resetuje do domyślnych ustawień
     */
    fun resetToDefaults() {
        prefs.edit()
            .putString(KEY_SERVER_URL, DEFAULT_IP)
            .putString(KEY_SERVER_PORT, DEFAULT_PORT)
            .putBoolean(KEY_USE_HTTPS, DEFAULT_USE_HTTPS)
            .apply()
    }
    
    /**
     * Sprawdza czy konfiguracja została zmieniona z domyślnej
     */
    fun isCustomConfig(): Boolean {
        return getServerIp() != DEFAULT_IP || 
               getServerPort() != DEFAULT_PORT || 
               getUseHttps() != DEFAULT_USE_HTTPS
    }
    
    /**
     * Pobiera listę popularnych IP dla deweloperów
     */
    fun getCommonDevIPs(): List<String> {
        return listOf(
            "10.0.2.2",        // Android Emulator default
            "192.168.1.100",   // Common local network
            "192.168.0.100",   // Common local network  
            "10.95.124.18",    // Current default
            "localhost"        // For testing
        )
    }
}