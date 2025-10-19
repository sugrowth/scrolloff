package com.unscroll.shared.domain.repository

import com.unscroll.shared.domain.model.InterceptEvent
import kotlinx.coroutines.flow.Flow

interface InterceptRepository {
    fun observeRecent(limit: Int = 25): Flow<List<InterceptEvent>>
    suspend fun record(event: InterceptEvent)
    suspend fun clear()
}
