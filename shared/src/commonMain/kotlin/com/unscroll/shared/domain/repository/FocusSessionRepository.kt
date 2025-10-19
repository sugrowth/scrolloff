package com.unscroll.shared.domain.repository

import com.unscroll.shared.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusSessionRepository {
    fun observeSessions(): Flow<List<FocusSession>>
    suspend fun upsert(session: FocusSession)
    suspend fun delete(id: String)
}
