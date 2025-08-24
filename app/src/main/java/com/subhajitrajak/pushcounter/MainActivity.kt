package com.subhajitrajak.pushcounter

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceDetector: FaceDetector
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    private lateinit var viewFinder: PreviewView
    private lateinit var cameraCard: CardView
    private lateinit var pushUpCount: TextView
    private lateinit var statusText: TextView
    private lateinit var faceSizeText: TextView
    private lateinit var cameraToggle: MaterialSwitch
    private lateinit var feedbackToggle: MaterialSwitch
    private lateinit var resetButton: Button

    private var currentCount = 0
    private var isInDownPosition = false
    private var lastFacePercentage = 0f
    private var smoothedFacePercentage = 0f
    private var consecutiveDownFrames = 0
    private var consecutiveUpFrames = 0
    private var consecutiveNoFaceFrames = 0

    companion object {
        private const val TAG = "PushCounter"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        // Detection thresholds (kept same semantics as your original logic)
        private const val DOWN_THRESHOLD = 40f  // % of frame area -> DOWN position
        private const val UP_THRESHOLD = 25f    // % of frame area -> UP position
        private const val FRAME_THRESHOLD = 3   // frames to confirm a state

        // New: short grace period for lost-face, smoothing for jitter
        private const val LOST_FACE_GRACE_FRAMES = 5      // tolerate brief occlusions
        private const val SMOOTHING_ALPHA = 0.25f         // EMA factor for face % (0..1)

        // Preview/analysis tuning
        private const val ANALYSIS_STRATEGY = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Respect system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Keep screen on during workout
        window.decorView.keepScreenOn = true

        // Init views
        viewFinder = findViewById(R.id.viewFinder)
        cameraCard = findViewById(R.id.cameraCard)
        pushUpCount = findViewById(R.id.pushUpCount)
        statusText = findViewById(R.id.statusText)
        faceSizeText = findViewById(R.id.faceSizeText)
        cameraToggle = findViewById(R.id.cameraToggle)
        feedbackToggle = findViewById(R.id.feedbackToggle)
        resetButton = findViewById(R.id.resetButton)

        // ML Kit face detector (lean config: only what we actually use)
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            // We only use bounding box; turn off extras for speed
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.1f) // sensitive to small faces
            .build()

        faceDetector = FaceDetection.getClient(options)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Feedback helpers
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Toggle camera preview visibility only (analysis continues)
        cameraToggle.setOnCheckedChangeListener { _, isChecked ->
            cameraCard.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Reset button
        resetButton.setOnClickListener {
            currentCount = 0
            pushUpCount.text = currentCount.toString()
            isInDownPosition = false
            consecutiveDownFrames = 0
            consecutiveUpFrames = 0
            consecutiveNoFaceFrames = 0
            statusText.text = getString(R.string.status_ready)
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also { it.surfaceProvider = viewFinder.surfaceProvider }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ANALYSIS_STRATEGY)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also { it.setAnalyzer(cameraExecutor, FaceAnalyzer()) }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(this, "Camera failed to start", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                statusText.text = getString(R.string.status_camera_permission)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "Camera permission is needed to count push-ups.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                faceDetector.process(image)
                    .addOnSuccessListener { faces ->
                        processFaces(faces, imageProxy.width, imageProxy.height)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Face detection failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    private fun processFaces(faces: List<Face>, imageWidth: Int, imageHeight: Int) {
        runOnUiThread {
            if (faces.isNotEmpty()) {
                // Reset lost-face grace if we see a face
                consecutiveNoFaceFrames = 0

                val face = faces[0] // Use the first detected face
                val boundingBox = face.boundingBox

                // Calculate face area percentage
                val faceArea = max(0, boundingBox.width()) * max(0, boundingBox.height())
                val imageArea = max(1, imageWidth) * max(1, imageHeight)
                val rawPercentage = (faceArea.toFloat() / imageArea * 100f)

                // Exponential Moving Average to reduce jitter
                smoothedFacePercentage =
                    if (smoothedFacePercentage == 0f) rawPercentage
                    else (SMOOTHING_ALPHA * rawPercentage + (1 - SMOOTHING_ALPHA) * smoothedFacePercentage)

                lastFacePercentage = smoothedFacePercentage
                val clampedDisplay = min(100, max(0, smoothedFacePercentage.toInt()))
                faceSizeText.text = getString(R.string.face_size_format, clampedDisplay)

                // Determine position based on smoothed face size
                when {
                    smoothedFacePercentage > DOWN_THRESHOLD -> {
                        consecutiveDownFrames++
                        consecutiveUpFrames = 0

                        if (consecutiveDownFrames >= FRAME_THRESHOLD && !isInDownPosition) {
                            isInDownPosition = true
                            statusText.text = getString(R.string.status_go_up)
                        }
                    }
                    smoothedFacePercentage > UP_THRESHOLD -> {
                        consecutiveUpFrames++
                        consecutiveDownFrames = 0

                        if (consecutiveUpFrames >= FRAME_THRESHOLD && isInDownPosition) {
                            // Complete push-up
                            currentCount++
                            pushUpCount.text = currentCount.toString()
                            isInDownPosition = false
                            statusText.text = getString(R.string.status_great)
                            emitFeedback()
                        } else if (consecutiveUpFrames >= FRAME_THRESHOLD) {
                            statusText.text = getString(R.string.status_ready_pushup)
                        }
                    }
                    else -> {
                        // Too small: probably too far
                        consecutiveDownFrames = 0
                        consecutiveUpFrames = 0
                        statusText.text = getString(R.string.status_move_closer)
                    }
                }
            } else {
                // Gracefully handle brief occlusions: don't instantly reset the state
                consecutiveNoFaceFrames++
                val remaining = (LOST_FACE_GRACE_FRAMES - consecutiveNoFaceFrames).coerceAtLeast(0)
                faceSizeText.text = getString(R.string.face_size_format, 0)

                if (consecutiveNoFaceFrames >= LOST_FACE_GRACE_FRAMES) {
                    statusText.text = getString(R.string.status_no_face)
                    consecutiveDownFrames = 0
                    consecutiveUpFrames = 0
                    // Don't forcibly flip isInDownPosition; let user resume naturally
                } else {
                    statusText.text = getString(R.string.status_no_face) + " ($remaining)"
                }
            }
        }
    }

    private fun emitFeedback() {
        if (!feedbackToggle.isChecked) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (_: Exception) { /* ignore */ }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60)
            }
        } catch (_: Exception) { /* ignore */ }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            cameraExecutor.shutdown()
        } catch (_: Exception) { }
        try {
            faceDetector.close()
        } catch (_: Exception) { }
        try {
            toneGenerator?.release()
        } catch (_: Exception) { }
        window.decorView.keepScreenOn = false
    }
}