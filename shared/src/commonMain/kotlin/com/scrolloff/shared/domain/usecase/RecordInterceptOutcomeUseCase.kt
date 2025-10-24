package com.scrolloff.shared.domain.usecase

import com.scrolloff.shared.domain.model.InterceptDecision
import com.scrolloff.shared.domain.model.InterceptEvent
import com.scrolloff.shared.domain.model.InterceptOutcome
import com.scrolloff.shared.domain.repository.CreditRepository
import com.scrolloff.shared.domain.repository.InterceptRepository
import kotlinx.datetime.Clock

class RecordInterceptOutcomeUseCase(
    private val interceptRepository: InterceptRepository,
    private val updateCreditsUseCase: UpdateCreditsUseCase,
    private val clock: Clock = Clock.System
) {
    suspend operator fun invoke(
        context: com.scrolloff.shared.domain.model.InterceptContext,
        decision: InterceptDecision,
        outcome: InterceptOutcome
    ) {
        val timestamp = clock.now()
        val event = InterceptEvent(
            context = context,
            decision = decision,
            outcome = outcome,
            recordedAt = timestamp
        )
        interceptRepository.record(event)

        if (outcome == InterceptOutcome.USE_CREDITS && decision.creditCostSeconds > 0) {
            updateCreditsUseCase.spend(
                seconds = decision.creditCostSeconds,
                metadata = mapOf(
                    "packageName" to context.app.packageName,
                    "reason" to "unlock"
                )
            )
        }
    }
}
