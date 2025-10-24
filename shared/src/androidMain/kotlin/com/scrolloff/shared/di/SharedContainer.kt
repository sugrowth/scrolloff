package com.scrolloff.shared.di

import android.content.Context
import com.scrolloff.shared.data.memory.InMemoryContextRuleRepository
import com.scrolloff.shared.data.memory.InMemoryCreditRepository
import com.scrolloff.shared.data.memory.InMemoryFocusSessionRepository
import com.scrolloff.shared.data.memory.InMemoryInsightRepository
import com.scrolloff.shared.data.memory.InMemoryInterceptRepository
import com.scrolloff.shared.data.memory.InMemoryWatchedAppRepository
import com.scrolloff.shared.domain.repository.ContextRuleRepository
import com.scrolloff.shared.domain.repository.CreditRepository
import com.scrolloff.shared.domain.repository.FocusSessionRepository
import com.scrolloff.shared.domain.repository.InsightRepository
import com.scrolloff.shared.domain.repository.InterceptRepository
import com.scrolloff.shared.domain.repository.WatchedAppRepository
import com.scrolloff.shared.domain.usecase.EvaluateInterceptDecisionUseCase
import com.scrolloff.shared.domain.usecase.ObserveDashboardStateUseCase
import com.scrolloff.shared.domain.usecase.RecordInterceptOutcomeUseCase
import com.scrolloff.shared.domain.usecase.RegisterFocusSessionUseCase
import com.scrolloff.shared.domain.usecase.UpdateCreditsUseCase

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
