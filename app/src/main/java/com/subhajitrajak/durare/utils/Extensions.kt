package com.subhajitrajak.durare.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Int.formatTwoDigits(): String {
    return if (this < 10 && this >= 0) {
        "0$this"
    } else {
        this.toString()
    }
}

fun Int.formatWithCommas(): String {
    return String.format(Locale.US, "%,d", this)
}

fun Int.formatToShortNumber(): String {
    return when {
        this >= 1_000_000 -> {
            val formatted = this / 1_000_000.0
            String.format(Locale.US, "%.1fM", formatted)
                .removeSuffix(".0M") + if (formatted % 1 == 0.0) "M" else ""
        }

        this >= 1_000 -> {
            val formatted = this / 1_000.0
            String.format(Locale.US, "%.1fK", formatted)
                .removeSuffix(".0K") + if (formatted % 1 == 0.0) "K" else ""
        }

        else -> this.toString()
    }
}

fun Long.formatToShortNumber(): String {
    return when {
        this >= 1_000_000 -> {
            val formatted = this / 1_000_000.0
            String.format(Locale.US, "%.1fM", formatted)
                .removeSuffix(".0M") + if (formatted % 1 == 0.0) "M" else ""
        }

        this >= 1_000 -> {
            val formatted = this / 1_000.0
            String.format(Locale.US, "%.1fK", formatted)
                .removeSuffix(".0K") + if (formatted % 1 == 0.0) "K" else ""
        }

        else -> this.toString()
    }
}

fun String.getFormattedDate(): String {
    val date = LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    val day = date.dayOfMonth
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }

    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
    val monthInText = date.format(monthFormatter) + "."

    return "$day$suffix $monthInText ${date.year}"
}

fun Long.getFormattedTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (minutes > 0) append("${minutes}m ")
        append("${seconds}s")
    }
}