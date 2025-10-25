package com.subhajitrajak.durare.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.view.isVisible

fun View.fadeView(
    duration: Long = 1000,
    from: Float = 0f,
    to: Float = 1f
) {
    animate().cancel()
    animate()
        .alpha(to)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .withStartAction { alpha = from }
        .start()
}

fun View.removeWithAnim(duration: Long = 100) {
    if (!isVisible) return
    fadeView(from = 1f, to = 0f, duration = duration)
    visibility = View.GONE
}

fun View.hideWithAnim(duration: Long = 100) {
    if (!isVisible) return
    fadeView(from = 1f, to = 0f, duration = duration)
    visibility = View.INVISIBLE
}

fun View.showWithAnim50ms() {
    visibility = View.VISIBLE
    fadeView(duration = 50)
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun log(message: String) {
    Log.e("Personal", message)
}

fun View.showWithAnim(duration: Long = 500) {
    if (isVisible) return
    visibility = View.VISIBLE
    fadeView(duration = duration)
}

fun View.show() {
    alpha = 1f
    visibility = View.VISIBLE
}

fun View.remove() {
    visibility = View.GONE
}