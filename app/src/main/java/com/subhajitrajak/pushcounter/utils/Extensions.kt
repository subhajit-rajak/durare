package com.subhajitrajak.pushcounter.utils

fun Int.formatTwoDigits(): String {
    return if (this < 10 && this >= 0) {
        "0$this"
    } else {
        this.toString()
    }
}