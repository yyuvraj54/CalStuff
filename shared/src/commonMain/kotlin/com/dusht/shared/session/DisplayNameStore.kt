package com.dusht.shared.session

/** Cached profile display name (encrypted storage on Android); cleared on logout. */
interface DisplayNameStore {
    fun get(): String?
    fun set(name: String)
    fun clear()
}
