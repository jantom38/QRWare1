import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting {
            dependencies {
                // Shared module dependency
                implementation(project(":shared"))
                
                // Compose Desktop
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                
                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.swing)
                
                // DI
                implementation(libs.koin.core)
                
                // Network
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.java)
            }
        }
        
        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.qrware.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "QRWare Desktop"
            packageVersion = "1.0.0"
            description = "QRWare Warehouse Management System - Desktop Edition"
            copyright = "© 2024 QRWare. All rights reserved."
            vendor = "QRWare"
            
            windows {
                menuGroup = "QRWare"
                upgradeUuid = "qrware-desktop-app-uuid"
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
        }
        
        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
    }
}