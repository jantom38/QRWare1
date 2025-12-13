package com.example.shared

// imporale gdzt androidx.compose.material3.MaterialTheme // not used here
import androidx.compose.runtime.Composable
import com.example.shared.di.AppContainer
import com.example.shared.ui.navigation.AppNavigation
 import com.example.shared.ui.theme.QRWareAppTheme

@Composable
fun App(appContainer: AppContainer) {
    QRWareAppTheme {
        AppNavigation(appContainer = appContainer)
    }
}