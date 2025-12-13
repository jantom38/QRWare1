import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Definicje wersji lokalnych
val ktorVersion = "2.3.12"
val coroutinesVersion = "1.7.3"
val serializationVersion = "1.6.3"
val lifecycleVersion = "2.8.0"
val navVersion = "2.8.0-alpha10"
val dateTimeVersion = "0.6.1"
val multiplatformSettingsVersion = "1.1.1"

plugins {
    // Standardowe pluginy KMP
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library) // Teraz zadziała (pobiera android-library z TOML)

    // Pluginy Compose
    alias(libs.plugins.jetbrains.compose) // Teraz zadziała (pobiera jetbrains-compose z TOML)
    alias(libs.plugins.compose.compiler) // Teraz zadziała (pobiera compose-compiler z TOML)

    // Serializacja
    alias(libs.plugins.kotlin.serialization) // Zalecany sposób zamiast kotlin("plugin.serialization")
}

kotlin {
    // 1. JVM Target
    jvm()

    // 2. Android Target
    androidTarget {
        publishLibraryVariants("release", "debug")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    // 3. iOS Targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    // 4. Source Sets
    sourceSets {

        // --- COMMON ---
        commonMain.dependencies {
            // Compose (używając helperów pluginu)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Kotlin X
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

            // Ktor Core & Common
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Settings
            implementation("com.russhwolf:multiplatform-settings:$multiplatformSettingsVersion")

            // Jetbrains Lifecycle & Navigation
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:$navVersion")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // --- ANDROID ---
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        }

        // --- JVM (Desktop/Server) ---
        jvmMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        }

        // --- iOS ---
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:$ktorVersion")
        }
    }
}

// Konfiguracja Androida (poza blokiem kotlin)
android {
    namespace = "com.example.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}