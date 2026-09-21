package com.dusht.shared.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateKeyTest {

    @Test
    fun of_padsSingleDigitMonthAndDay() {
        assertEquals("2026-01-05", DateKey.of(2026, 1, 5))
    }

    @Test
    fun of_doesNotPadFourDigitYear() {
        assertEquals("2026-12-31", DateKey.of(2026, 12, 31))
    }

    @Test
    fun parse_roundTripsWithOf() {
        val key = DateKey.of(2026, 7, 9)
        assertEquals(Triple(2026, 7, 9), DateKey.parse(key))
    }

    @Test
    fun parse_malformedInput_returnsNull() {
        assertNull(DateKey.parse("not-a-date"))
        assertNull(DateKey.parse("2026-07"))
        assertNull(DateKey.parse(""))
    }
}
