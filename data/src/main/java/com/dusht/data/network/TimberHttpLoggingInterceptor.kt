package com.dusht.data.network

import com.dusht.core.logging.AppLogger
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Bridges OkHttp body logging to Timber (debug). Redundant with [com.dusht.data.di.DataProvidesModule] timing logs but useful for payloads.
 */
class TimberHttpLoggingInterceptor : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        AppLogger.api(message = message, extras = mapOf("channel" to "okhttp-body"))
    }
}
