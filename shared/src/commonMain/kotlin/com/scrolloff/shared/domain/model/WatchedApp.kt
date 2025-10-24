package com.scrolloff.shared.domain.model

import kotlinx.datetime.Instant

/**
 * Represents a user-selected distraction app.
 */
data class WatchedApp(
    val packageName: String,
    val displayName: String,
    val category: AppCategory,
    val isNotificationSafe: Boolean = false,
    val sensitivity: InterceptSensitivity = InterceptSensitivity.DEFAULT,
    val createdAt: Instant = Instant.DISTANT_PAST
)

enum class AppCategory {
    SOCIAL,
    VIDEO,
    NEWS,
    SHOPPING,
    GAMES,
    OTHER
}

enum class InterceptSensitivity {
    RELAXED,
    DEFAULT,
    STRICT
}
