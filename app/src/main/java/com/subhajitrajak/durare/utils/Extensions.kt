package com.subhajitrajak.durare.utils

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
            String.format(Locale.US, "%.1fM", formatted).removeSuffix(".0M") + if (formatted % 1 == 0.0) "M" else ""
        }
        this >= 1_000 -> {
            val formatted = this / 1_000.0
            String.format(Locale.US, "%.1fK", formatted).removeSuffix(".0K") + if (formatted % 1 == 0.0) "K" else ""
        }
        else -> this.toString()
    }
}

fun Long.formatToShortNumber(): String {
    return when {
        this >= 1_000_000 -> {
            val formatted = this / 1_000_000.0
            String.format(Locale.US, "%.1fM", formatted).removeSuffix(".0M") + if (formatted % 1 == 0.0) "M" else ""
        }
        this >= 1_000 -> {
            val formatted = this / 1_000.0
            String.format(Locale.US, "%.1fK", formatted).removeSuffix(".0K") + if (formatted % 1 == 0.0) "K" else ""
        }
        else -> this.toString()
    }
}