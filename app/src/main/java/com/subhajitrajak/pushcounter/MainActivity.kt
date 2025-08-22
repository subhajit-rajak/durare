package com.subhajitrajak.pushcounter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
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

class MainActivity : AppCompatActivity() {
    
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceDetector: FaceDetector
    
    private lateinit var viewFinder: PreviewView
    private lateinit var cameraCard: CardView
    private lateinit var pushUpCount: TextView
    private lateinit var statusText: TextView
    private lateinit var faceSizeText: TextView
    private lateinit var cameraToggle: MaterialSwitch
    
    private var currentCount = 0
    private var isInDownPosition = false
    private var lastFacePercentage = 0f
    private var consecutiveDownFrames = 0
    private var consecutiveUpFrames = 0
    
    companion object {
        private const val TAG = "PushCounter"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        
        // Sensitivity settings for push-up detection
        private const val DOWN_THRESHOLD = 50f // Face takes more than 50% of screen = down position
        private const val UP_THRESHOLD = 25f   // Face takes more than 25% of screen = up position
        private const val FRAME_THRESHOLD = 3   // Need 3 consecutive frames to confirm position
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize views
        viewFinder = findViewById(R.id.viewFinder)
        cameraCard = findViewById(R.id.cameraCard)
        pushUpCount = findViewById(R.id.pushUpCount)
        statusText = findViewById(R.id.statusText)
        faceSizeText = findViewById(R.id.faceSizeText)
        cameraToggle = findViewById(R.id.cameraToggle)
        
        // Initialize face detector with high sensitivity
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setMinFaceSize(0.1f) // Detect very small faces for sensitivity
            .build()
        
        faceDetector = FaceDetection.getClient(options)
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Setup camera toggle
        cameraToggle.setOnCheckedChangeListener { _, isChecked ->
            cameraCard.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        // Request camera permissions
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
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FaceAnalyzer())
                }
            
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
                val face = faces[0] // Use the first detected face
                val boundingBox = face.boundingBox
                
                // Calculate face area percentage
                val faceArea = boundingBox.width() * boundingBox.height()
                val imageArea = imageWidth * imageHeight
                val facePercentage = (faceArea.toFloat() / imageArea * 100)
                
                lastFacePercentage = facePercentage
                faceSizeText.text = getString(R.string.face_size_format, facePercentage.toInt())
                
                // Determine position based on face size
                when {
                    facePercentage > DOWN_THRESHOLD -> {
                        consecutiveDownFrames++
                        consecutiveUpFrames = 0
                        
                        if (consecutiveDownFrames >= FRAME_THRESHOLD && !isInDownPosition) {
                            isInDownPosition = true
                            statusText.text = getString(R.string.status_go_up)
                        }
                    }
                    facePercentage > UP_THRESHOLD -> {
                        consecutiveUpFrames++
                        consecutiveDownFrames = 0
                        
                        if (consecutiveUpFrames >= FRAME_THRESHOLD && isInDownPosition) {
                            // Complete push-up
                            currentCount++
                            pushUpCount.text = currentCount.toString()
                            isInDownPosition = false
                            statusText.text = getString(R.string.status_great)
                        } else if (consecutiveUpFrames >= FRAME_THRESHOLD) {
                            statusText.text = getString(R.string.status_ready_pushup)
                        }
                    }
                    else -> {
                        consecutiveDownFrames = 0
                        consecutiveUpFrames = 0
                        statusText.text = getString(R.string.status_move_closer)
                    }
                }
            } else {
                faceSizeText.text = getString(R.string.face_size_format, 0)
                statusText.text = getString(R.string.status_no_face)
                consecutiveDownFrames = 0
                consecutiveUpFrames = 0
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceDetector.close()
    }
}