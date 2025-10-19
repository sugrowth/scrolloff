package com.unscroll.shared.domain.repository

import com.unscroll.shared.domain.model.CreditLedger
import kotlinx.coroutines.flow.Flow

interface CreditRepository {
    fun observeLedger(): Flow<CreditLedger>
    suspend fun setLedger(ledger: CreditLedger)
}
