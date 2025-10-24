package com.scrolloff.shared.domain.repository

import com.scrolloff.shared.domain.model.CreditLedger
import kotlinx.coroutines.flow.Flow

interface CreditRepository {
    fun observeLedger(): Flow<CreditLedger>
    suspend fun setLedger(ledger: CreditLedger)
}
