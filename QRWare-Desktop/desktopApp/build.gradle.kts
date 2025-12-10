import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                
                // Desktop-specific dependencies
                implementation(libs.kotlinx.coroutines.swing)
                implementation("org.jetbrains.compose.desktop:desktop-jvm:${libs.versions.compose.get()}")
            }
        }
        
        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.qrware.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "QRWare"
            packageVersion = "1.0.0"
            
            windows {
                menuGroup = "QRWare"
                upgradeUuid = "qrware-desktop-app"
            }
        }
    }
}