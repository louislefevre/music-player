package com.example.musicplayer.misc

open class Event<out T>(private val data: T) {

    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            data
        }
    }

    // Not needed in this project, but useful for other projects.
    // Used in case you need the data even if it has been handled.
    fun peekContent() = data
}
