package com.unscroll.shared.domain.usecase

import com.unscroll.shared.domain.model.DashboardSnapshot
import com.unscroll.shared.domain.repository.CreditRepository
import com.unscroll.shared.domain.repository.FocusSessionRepository
import com.unscroll.shared.domain.repository.InsightRepository
import com.unscroll.shared.domain.repository.InterceptRepository
import com.unscroll.shared.domain.repository.WatchedAppRepository
import com.unscroll.shared.domain.model.DailyStreak
import com.unscroll.shared.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.minus

class ObserveDashboardStateUseCase(
    private val watchedAppRepository: WatchedAppRepository,
    private val creditRepository: CreditRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val interceptRepository: InterceptRepository,
    private val insightRepository: InsightRepository,
    private val clock: Clock = Clock.System
) {
    operator fun invoke(): Flow<DashboardSnapshot> = combine(
        watchedAppRepository.observeWatchedApps(),
        creditRepository.observeLedger(),
        focusSessionRepository.observeSessions(),
        interceptRepository.observeRecent(limit = 20),
        insightRepository.observeInsights()
    ) { watchedApps, ledger, sessions, intercepts, insights ->
        DashboardSnapshot(
            watchedApps = watchedApps,
            creditLedger = ledger,
            streak = calculateStreak(sessions),
            focusSessions = sessions,
            intercepts = intercepts,
            insights = insights,
            generatedAt = clock.now()
        )
    }
}

private fun calculateStreak(sessions: List<FocusSession>): DailyStreak {
    if (sessions.isEmpty()) return DailyStreak(current = 0, longest = 0, lastActiveDate = null)
    val completedDates = sessions
        .mapNotNull { it.completedAt }
        .map { it.toLocalDateTime(TimeZone.UTC).date }
        .distinct()
        .sortedDescending()

    var currentStreak = 0
    var longestStreak = 0
    var expectedDate = completedDates.firstOrNull()
    completedDates.forEach { date ->
        if (expectedDate == null) {
            expectedDate = date
            currentStreak = 1
            longestStreak = 1
        } else if (date == expectedDate) {
            currentStreak += 1
        } else if (date == expectedDate!!.minus(DatePeriod(days = 1))) {
            currentStreak += 1
            expectedDate = date
        } else {
            longestStreak = maxOf(longestStreak, currentStreak)
            currentStreak = 1
            expectedDate = date
        }
    }
    longestStreak = maxOf(longestStreak, currentStreak)
    return DailyStreak(
        current = currentStreak,
        longest = longestStreak,
        lastActiveDate = completedDates.firstOrNull()
    )
}
