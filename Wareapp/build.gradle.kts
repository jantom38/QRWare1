plugins {
    // Android Application (dla aplikacji)
    alias(libs.plugins.android.application) apply false

    // Android Library (dla modułów shared) - ZMIANA: używamy standardowego android.library
    alias(libs.plugins.android.library) apply false

    // Kotlin Multiplatform
    alias(libs.plugins.kotlin.multiplatform) apply false

    // Kotlin Android (opcjonalne, jeśli używasz KMP, ale bezpiecznie zostawić)
    alias(libs.plugins.kotlin.android) apply false

    // --- POPRAWKA DLA COMPOSE ---
    // Główny plugin Compose (UI)
    alias(libs.plugins.jetbrains.compose) apply false

    // Plugin Kompilatora Compose (Wcześniej nazywał się u Ciebie kotlin.compose)
    alias(libs.plugins.compose.compiler) apply false

    // Serializacja
    alias(libs.plugins.kotlin.serialization) apply false
}