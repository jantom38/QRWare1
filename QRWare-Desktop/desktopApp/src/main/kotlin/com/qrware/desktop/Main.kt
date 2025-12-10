package com.qrware.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.qrware.desktop.ui.QRWareDesktopApp
import com.qrware.shared.di.SharedDI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun main() = application {
    // Initialize shared dependencies
    val appScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    SharedDI.initialize(
        baseUrl = "http://localhost:8080", // TODO: Make this configurable
        scope = appScope
    )
    
    Window(
        onCloseRequest = {
            // Clean up resources
            SharedDI.dispose()
            exitApplication()
        },
        title = "QRWare - Warehouse Management System",
    ) {
        QRWareDesktopApp()
    }
}