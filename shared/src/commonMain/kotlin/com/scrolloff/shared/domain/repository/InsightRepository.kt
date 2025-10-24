package com.scrolloff.shared.domain.repository

import com.scrolloff.shared.domain.model.PatternInsight
import kotlinx.coroutines.flow.Flow

interface InsightRepository {
    fun observeInsights(): Flow<List<PatternInsight>>
    suspend fun addInsight(insight: PatternInsight)
    suspend fun clear()
}
