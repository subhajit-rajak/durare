package com.subhajitrajak.pushcounter.ui.counter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.data.models.DailyPushStats
import com.subhajitrajak.pushcounter.data.repositories.StatsRepository
import com.subhajitrajak.pushcounter.databinding.ActivityCounterBinding
import com.subhajitrajak.pushcounter.ui.shareStats.ShareStatsActivity
import com.subhajitrajak.pushcounter.utils.Constants.DATE_FORMAT
import com.subhajitrajak.pushcounter.utils.Preferences
import com.subhajitrajak.pushcounter.utils.PushUpDetector
import com.subhajitrajak.pushcounter.utils.log
import com.subhajitrajak.pushcounter.utils.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max

class CounterActivity : AppCompatActivity(), PushUpDetector.Listener {

    private val binding: ActivityCounterBinding by lazy {
        ActivityCounterBinding.inflate(layoutInflater)
    }

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceDetector: FaceDetector
    private lateinit var pushUpDetector: PushUpDetector
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private var showCameraCardSwitch: Boolean = false
    private var counterFeedbackSwitch: Boolean = false

    // Session/rep state
    private var totalReps: Int = 3
    private var currentRep: Int = 1
    private var currentRepCount: Int = 0
    private var completedRepsPushUpsSum: Int = 0
    private val totalPushUps: Int
        get() = completedRepsPushUpsSum + currentRepCount

    // Timer state
    private var isPaused: Boolean = false
    private var isResting: Boolean = false
    private val uiHandler: Handler = Handler(Looper.getMainLooper())
    private var activeAccumulatedMs: Long = 0L
    private var restAccumulatedMs: Long = 0L
    private var lastTickStartMs: Long = 0L
    private var restRemainingMs: Long = 0L
    private var customRestMs: Long = 5000L
    private val tickRunnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            if (!isPaused) {
                val delta = now - lastTickStartMs
                if (isResting) {
                    restAccumulatedMs += delta
                    restRemainingMs = max(0, restRemainingMs - delta)
                    if (restRemainingMs <= 0) {
                        exitRestAndStartNextRep()
                    }
                } else {
                    activeAccumulatedMs += delta
                }
                updateTimerText()
            }
            lastTickStartMs = now
            uiHandler.postDelayed(this, 1000L)
        }
    }

    // Push duration tracking
    private var lastPushTimestampMs: Long = 0L
    private var cumulativePushDurationMs: Long = 0L

    // Repo
    private val statsRepository: StatsRepository by lazy { StatsRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get dynamic values from intent extras
        totalReps = intent.getIntExtra(Preferences.KEY_TOTAL_REPS, 3)
        customRestMs = intent.getLongExtra(Preferences.KEY_REST_TIME, 5000L)

        // keeps screen on during push-ups
        window.decorView.keepScreenOn = true

        binding.backButton.setOnClickListener {
            finish()
        }

        // Init helpers
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Detector
        pushUpDetector = PushUpDetector(this)

        // Face detector
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.1f)
            .build()

        faceDetector = FaceDetection.getClient(options)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // initializing preferences
        val prefs = Preferences.getInstance(this)
        showCameraCardSwitch = prefs.isCameraCardEnabled()
        counterFeedbackSwitch = prefs.isCounterFeedbackEnabled()

        // Camera card
        binding.cameraCard.visibility = if (showCameraCardSwitch) View.VISIBLE else View.INVISIBLE

        binding.resetButton.setOnClickListener { pushUpDetector.reset() }
        binding.playPause.setOnClickListener { togglePause() }
        binding.done.setOnClickListener { onDoneClicked() }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Initialize UI
        updateRepTitle()
        updateTotalCount()
        updateTimerText()
        startUiTicker()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build().also { it.surfaceProvider = binding.viewFinder.surfaceProvider }

            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(resolutionSelector)
                .build().also { it.setAnalyzer(cameraExecutor, FaceAnalyzer()) }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                log("Camera start failed - ${exc.message}")
                showToast(this, "Camera failed to start")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startUiTicker() {
        lastTickStartMs = System.currentTimeMillis()
        uiHandler.removeCallbacks(tickRunnable)
        uiHandler.post(tickRunnable)
    }

    private fun togglePause() {
        isPaused = !isPaused
        val iconRes = if (isPaused) R.drawable.play else R.drawable.pause
        binding.playPauseIcon.setImageResource(iconRes)
    }

    private fun updateRepTitle() {
        binding.repCount.text = getString(R.string.rep_1).replace("1", "$currentRep/$totalReps")
    }

    private fun updateTotalCount() {
        binding.totalCountText.text = totalPushUps.toString()
    }

    private fun updateTimerText() {
        val displayMs = if (isResting) restRemainingMs else activeAccumulatedMs
        binding.timerText.text = formatDuration(displayMs)
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = (ms / 1000L).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun onDoneClicked() {
        if (isResting) return
        // Finish current rep: move currentRepCount into completed sum, then reset per-rep counter
        completedRepsPushUpsSum += currentRepCount
        currentRepCount = 0
        binding.pushUpCount.text = currentRepCount.toString()
        updateTotalCount()

        if (currentRep >= totalReps) {
            completeSessionAndExit()
        } else {
            enterRest()
        }
    }

    private fun enterRest() {
        isResting = true
        restRemainingMs = customRestMs
        // Prepare for next rep: freeze counting during rest
        binding.statusText.text = getString(R.string.status_ready)
    }

    private fun exitRestAndStartNextRep() {
        isResting = false
        currentRep++
        if (currentRep > totalReps) {
            completeSessionAndExit()
            return
        }
        // Reset per-rep counter for UI but keep totalPushUps
        currentRepCount = 0
        pushUpDetector.reset()
        updateRepTitle()
        updateTotalCount()
    }

    private fun completeSessionAndExit() {
        // Persist stats
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val dateKey = SimpleDateFormat(DATE_FORMAT, Locale.US).format(Date())
        val avgPushDuration = if (totalPushUps > 0) cumulativePushDurationMs / totalPushUps else 0L
        val stats = DailyPushStats(
            date = dateKey,
            totalReps = totalReps,
            totalPushups = totalPushUps,
            totalActiveTimeMs = activeAccumulatedMs,
            averagePushDurationMs = avgPushDuration,
            totalRestTimeMs = restAccumulatedMs
        )
        if (uid == null) {
            showToast(this, "Couldn't save stats, please login again.")
            finish()
            return
        }
        statsRepository.saveOrAccumulateDaily(uid, dateKey, stats)
            .addOnSuccessListener {
                navigateToShareStats(stats)
            }
            .addOnFailureListener { err ->
                log("Failed to save stats - ${err.message}")
                showToast(this, "Failed to save stats")
                finish()
            }
    }

    private fun navigateToShareStats(stats: DailyPushStats) {
        val pushUps = stats.totalPushups.toString()
        val timeMinutes = stats.totalActiveTimeMs / 1000 / 60
        val timeSeconds = (stats.totalActiveTimeMs / 1000) % 60
        val time = "${timeMinutes}m ${timeSeconds}s"

        val restMinutes = stats.totalRestTimeMs / 1000 / 60
        val restSeconds = (stats.totalRestTimeMs / 1000) % 60
        val rest = "${restMinutes}m ${restSeconds}s"

        val intent = Intent(this, ShareStatsActivity::class.java).apply {
            putExtra(ShareStatsActivity.EXTRA_PUSH_UPS, pushUps)
            putExtra(ShareStatsActivity.EXTRA_TIME, time)
            putExtra(ShareStatsActivity.EXTRA_REST, rest)
        }
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera()
        } else {
            binding.statusText.text = getString(R.string.status_camera_permission)
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
                        if (faces.isNotEmpty()) {
                            val face = faces[0]
                            val faceArea = max(0, face.boundingBox.width()) * max(
                                0,
                                face.boundingBox.height()
                            )
                            val imageArea = max(1, imageProxy.width) * max(1, imageProxy.height)
                            pushUpDetector.process(faceArea, imageArea)
                        } else {
                            pushUpDetector.onNoFace()
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else imageProxy.close()
        }
    }

    override fun onCountChanged(count: Int) {
        if (isResting || isPaused) return
        // count is per detector session; map it to current rep count
        currentRepCount = count
        binding.pushUpCount.text = currentRepCount.toString()
        updateTotalCount()
    }

    override fun onStatusChanged(statusRes: Int) {
        binding.statusText.text = getString(statusRes)
    }

    override fun onFaceSizeChanged(percentage: Int) {
        binding.faceSizeText.text = getString(R.string.face_size_format, percentage)
    }

    override fun onPushUpCompleted() {
        if (isResting || isPaused) return
        // Track average push duration
        val now = System.currentTimeMillis()
        if (lastPushTimestampMs != 0L) cumulativePushDurationMs += (now - lastPushTimestampMs)
        lastPushTimestampMs = now
        if (!counterFeedbackSwitch) return
        try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120) } catch (_: Exception) {}
        try {
            vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceDetector.close()
        toneGenerator?.release()
        window.decorView.keepScreenOn = false
        uiHandler.removeCallbacksAndMessages(null)
    }
}