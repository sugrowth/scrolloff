package com.scrolloff.shared.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

sealed interface ContextRule {
    val id: String
    val label: String

    data class TimeWindow(
        override val id: String,
        override val label: String,
        val daysOfWeek: Set<DayOfWeek>,
        val startTime: LocalTime,
        val endTime: LocalTime
    ) : ContextRule

    data class Location(
        override val id: String,
        override val label: String,
        val latitude: Double,
        val longitude: Double,
        val radiusMeters: Float
    ) : ContextRule

    data class CalendarKeyword(
        override val id: String,
        override val label: String,
        val keyword: String
    ) : ContextRule
}
