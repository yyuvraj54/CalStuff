package com.dusht.core.logging

import timber.log.Timber

object TimberInitializer {

    fun init(isDebug: Boolean) {
        if (isDebug) {
            Timber.plant(DebugTreeWithTag())
        } else {
            Timber.plant(ReleaseTree())
        }
    }

    private class DebugTreeWithTag : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
            return "${super.createStackElementTag(element)}:${element.lineNumber}"
        }
    }

    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Forward only warnings/errors in release; extend with Crashlytics if needed.
            if (priority >= android.util.Log.WARN) {
                android.util.Log.println(priority, tag, message)
                t?.let { android.util.Log.println(priority, tag, android.util.Log.getStackTraceString(it)) }
            }
        }
    }
}
