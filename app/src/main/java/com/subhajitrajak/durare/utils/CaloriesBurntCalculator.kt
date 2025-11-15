package com.subhajitrajak.durare.utils

import java.util.Locale

fun calculatePushupCalories(
    weightKg: Float,           // user's weight in kilograms
    pushupCount: Int,           // total number of pushups done
    pushupsPerMinute: Float, // average pushup rate (default 25/min)
    metValue: Float = 8.0f      // MET value for pushups (moderate to vigorous)
): Float {
    // Calculate total exercise duration in hours
    val durationHours = (pushupCount / pushupsPerMinute) / 60.0

    // Calories burned formula
    val caloriesBurned = metValue * weightKg * durationHours

    // Round to 2 decimal places for readability
    return String.format(Locale.US, "%.2f", caloriesBurned).toFloat()
}