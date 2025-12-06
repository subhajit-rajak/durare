package com.subhajitrajak.durare.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import kotlin.math.hypot

object ThemeSwitcher {
    private var lastScreenshot: Bitmap? = null
    private var isAnimating = false
    const val DURATION: Long = 600

    fun switchThemeWithAnimation(activity: Activity, isDark: Boolean) {
        if (isAnimating) return
        isAnimating = true

        // take a screenshot of the CURRENT (Old) screen
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        lastScreenshot = getBitmapFromView(rootView)

        // apply the new theme preference
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun animateActivity(activity: Activity) {
        val screenshot = lastScreenshot ?: return
        lastScreenshot = null // clear memory
        isAnimating = false // reset flag

        val decorView = activity.window.decorView as ViewGroup
        val contentView = activity.findViewById<View>(android.R.id.content)

        // hides content temporarily to prevents flashes of the new theme appearing before the animation starts.
        contentView.visibility = View.INVISIBLE

        // create an ImageView to hold the OLD screen
        val backgroundOverlay = ImageView(activity).apply {
            setImageBitmap(screenshot)
            scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // adds the OLD screen BEHIND the NEW content
        decorView.addView(backgroundOverlay, 0)

        // prepares the Circular Reveal on the NEW Content
        // The new content (Dark mode) will start with radius 0 (invisible),
        // revealing the backgroundOverlay (Light mode) behind it.
        contentView.post {
            val w = contentView.width
            val h = contentView.height
            val finalRadius = hypot(w.toDouble(), h.toDouble()).toFloat()

            // animation start position (center of screen)
            val cx = w / 2
            val cy = h / 2

            try {
                val anim = ViewAnimationUtils.createCircularReveal(
                    contentView, cx, cy, 0f, finalRadius
                )
                anim.duration = DURATION
                anim.interpolator = AccelerateInterpolator()

                // makes the view visible since the animation is ready
                // Since radius starts at 0, it will look invisible initially, giving a smooth start.
                contentView.visibility = View.VISIBLE

                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        // cleanup
                        decorView.removeView(backgroundOverlay)
                    }
                })
                anim.start()
            } catch (_: Exception) {
                // fallback if animation fails
                decorView.removeView(backgroundOverlay)
            }
        }
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        if (view.width == 0 || view.height == 0) return null
        return try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}