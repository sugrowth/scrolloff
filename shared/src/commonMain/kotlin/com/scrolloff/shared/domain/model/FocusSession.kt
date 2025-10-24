package com.scrolloff.shared.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class FocusSession(
    val id: String,
    val targetMinutes: Int,
    val completedMinutes: Int,
    val startedAt: Instant,
    val completedAt: Instant?,
    val intentTag: IntentTag?,
    val rewardedSeconds: Long
)

data class DailyStreak(
    val current: Int,
    val longest: Int,
    val lastActiveDate: LocalDate?
)

data class PatternInsight(
    val id: String,
    val title: String,
    val description: String,
    val confidence: PatternConfidence,
    val detectedAt: Instant
)

enum class PatternConfidence {
    LOW,
    MEDIUM,
    HIGH
}
