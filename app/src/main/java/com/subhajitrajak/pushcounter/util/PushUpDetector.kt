package com.subhajitrajak.pushcounter.util

import com.subhajitrajak.pushcounter.R
import kotlin.math.max
import kotlin.math.min

class PushUpDetector(
    private val listener: Listener
) {

    interface Listener {
        fun onCountChanged(count: Int)
        fun onStatusChanged(statusRes: Int)
        fun onFaceSizeChanged(percentage: Int)
        fun onPushUpCompleted()
    }

    private var currentCount = 0
    private var isInDownPosition = false
    private var smoothedFacePercentage = 0f

    private var consecutiveDownFrames = 0
    private var consecutiveUpFrames = 0
    private var consecutiveNoFaceFrames = 0

    companion object {
        private const val DOWN_THRESHOLD = 40f
        private const val UP_THRESHOLD = 25f
        private const val FRAME_THRESHOLD = 3
        private const val LOST_FACE_GRACE_FRAMES = 5
        private const val SMOOTHING_ALPHA = 0.25f
    }

    fun reset() {
        currentCount = 0
        isInDownPosition = false
        smoothedFacePercentage = 0f
        consecutiveDownFrames = 0
        consecutiveUpFrames = 0
        consecutiveNoFaceFrames = 0
        listener.onCountChanged(currentCount)
        listener.onStatusChanged(R.string.status_ready)
        listener.onFaceSizeChanged(0)
    }

    fun process(faceArea: Int, imageArea: Int) {
        consecutiveNoFaceFrames = 0

        val rawPercentage = (faceArea.toFloat() / imageArea * 100f)
        smoothedFacePercentage =
            if (smoothedFacePercentage == 0f) rawPercentage
            else SMOOTHING_ALPHA * rawPercentage + (1 - SMOOTHING_ALPHA) * smoothedFacePercentage

        val clampedDisplay = min(100, max(0, smoothedFacePercentage.toInt()))
        listener.onFaceSizeChanged(clampedDisplay)

        when {
            smoothedFacePercentage > DOWN_THRESHOLD -> {
                consecutiveDownFrames++
                consecutiveUpFrames = 0
                if (consecutiveDownFrames >= FRAME_THRESHOLD && !isInDownPosition) {
                    isInDownPosition = true
                    listener.onStatusChanged(R.string.status_go_up)
                }
            }
            smoothedFacePercentage > UP_THRESHOLD -> {
                consecutiveUpFrames++
                consecutiveDownFrames = 0
                if (consecutiveUpFrames >= FRAME_THRESHOLD && isInDownPosition) {
                    currentCount++
                    isInDownPosition = false
                    listener.onCountChanged(currentCount)
                    listener.onStatusChanged(R.string.status_great)
                    listener.onPushUpCompleted()
                } else if (consecutiveUpFrames >= FRAME_THRESHOLD) {
                    listener.onStatusChanged(R.string.status_ready_pushup)
                }
            }
            else -> {
                consecutiveDownFrames = 0
                consecutiveUpFrames = 0
                listener.onStatusChanged(R.string.status_move_closer)
            }
        }
    }

    fun onNoFace() {
        consecutiveNoFaceFrames++
        if (consecutiveNoFaceFrames >= LOST_FACE_GRACE_FRAMES) {
            listener.onStatusChanged(R.string.status_no_face)
            consecutiveDownFrames = 0
            consecutiveUpFrames = 0
        } else {
            listener.onStatusChanged(R.string.status_no_face)
        }
        listener.onFaceSizeChanged(0)
    }
}