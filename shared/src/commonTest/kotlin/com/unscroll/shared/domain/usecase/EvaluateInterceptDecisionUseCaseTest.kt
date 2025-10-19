package com.unscroll.shared.domain.usecase

import com.unscroll.shared.domain.model.AppCategory
import com.unscroll.shared.domain.model.InterceptContext
import com.unscroll.shared.domain.model.WatchedApp
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluateInterceptDecisionUseCaseTest {

    private val useCase = EvaluateInterceptDecisionUseCase()
    private val app = WatchedApp(
        packageName = "com.example.app",
        displayName = "Example",
        category = AppCategory.SOCIAL,
        createdAt = Clock.System.now()
    )

    @Test
    fun `notification triggered intercept returns peek action`() {
        val context = InterceptContext(
            app = app,
            timestamp = Clock.System.now(),
            availableCreditsSeconds = 0,
            streakDays = 2,
            activeContextRule = null,
            notificationTriggered = true,
            recentSlipCount = 0
        )

        val decision = useCase(context)
        assertTrue(decision.recommendedAction is com.unscroll.shared.domain.model.InterceptAction.NotificationPeek)
    }

    @Test
    fun `credits available recommends allow`() {
        val context = InterceptContext(
            app = app,
            timestamp = Clock.System.now(),
            availableCreditsSeconds = 600,
            streakDays = 2,
            activeContextRule = null,
            notificationTriggered = false,
            recentSlipCount = 0
        )

        val decision = useCase(context)
        assertTrue(decision.recommendedAction is com.unscroll.shared.domain.model.InterceptAction.Allow)
        assertEquals(600, decision.creditCostSeconds)
    }

    @Test
    fun `no credits recommends focus`() {
        val context = InterceptContext(
            app = app,
            timestamp = Clock.System.now(),
            availableCreditsSeconds = 0,
            streakDays = 2,
            activeContextRule = null,
            notificationTriggered = false,
            recentSlipCount = 0
        )

        val decision = useCase(context)
        assertTrue(decision.recommendedAction is com.unscroll.shared.domain.model.InterceptAction.PromptFocus)
    }
}
