package com.qrware.app.ui.screens.OrderManagement

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.qrware.app.data.model.*
import com.qrware.app.data.repository.OrderItemRepository
import com.qrware.app.data.repository.OrderRepository
import com.qrware.app.ui.viewmodel.OrderManagement.QRScanOrderViewModel
import com.qrware.app.ui.viewmodel.OrderManagement.QRScanOrderViewModelFactory
import com.qrware.app.util.BarcodeAnalyzer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanOrderScreen(
    orderId: Long,
    navController: NavController,
    orderRepository: OrderRepository,
    orderItemRepository: OrderItemRepository
) {
    val context = LocalContext.current

    // TUTAJ BYŁ BŁĄD - Teraz używamy nazwanych argumentów, żeby kolejność była zawsze dobra
    val viewModel: QRScanOrderViewModel = viewModel(
        factory = QRScanOrderViewModelFactory(
            orderRepository = orderRepository,
            orderItemRepository = orderItemRepository,
            orderId = orderId
        )
    )

    // 2. Pobieranie stanu z ViewModelu
    val uiState by viewModel.uiState.collectAsState()

    // 3. Stan lokalny dla nagłówka
    var orderHeader by remember { mutableStateOf<OrderDTO?>(null) }

    // 4. Obsługa uprawnień kamery
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(orderId) {
        orderRepository.getOrderById(orderId)
            .onSuccess { orderHeader = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skanowanie - ${orderHeader?.orderNumber ?: "..."}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!uiState.isScanning) {
                                viewModel.resetScanner()
                            }
                        }
                    ) {
                        Icon(
                            if (uiState.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isScanning) "Skanowanie aktywne" else "Wznów skanowanie",
                            tint = if (uiState.isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sprawdzenie uprawnień przed wyświetleniem kamery
            if (hasCameraPermission) {
                if (uiState.isScanning) {
                    // --- WIDOK KAMERY Z ANALIZATOREM ---
                    QRScannerView(
                        onQRCodeScanned = { qrCode ->
                            viewModel.onQrScanned(qrCode)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    QRScanningOverlay()

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                } else {
                    // --- WIDOK WYNIKÓW / BŁĘDÓW (Gdy skanowanie zatrzymane) ---
                    ResultView(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            } else {
                // --- BRAK UPRAWNIEŃ ---
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Wymagany dostęp do kamery")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Przyznaj uprawnienia")
                        }
                    }
                }
            }
        }
    }
}

// --- ZINTEGROWANY WIDOK KAMERY ---

@Composable
fun QRScannerView(
    onQRCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Executor dla analizy w tle
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
                .also {
                    // Tutaj podpinamy Twój BarcodeAnalyzer
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        // Callback z analyzera
                        onQRCodeScanned(barcode)
                    })
                }

            try {
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        imageSelector,
                        preview,
                        imageAnalysis
                    )
                }, ContextCompat.getMainExecutor(ctx))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            previewView
        }
    )
}

// --- WIDOK WYNIKÓW (Wydzielony dla czytelności) ---

@Composable
fun ResultView(
    uiState: com.qrware.app.ui.viewmodel.OrderManagement.QRScanUiState,
    viewModel: QRScanOrderViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Przetwarzanie...")
            }

            uiState.scannedItem != null -> {
                QRScanResultCard(
                    orderItem = uiState.scannedItem!!,
                    onCompleteItem = { orderItem, quantity, notes ->
                        viewModel.completeItem(orderItem, quantity, notes)
                    },
                    onPickItem = { orderItem ->
                        viewModel.pickItem(orderItem)
                    }
                )

                if (uiState.successMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.successMessage!!,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.resetScanner() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Skanuj następny")
                    }
                }
            }

            uiState.error != null -> {
                QRScanErrorCard(
                    error = uiState.error!!,
                    onRetry = { viewModel.resetScanner() }
                )
            }

            else -> {
                Text("Skanowanie zatrzymane.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.resetScanner() }) {
                    Text("Wznów skanowanie")
                }
            }
        }
    }
}

// --- POZOSTAŁE ELEMENTY UI (Overlay, Karty) ---

@Composable
fun QRScanningOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(250.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {}

        Text(
            text = "Umieść kod QR w ramce",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanResultCard(
    orderItem: OrderItemDTO,
    onCompleteItem: (OrderItemDTO, Int, String) -> Unit,
    onPickItem: (OrderItemDTO) -> Unit
) {
    var showCompleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Sukces",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Produkt rozpoznany!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Nazwa: ${orderItem.productName}", style = MaterialTheme.typography.titleMedium)
                Text(text = "SKU: ${orderItem.productSku}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Żądana ilość: ${orderItem.requestedQuantity}")
                    Text("Zrealizowano: ${orderItem.completedQuantity}", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (orderItem.status) {
                    OrderItemStatus.PENDING -> {
                        Button(
                            onClick = { onPickItem(orderItem) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pobierz")
                        }
                    }
                    OrderItemStatus.IN_PROGRESS, OrderItemStatus.PICKED -> {
                        Button(
                            onClick = { showCompleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Realizuj")
                        }
                    }
                    OrderItemStatus.COMPLETED -> {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            enabled = false
                        ) {
                            Text("Zakończone")
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    if (showCompleteDialog) {
        CompleteOrderItemDialog(
            orderItem = orderItem,
            onDismiss = { showCompleteDialog = false },
            onComplete = { quantity, notes ->
                onCompleteItem(orderItem, quantity, notes)
                showCompleteDialog = false
            }
        )
    }
}

@Composable
fun QRScanErrorCard(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Błąd",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Błąd skanowania",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Spróbuj ponownie")
            }
        }
    }
}
