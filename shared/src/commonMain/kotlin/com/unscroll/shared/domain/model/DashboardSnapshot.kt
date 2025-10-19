package com.unscroll.shared.domain.model

import kotlinx.datetime.Instant

data class DashboardSnapshot(
    val watchedApps: List<WatchedApp>,
    val creditLedger: CreditLedger,
    val streak: DailyStreak,
    val focusSessions: List<FocusSession>,
    val intercepts: List<InterceptEvent>,
    val insights: List<PatternInsight>,
    val generatedAt: Instant
) {
    val hasCredits: Boolean = creditLedger.clampedTotal > 0
    val todaysFocusMinutes: Int = focusSessions
        .filter { it.completedAt != null }
        .sumOf { it.completedMinutes }
}
