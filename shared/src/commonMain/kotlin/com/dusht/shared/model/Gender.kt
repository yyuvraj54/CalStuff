package com.dusht.shared.model

/**
 * Canonical gender values. [wireValue] is what's stored on [UserProfile.gender] — keep it
 * stable, both platforms parse against it.
 */
enum class Gender(val wireValue: String) {
    MALE("MALE"),
    FEMALE("FEMALE"),
    OTHER("OTHER"),
    PREFER_NOT_SAY("PREFER_NOT_SAY"),
}
