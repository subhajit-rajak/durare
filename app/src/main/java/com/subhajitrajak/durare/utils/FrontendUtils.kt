package com.subhajitrajak.durare.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast

fun View.fadeView(
    duration: Long = 1000,
    from: Float = 0f,
    to: Float = 1f,
) {
    alpha = from
    animate()
        .alpha(to)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .start()
}

fun View.removeWithAnim() {
    visibility = View.GONE
    fadeView(from = 1f, to = 0f, duration = 100)
}

fun View.hideWithAnim() {
    visibility = View.INVISIBLE
    fadeView(from = 1f, to = 0f, duration = 100)
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
    visibility = View.VISIBLE
    fadeView(duration = duration)
}

fun View.show() {
    visibility = View.VISIBLE
}