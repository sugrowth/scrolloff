package com.scrolloff.shared.domain.repository

import com.scrolloff.shared.domain.model.WatchedApp
import kotlinx.coroutines.flow.Flow

interface WatchedAppRepository {
    fun observeWatchedApps(): Flow<List<WatchedApp>>
    suspend fun addApp(app: WatchedApp)
    suspend fun removeApp(packageName: String)
    suspend fun updateNotificationSafe(packageName: String, isSafe: Boolean)
    suspend fun updateSensitivity(packageName: String, sensitivity: com.scrolloff.shared.domain.model.InterceptSensitivity)
}
