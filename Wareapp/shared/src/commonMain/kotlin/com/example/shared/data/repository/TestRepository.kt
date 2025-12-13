package com.example.shared.data.repository

import com.example.shared.data.remote.TestService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TestRepository(private val testService: TestService) {

    // Konfiguracja JSON do ładnego formatowania
    private val json = Json { prettyPrint = true }

    private suspend fun fetchAndFormatException(
        apiCall: suspend () -> Map<String, Any>
    ): Result<String> {
        return try {
            val responseMap = apiCall()
            // W KMP 'Any' w mapie serializuje się do JSON, jeśli to podstawowe typy (String, Number, Boolean)
            // Jeśli obiekt jest bardziej złożony, może wymagać 'JsonObject' zamiast Map.
            // Zakładam, że API zwraca proste struktury.
            // UWAGA: Serializacja Map<String, Any> w KMP jest ryzykowna.
            // Najlepiej używać JsonObject, ale przy tym API (Map<String, Any>) zrobimy toString() dla uproszczenia
            // lub spróbujemy serializacji jeśli typy są proste.

            val formattedJson = responseMap.toString() // Fallback lub właściwa serializacja
            // W idealnym świecie apiCall zwracałoby JsonElement

            Result.success(formattedJson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPublicData(): Result<String> {
        return fetchAndFormatException { testService.getPublicEndpoint() }
    }

    suspend fun getProtectedData(): Result<String> {
        return fetchAndFormatException { testService.getProtectedEndpoint() }
    }

    suspend fun getAdminData(): Result<String> {
        return fetchAndFormatException { testService.getAdminEndpoint() }
    }
}