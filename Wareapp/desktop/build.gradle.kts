plugins {
    // 1. NAPRAWA: Zmieniono 'kotlin.compose' na 'compose.compiler' (zgodnie z TOML)
    alias(libs.plugins.compose.compiler)

    // Plugin Kotlin JVM
    kotlin("jvm")

    // 2. NAPRAWA: Używamy aliasu z TOML zamiast wpisywać wersję na sztywno
    // Dzięki temu wersja (1.7.0) będzie zgodna z resztą projektu
    alias(libs.plugins.jetbrains.compose)
}

group = "com.qrware.desktop"
version = "1.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.russhwolf:multiplatform-settings:1.1.1")
    implementation(project(":shared"))

    // To zadziała poprawnie dzięki pluginowi jetbrains.compose
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.qrware.desktop.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "QRWareDesktop"
            packageVersion = "1.0.0"
        }
    }
}