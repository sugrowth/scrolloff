package com.unscroll.shared.domain.model

import kotlinx.datetime.Instant

enum class IntentTag {
    BORED,
    CHECKING_NOTIFICATION,
    LOOKING_FOR_SOMETHING,
    HABIT,
    OTHER
}

enum class InterceptOutcome {
    SKIP,
    START_FOCUS,
    USE_CREDITS,
    EMERGENCY_BYPASS,
    NOTIFICATION_PEEK,
    DISMISSED
}

data class InterceptContext(
    val app: WatchedApp,
    val timestamp: Instant,
    val availableCreditsSeconds: Long,
    val streakDays: Int,
    val activeContextRule: ContextRule?,
    val notificationTriggered: Boolean,
    val recentSlipCount: Int,
    val intentTag: IntentTag? = null
)

data class InterceptDecision(
    val recommendedAction: InterceptAction,
    val reasoning: List<String>,
    val creditCostSeconds: Long = 0,
    val autoBypass: Boolean = false
)

sealed interface InterceptAction {
    data object Block : InterceptAction
    data class Allow(val durationSeconds: Long) : InterceptAction
    data class PromptFocus(val durationMinutes: Int) : InterceptAction
    data class NotificationPeek(val durationSeconds: Long) : InterceptAction
}

data class InterceptEvent(
    val context: InterceptContext,
    val decision: InterceptDecision,
    val outcome: InterceptOutcome,
    val recordedAt: Instant
) {
    val isSlip: Boolean = outcome == InterceptOutcome.USE_CREDITS || outcome == InterceptOutcome.EMERGENCY_BYPASS
}
