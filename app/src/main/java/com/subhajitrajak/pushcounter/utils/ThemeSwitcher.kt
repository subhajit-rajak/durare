package com.subhajitrajak.pushcounter.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import kotlin.math.hypot
import androidx.core.graphics.createBitmap

object ThemeSwitcher {

    private var isAnimating = false
    private var currentReveal: Animator? = null
    private var currentFade: Animator? = null

    fun switchThemeWithAnimation(activity: Activity, isDark: Boolean) {
        if (isAnimating) return

        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        // Cancel any running animations
        currentReveal?.cancel()
        currentFade?.cancel()

        isAnimating = true

        // Remove leftover overlays if any
        (0 until rootView.childCount)
            .map { rootView.getChildAt(it) }
            .filter { it.tag == "themeOverlay" }
            .forEach { rootView.removeView(it) }

        // Take screenshot of the current theme
        val bitmap = getBitmapFromView(rootView)
        val oldOverlay = ImageView(activity).apply {
            setImageBitmap(bitmap)
            tag = "themeOverlay"
        }
        rootView.addView(oldOverlay,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Apply the new theme immediately
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        // New overlay for circular reveal
        val newOverlay = View(activity).apply {
            setBackgroundColor(if (isDark) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            alpha = 1f
            tag = "themeOverlay"
        }
        rootView.addView(newOverlay,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val centerX = rootView.width / 2
        val centerY = rootView.height / 2
        val finalRadius = hypot(rootView.width.toDouble(), rootView.height.toDouble()).toFloat()

        currentReveal = ViewAnimationUtils.createCircularReveal(
            newOverlay, centerX, centerY, 0f, finalRadius
        ).apply {
            duration = 600
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootView.removeView(newOverlay)
                    rootView.removeView(oldOverlay)
                    currentReveal = null
                    currentFade = null
                    isAnimating = false
                }
                override fun onAnimationCancel(animation: Animator) {
                    rootView.removeView(newOverlay)
                    rootView.removeView(oldOverlay)
                    currentReveal = null
                    currentFade = null
                    isAnimating = false
                }
            })
        }

        currentFade = ObjectAnimator.ofFloat(oldOverlay, "alpha", 1f, 0f).apply {
            duration = 600
        }

        currentReveal?.start()
        currentFade?.start()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}