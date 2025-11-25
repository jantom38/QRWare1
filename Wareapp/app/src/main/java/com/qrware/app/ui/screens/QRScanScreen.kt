package com.qrware.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.model.QRCodeType
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.QRCodeViewModel
import com.qrware.app.util.BarcodeAnalyzer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: QRCodeViewModel = viewModel(factory = appContainer.qrCodeViewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // === LOGIKA NAWIGACJI PO SKANOWANIU (POPRAWIONA) ===
    LaunchedEffect(scanResult) {
        scanResult?.let { result ->
            if (result.success && result.entityId != null) {
                // Definiujemy trasę docelową
                val route = when (result.type) {
                    QRCodeType.PRODUCT -> "product_details/${result.entityId}"
                    QRCodeType.INVENTORY_ITEM -> "inventory_details/${result.entityId}"
                    else -> null
                }

                if (route != null) {
                    navController.navigate(route) {
                        // === KLUCZOWA POPRAWKA ===
                        // Usuwamy ekran skanera (obecny ekran) ze stosu nawigacji.
                        // Dzięki temu po kliknięciu "Wstecz" w szczegółach,
                        // użytkownik wróci do ekranu PRZED skanerem (np. Menu/Lista),
                        // a nie z powrotem do kamery (co powodowało pętlę).
                        popUpTo(navController.currentBackStackEntry?.destination?.route ?: return@navigate) {
                            inclusive = true
                        }
                    }
                }
                // Czyścimy wynik, aby nie nawigować ponownie przy ewentualnym powrocie
                viewModel.clearScanResult()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skanuj Kod") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            // Konfiguracja analizy obrazu
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(
                                        Executors.newSingleThreadExecutor(),
                                        BarcodeAnalyzer { rawScannedContent ->

                                            // Sprawdzamy stan UI, aby nie skanować wielokrotnie
                                            if (!uiState.isScanning && !uiState.isLoading) {

                                                val separator = "###"

                                                // Parsowanie danych (hybrydowe QR)
                                                val systemId = if (rawScannedContent.contains(separator)) {
                                                    rawScannedContent.split(separator)[0]
                                                } else {
                                                    rawScannedContent
                                                }

                                                // Przekazujemy CZYSTE ID do ViewModelu
                                                viewModel.scanQRCode(systemId)
                                            }
                                        }
                                    )
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Nakładka (Overlay)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(250.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                )

                Text(
                    text = "Skieruj kamerę na kod QR",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )

            } else {
                // Brak uprawnień
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Brak uprawnień do kamery")
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Przyznaj uprawnienia")
                    }
                }
            }

            // Loader
            if (uiState.isScanning || uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Weryfikacja kodu...", color = Color.White)
                    }
                }
            }

            // Błędy
            uiState.error?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearMessages() },
                    title = { Text("Błąd skanowania") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}