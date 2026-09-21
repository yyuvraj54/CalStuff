package com.dusht.shared.profile

import com.dusht.shared.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileGateRepositoryTest {

    private fun profile(name: String) = UserProfile(
        uid = "u1", name = name, age = 30, heightCm = 180f, weightKg = 75f,
        gender = "MALE", activityLevel = "MODERATE", dailyCalorieGoal = 2000,
        createdAt = 0L, updatedAt = 0L,
    )

    @Test
    fun nullProfile_isIncomplete() {
        assertFalse(isProfileComplete(null))
    }

    @Test
    fun blankName_isIncomplete() {
        assertFalse(isProfileComplete(profile("")))
        assertFalse(isProfileComplete(profile("   ")))
    }

    @Test
    fun nonBlankName_isComplete() {
        assertTrue(isProfileComplete(profile("Ada Lovelace")))
    }
}
