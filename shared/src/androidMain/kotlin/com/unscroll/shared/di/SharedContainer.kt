package com.unscroll.shared.di

import android.content.Context
import com.unscroll.shared.data.memory.InMemoryContextRuleRepository
import com.unscroll.shared.data.memory.InMemoryCreditRepository
import com.unscroll.shared.data.memory.InMemoryFocusSessionRepository
import com.unscroll.shared.data.memory.InMemoryInsightRepository
import com.unscroll.shared.data.memory.InMemoryInterceptRepository
import com.unscroll.shared.data.memory.InMemoryWatchedAppRepository
import com.unscroll.shared.domain.repository.ContextRuleRepository
import com.unscroll.shared.domain.repository.CreditRepository
import com.unscroll.shared.domain.repository.FocusSessionRepository
import com.unscroll.shared.domain.repository.InsightRepository
import com.unscroll.shared.domain.repository.InterceptRepository
import com.unscroll.shared.domain.repository.WatchedAppRepository
import com.unscroll.shared.domain.usecase.EvaluateInterceptDecisionUseCase
import com.unscroll.shared.domain.usecase.ObserveDashboardStateUseCase
import com.unscroll.shared.domain.usecase.RecordInterceptOutcomeUseCase
import com.unscroll.shared.domain.usecase.RegisterFocusSessionUseCase
import com.unscroll.shared.domain.usecase.UpdateCreditsUseCase

class SharedContainer(
    private val appContext: Context
) {

    val watchedAppRepository: WatchedAppRepository by lazy { InMemoryWatchedAppRepository() }
    val creditRepository: CreditRepository by lazy { InMemoryCreditRepository() }
    val interceptRepository: InterceptRepository by lazy { InMemoryInterceptRepository() }
    val focusSessionRepository: FocusSessionRepository by lazy { InMemoryFocusSessionRepository() }
    val contextRuleRepository: ContextRuleRepository by lazy { InMemoryContextRuleRepository() }
    val insightRepository: InsightRepository by lazy { InMemoryInsightRepository() }

    val updateCreditsUseCase: UpdateCreditsUseCase by lazy {
        UpdateCreditsUseCase(
            creditRepository = creditRepository
        )
    }

    val registerFocusSessionUseCase: RegisterFocusSessionUseCase by lazy {
        RegisterFocusSessionUseCase(
            focusSessionRepository = focusSessionRepository,
            updateCreditsUseCase = updateCreditsUseCase
        )
    }

    val evaluateInterceptDecisionUseCase: EvaluateInterceptDecisionUseCase by lazy {
        EvaluateInterceptDecisionUseCase()
    }

    val recordInterceptOutcomeUseCase: RecordInterceptOutcomeUseCase by lazy {
        RecordInterceptOutcomeUseCase(
            interceptRepository = interceptRepository,
            updateCreditsUseCase = updateCreditsUseCase
        )
    }

    val observeDashboardStateUseCase: ObserveDashboardStateUseCase by lazy {
        ObserveDashboardStateUseCase(
            watchedAppRepository = watchedAppRepository,
            creditRepository = creditRepository,
            focusSessionRepository = focusSessionRepository,
            interceptRepository = interceptRepository,
            insightRepository = insightRepository
        )
    }
}
