package com.unscroll.shared.domain.usecase

import com.unscroll.shared.domain.model.ContextRule
import com.unscroll.shared.domain.model.InterceptAction
import com.unscroll.shared.domain.model.InterceptContext
import com.unscroll.shared.domain.model.InterceptDecision

class EvaluateInterceptDecisionUseCase {

    operator fun invoke(context: InterceptContext): InterceptDecision {
        val reasoning = mutableListOf<String>()

        context.activeContextRule?.let { rule ->
            reasoning += describeRule(rule)
            return InterceptDecision(
                recommendedAction = InterceptAction.Allow(durationSeconds = AUTO_BYPASS_DURATION_SECONDS),
                reasoning = reasoning,
                autoBypass = true
            )
        }

        if (context.notificationTriggered) {
            reasoning += "Notification triggered unlock; granting peek window."
            return InterceptDecision(
                recommendedAction = InterceptAction.NotificationPeek(durationSeconds = NOTIFICATION_PEEK_SECONDS),
                reasoning = reasoning,
                creditCostSeconds = 0
            )
        }

        if (context.availableCreditsSeconds > 0) {
            val creditChunk = context.availableCreditsSeconds.coerceAtMost(DEFAULT_UNLOCK_SECONDS)
            reasoning += "Credits available: recommending time-boxed unlock."
            if (context.recentSlipCount >= SLIP_THRESHOLD) {
                reasoning += "Recent slips detected; suggesting shorter unlock."
            }
            return InterceptDecision(
                recommendedAction = InterceptAction.Allow(durationSeconds = creditChunk),
                reasoning = reasoning,
                creditCostSeconds = creditChunk
            )
        }

        reasoning += "No credits; recommend quick focus session instead of opening app."
        return InterceptDecision(
            recommendedAction = InterceptAction.PromptFocus(durationMinutes = DEFAULT_FOCUS_MINUTES),
            reasoning = reasoning,
            creditCostSeconds = 0
        )
    }

    private fun describeRule(rule: ContextRule): String = when (rule) {
        is ContextRule.TimeWindow -> "Time rule \"${rule.label}\" matched."
        is ContextRule.Location -> "Location rule \"${rule.label}\" matched."
        is ContextRule.CalendarKeyword -> "Calendar rule \"${rule.label}\" matched."
    }

    companion object {
        private const val AUTO_BYPASS_DURATION_SECONDS = 15 * 60L
        private const val NOTIFICATION_PEEK_SECONDS = 2 * 60L
        private const val DEFAULT_UNLOCK_SECONDS = 10 * 60L
        private const val DEFAULT_FOCUS_MINUTES = 5
        private const val SLIP_THRESHOLD = 3
    }
}
