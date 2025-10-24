package com.scrolloff.shared.domain.model

import kotlinx.datetime.Instant

/**
 * Tracks the user's earned and spent credits.
 */
data class CreditLedger(
    val totalCreditsSeconds: Long,
    val transactions: List<CreditTransaction>
) {
    val clampedTotal: Long
        get() = totalCreditsSeconds.coerceIn(0, MAX_CREDITS_SECONDS)

    fun canSpend(seconds: Long): Boolean = clampedTotal >= seconds

    companion object {
        const val MAX_CREDITS_SECONDS: Long = 60 * 180 // 180 minutes

        val EMPTY = CreditLedger(
            totalCreditsSeconds = 0,
            transactions = emptyList()
        )
    }
}

data class CreditTransaction(
    val type: CreditTransactionType,
    val seconds: Long,
    val timestamp: Instant,
    val metadata: Map<String, String>
)

enum class CreditTransactionType {
    EARN,
    SPEND,
    DECAY
}
