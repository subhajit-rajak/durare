package com.subhajitrajak.durare.utils

open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    /**
     * Returns the content and prevents it from being used again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}