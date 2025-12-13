package com.qrware.desktop


import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.shared.App
import com.example.shared.di.AppContainer
import com.russhwolf.settings.MapSettings

fun main() = application {
    val settings = MapSettings()
    val appContainer = AppContainer(settings)
    Window(onCloseRequest = ::exitApplication, title = "QRWare Desktop") {
        App(appContainer)
    }
}
