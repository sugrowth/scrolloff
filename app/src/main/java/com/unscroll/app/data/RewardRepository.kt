package com.unscroll.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.rewardDataStore by preferencesDataStore("reward_store")

private val LAST_ACTIVITY_KEY = longPreferencesKey("last_allowed_activity")
private val ACCUMULATED_FOCUS_KEY = longPreferencesKey("accumulated_focus")
private val AVAILABLE_REWARD_KEY = longPreferencesKey("available_reward_seconds")

class RewardRepository(private val context: Context) {

    val rewardState: Flow<RewardState> = context.rewardDataStore.data.map { prefs ->
        RewardState(
            lastActivityEpochMillis = prefs[LAST_ACTIVITY_KEY] ?: 0L,
            accumulatedFocusMillis = prefs[ACCUMULATED_FOCUS_KEY] ?: 0L,
            availableRewardSeconds = prefs[AVAILABLE_REWARD_KEY] ?: 0L
        )
    }

    suspend fun recordActivity(now: Long) {
        context.rewardDataStore.edit { prefs ->
            val previous = prefs[LAST_ACTIVITY_KEY] ?: now
            val focusMillis = prefs[ACCUMULATED_FOCUS_KEY] ?: 0L
            val delta = (now - previous).coerceAtLeast(0L)
            val updatedFocus = focusMillis + delta
            var rewardSeconds = prefs[AVAILABLE_REWARD_KEY] ?: 0L
            if (updatedFocus >= FOCUS_GOAL_MILLIS) {
                val earnedRewards = updatedFocus / FOCUS_GOAL_MILLIS
                rewardSeconds += earnedRewards * REWARD_SECONDS
            }
            val remainingFocus = updatedFocus % FOCUS_GOAL_MILLIS
            prefs[LAST_ACTIVITY_KEY] = now
            prefs[ACCUMULATED_FOCUS_KEY] = remainingFocus
            prefs[AVAILABLE_REWARD_KEY] = rewardSeconds
        }
    }

    suspend fun consumeReward(seconds: Long): Boolean {
        var success = false
        context.rewardDataStore.edit { prefs ->
            val available = prefs[AVAILABLE_REWARD_KEY] ?: 0L
            if (available >= seconds) {
                prefs[AVAILABLE_REWARD_KEY] = available - seconds
                success = true
            }
        }
        return success
    }

    suspend fun markBlocked(now: Long) {
        context.rewardDataStore.edit { prefs ->
            prefs[LAST_ACTIVITY_KEY] = now
            prefs[ACCUMULATED_FOCUS_KEY] = 0L
        }
    }

    suspend fun resetRewards() {
        context.rewardDataStore.edit { prefs ->
            prefs[AVAILABLE_REWARD_KEY] = 0L
            prefs[ACCUMULATED_FOCUS_KEY] = 0L
            prefs[LAST_ACTIVITY_KEY] = System.currentTimeMillis()
        }
    }

    companion object {
        private const val FOCUS_GOAL_MILLIS = 60 * 60 * 1000L
        private const val REWARD_SECONDS = 5 * 60L
    }
}

data class RewardState(
    val lastActivityEpochMillis: Long,
    val accumulatedFocusMillis: Long,
    val availableRewardSeconds: Long
)

fun RewardState.formattedRewardMinutes(): Int = (availableRewardSeconds / 60).toInt()
