package com.unscroll.shared.domain.repository

import com.unscroll.shared.domain.model.ContextRule
import kotlinx.coroutines.flow.Flow

interface ContextRuleRepository {
    fun observeRules(): Flow<List<ContextRule>>
    suspend fun upsert(rule: ContextRule)
    suspend fun delete(id: String)
}
