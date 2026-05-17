package com.dusht.core.logging

import timber.log.Timber

/**
 * Structured debug logging for Timber. Plant [Timber.DebugTree] in debug builds from [Application][android.app.Application].
 * Tags group events for filtering: API, NAV, LIFECYCLE, APP.
 */
object AppLogger {

    private const val TAG_API = "API"
    private const val TAG_NAV = "NAV"
    private const val TAG_LIFECYCLE = "LIFECYCLE"
    private const val TAG_APP = "APP"
    private const val TAG_FIREBASE = "FIREBASE"

    fun api(
        message: String,
        extras: Map<String, Any?> = emptyMap(),
        throwable: Throwable? = null
    ) {
        log(TAG_API, message, extras, throwable)
    }

    fun navigation(
        message: String,
        extras: Map<String, Any?> = emptyMap()
    ) {
        log(TAG_NAV, message, extras, null)
    }

    fun lifecycle(
        message: String,
        extras: Map<String, Any?> = emptyMap()
    ) {
        log(TAG_LIFECYCLE, message, extras, null)
    }

    fun app(
        message: String,
        extras: Map<String, Any?> = emptyMap(),
        throwable: Throwable? = null
    ) {
        log(TAG_APP, message, extras, throwable)
    }

    /** Firestore / Firebase Auth — filter Logcat tag `FIREBASE`. */
    fun firebase(
        message: String,
        extras: Map<String, Any?> = emptyMap(),
        throwable: Throwable? = null
    ) {
        log(TAG_FIREBASE, message, extras, throwable)
    }

    private fun log(
        tag: String,
        message: String,
        extras: Map<String, Any?>,
        throwable: Throwable?
    ) {
        val suffix = if (extras.isEmpty()) {
            ""
        } else {
            " | " + extras.entries.joinToString(", ") { "${it.key}=${it.value}" }
        }
        val line = "[$tag] $message$suffix"
        if (throwable != null) {
            Timber.tag(tag).d(throwable, line)
        } else {
            Timber.tag(tag).d(line)
        }
    }
}
