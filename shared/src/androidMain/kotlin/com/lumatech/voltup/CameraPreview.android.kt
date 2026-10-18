package com.lumatech.voltup

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
actual fun CameraPreview(
    onQrCodeScanned: (String) -> Unit,
    modifier: Modifier
) {
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

    var showPermissionDialog by remember { mutableStateOf(!hasCameraPermission) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                hasCameraPermission = granted
                if (granted) {
                    showPermissionDialog = false
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = modifier.background(Color(0xFF0F1115))
        ) {
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Text(
                            text = "Wymagany dostęp do kamery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Aby skanować kody QR na stacjach kinetycznych, włącz uprawnienie do aparatu w ustawieniach aplikacji.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    showPermissionDialog = false
                                    try {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                                shape = CircleShape
                            ) {
                                Text("Przejdź do ustawień", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showPermissionDialog = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = CircleShape
                            ) {
                                Text("Anuluj", fontWeight = FontWeight.Medium)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = null
                )
            }

            Button(
                onClick = {
                    val activity = context as? android.app.Activity
                    val shouldShowRationale = activity?.let {
                        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
                    } ?: false
                    if (shouldShowRationale) {
                        showPermissionDialog = true
                    } else {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp).padding(end = 6.dp)
                )
                Text(
                    text = "Zezwól na użycie kamery",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        var isScanned by remember { mutableStateOf(false) }
        val executor = remember { Executors.newSingleThreadExecutor() }

        DisposableEffect(Unit) {
            onDispose {
                try {
                    executor.shutdown()
                } catch (_: Exception) {}
            }
        }

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val barcodeScanner = BarcodeScanning.getClient()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !isScanned) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (!rawValue.isNullOrBlank() && !isScanned) {
                                            isScanned = true
                                            onQrCodeScanned(rawValue)
                                            break
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )
    }
}
