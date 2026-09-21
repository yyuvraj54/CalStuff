package com.dusht.shared.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Cancel handle returned by [watch] — call [cancel] (e.g. from Swift `deinit`) to stop observing. */
class FlowSubscription(private val job: Job) {
    fun cancel() {
        job.cancel()
    }
}

private val bridgeScope = CoroutineScope(Dispatchers.Main)

/**
 * Callback-based subscribe for consumers that can't use Kotlin [Flow]/[StateFlow] directly —
 * namely Swift, which only sees a raw interop object for those types. Call from Swift as
 * `repo.themeMode.watch { mode in ... }`; delivers on the main thread. Cancel the returned
 * [FlowSubscription] when the observing screen/view model goes away.
 */
fun <T> Flow<T>.watch(onEach: (T) -> Unit): FlowSubscription {
    val job = this.onEach { onEach(it) }.launchIn(bridgeScope)
    return FlowSubscription(job)
}
