package com.scrolloff.shared.domain.usecase

import com.scrolloff.shared.domain.model.CreditLedger
import com.scrolloff.shared.domain.model.CreditTransaction
import com.scrolloff.shared.domain.model.CreditTransactionType
import com.scrolloff.shared.domain.repository.CreditRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

class UpdateCreditsUseCase(
    private val creditRepository: CreditRepository,
    private val clock: Clock = Clock.System
) {

    suspend fun earn(seconds: Long, metadata: Map<String, String> = emptyMap()) {
        require(seconds > 0) { "Seconds must be positive" }
        val ledger = creditRepository.observeLedger().firstValue()
        val updated = ledger.addTransaction(
            CreditTransaction(
                type = CreditTransactionType.EARN,
                seconds = seconds,
                timestamp = clock.now(),
                metadata = metadata
            )
        )
        creditRepository.setLedger(updated)
    }

    suspend fun spend(seconds: Long, metadata: Map<String, String> = emptyMap()): Boolean {
        require(seconds > 0) { "Seconds must be positive" }
        val ledger = creditRepository.observeLedger().firstValue()
        if (!ledger.canSpend(seconds)) return false
        val updated = ledger.addTransaction(
            CreditTransaction(
                type = CreditTransactionType.SPEND,
                seconds = seconds,
                timestamp = clock.now(),
                metadata = metadata
            )
        )
        creditRepository.setLedger(updated)
        return true
    }

    suspend fun decay(expiryHours: Long = DEFAULT_EXPIRY_HOURS) {
        val ledger = creditRepository.observeLedger().firstValue()
        val decayed = ledger.applyDecay(expiryHours = expiryHours, clock = clock)
        creditRepository.setLedger(decayed)
    }

    private suspend fun <T> Flow<T>.firstValue(): T = first()

    companion object {
        private const val DEFAULT_EXPIRY_HOURS = 48L
    }
}

private fun CreditLedger.addTransaction(transaction: CreditTransaction): CreditLedger {
    val updatedTotal = when (transaction.type) {
        CreditTransactionType.EARN -> (clampedTotal + transaction.seconds).coerceAtMost(CreditLedger.MAX_CREDITS_SECONDS)
        CreditTransactionType.SPEND -> (clampedTotal - transaction.seconds).coerceAtLeast(0)
        CreditTransactionType.DECAY -> (clampedTotal - transaction.seconds).coerceAtLeast(0)
    }
    return copy(
        totalCreditsSeconds = updatedTotal,
        transactions = transactions + transaction
    )
}

private fun CreditLedger.applyDecay(expiryHours: Long, clock: Clock): CreditLedger {
    val cutoff = clock.now() - expiryHours.hours
    val decaySeconds = transactions
        .filter { it.type == CreditTransactionType.EARN && it.timestamp < cutoff }
        .sumOf { (it.seconds / 2).coerceAtLeast(1) }

    if (decaySeconds <= 0) return this

    return addTransaction(
        CreditTransaction(
            type = CreditTransactionType.DECAY,
            seconds = decaySeconds,
            timestamp = clock.now(),
            metadata = mapOf("reason" to "decay")
        )
    )
}
