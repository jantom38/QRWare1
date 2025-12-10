package com.qrware.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.qrware.desktop.ui.QRWareDesktopApp
import com.qrware.shared.di.NetworkDI

fun main() = application {
    // Initialize Network DI with default server URL
    NetworkDI.initialize(
        baseUrl = "http://localhost:8080",
        enableLogging = true
    )
    
    Window(
        onCloseRequest = {
            // Clean up network resources
            NetworkDI.dispose()
            exitApplication()
        },
        title = "QRWare - Warehouse Management System",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        QRWareDesktopApp()
    }
}