package com.tatu.workout.pushup

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * フレームごとに一番大きく写っている顔のバウンディングボックス高さ(px)を返す。
 * 検出に失敗、あるいは顔が写っていないフレームは null を返す。
 */
class FaceHeightAnalyzer(
    private val onResult: (heightPx: Float?, timestampMillis: Long) -> Unit,
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener { faces ->
                val face = faces.maxByOrNull { it.boundingBox.height() }
                onResult(face?.boundingBox?.height()?.toFloat(), System.currentTimeMillis())
            }
            .addOnFailureListener {
                onResult(null, System.currentTimeMillis())
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun close() {
        detector.close()
    }
}
