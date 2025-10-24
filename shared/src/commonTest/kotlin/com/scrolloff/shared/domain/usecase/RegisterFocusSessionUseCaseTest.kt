package com.scrolloff.shared.domain.usecase

import com.scrolloff.shared.data.memory.InMemoryCreditRepository
import com.scrolloff.shared.data.memory.InMemoryFocusSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterFocusSessionUseCaseTest {

    private val focusRepository = InMemoryFocusSessionRepository()
    private val creditRepository = InMemoryCreditRepository()
    private val updateCredits = UpdateCreditsUseCase(creditRepository)
    private val useCase = RegisterFocusSessionUseCase(focusRepository, updateCredits)

    @Test
    fun `focus session rewards credits`() = runTest {
        val initial = creditRepository.observeLedger().first()
        assertEquals(0, initial.clampedTotal)

        val session = useCase(targetMinutes = 10, completedMinutes = 10, intentTag = null)

        val updated = creditRepository.observeLedger().first()
        assertTrue(updated.clampedTotal > initial.clampedTotal)
        assertEquals(600, updated.clampedTotal)
        assertEquals(600, session.rewardedSeconds)

        val storedSessions = focusRepository.observeSessions().first()
        assertEquals(1, storedSessions.size)
    }
}
