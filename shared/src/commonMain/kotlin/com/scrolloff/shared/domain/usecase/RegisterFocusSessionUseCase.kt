package com.scrolloff.shared.domain.usecase

import com.scrolloff.shared.domain.model.FocusSession
import com.scrolloff.shared.domain.model.IntentTag
import com.scrolloff.shared.domain.repository.FocusSessionRepository
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

class RegisterFocusSessionUseCase(
    private val focusSessionRepository: FocusSessionRepository,
    private val updateCreditsUseCase: UpdateCreditsUseCase,
    private val clock: Clock = Clock.System
) {
    suspend operator fun invoke(
        targetMinutes: Int,
        completedMinutes: Int,
        intentTag: IntentTag?
    ): FocusSession {
        require(targetMinutes > 0)
        require(completedMinutes >= 0)
        val now = clock.now()
        val rewardedSeconds = completedMinutes.coerceAtMost(targetMinutes) * 60L
        val session = FocusSession(
            id = generateId(),
            targetMinutes = targetMinutes,
            completedMinutes = completedMinutes,
            startedAt = now - completedMinutes.minutes,
            completedAt = now,
            intentTag = intentTag,
            rewardedSeconds = rewardedSeconds
        )
        focusSessionRepository.upsert(session)
        if (rewardedSeconds > 0) {
            updateCreditsUseCase.earn(
                seconds = rewardedSeconds,
                metadata = mapOf("source" to "focus_session", "sessionId" to session.id)
            )
        }
        return session
    }

    private fun generateId(): String = buildString {
        append("fs-")
        repeat(8) {
            append(ALPHABET[Random.nextInt(ALPHABET.size)])
        }
    }

    private companion object {
        private val ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray()
    }
}
