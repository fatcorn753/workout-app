package com.tatu.workout.pushup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tatu.workout.data.formatSessionDuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun PushUpCounterScreen(
    onBack: () -> Unit,
    viewModel: PushUpCounterViewModel = viewModel(),
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreviewWithFaceAnalysis(onFaceHeight = viewModel::onFaceHeight)
        } else {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(120.dp))
                    Text(
                        "カメラへのアクセスが必要です",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "顔の上下動を見て腕立て伏せの回数を数えます。映像は保存も送信もされません。",
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("許可する")
                    }
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier.statusBarsPadding().padding(8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = Color.White)
        }

        if (hasCameraPermission) {
            CounterOverlay(
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
            )
        }
    }
}

@Composable
private fun CounterOverlay(viewModel: PushUpCounterViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), shape = MaterialTheme.shapes.large)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${viewModel.reps}",
            color = Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "回",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
        )

        Spacer(Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { viewModel.normalizedPosition ?: 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )

        Spacer(Modifier.height(8.dp))

        val faceLost = viewModel.isRunning && !viewModel.isFaceDetected
        Text(
            text = if (faceLost) "顔が見えていません" else formatSessionDuration(viewModel.elapsedMillis),
            color = if (faceLost) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.8f),
        )

        Spacer(Modifier.height(16.dp))

        if (viewModel.isRunning) {
            Button(
                onClick = viewModel::stop,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("終了して記録する")
            }
        } else {
            Button(onClick = viewModel::start, modifier = Modifier.fillMaxWidth()) {
                Text(if (viewModel.lastSavedReps != null) "もう一度始める" else "開始")
            }
            viewModel.lastSavedReps?.let { saved ->
                Spacer(Modifier.height(8.dp))
                Text("${saved}回を記録しました", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CameraPreviewWithFaceAnalysis(
    onFaceHeight: (Float?, Long) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember { FaceHeightAnalyzer(onFaceHeight) }

    DisposableEffect(Unit) {
        onDispose {
            analyzer.close()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            bindCamera(ctx, previewView, lifecycleOwner, cameraExecutor, analyzer)
            previewView
        },
    )
}

private fun bindCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    analyzer: FaceHeightAnalyzer,
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(cameraExecutor, analyzer) }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            preview,
            imageAnalysis,
        )
    }, ContextCompat.getMainExecutor(context))
}
