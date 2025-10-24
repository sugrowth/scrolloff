package com.scrolloff.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.blockerDataStore by preferencesDataStore(name = "app_blocker")

private val BLOCKED_PACKAGES_KEY = stringSetPreferencesKey("blocked_packages")
private val TEMP_ALLOWANCES_KEY = stringSetPreferencesKey("temporary_allowances")
private val ACTIVATION_LOCKS_KEY = stringSetPreferencesKey("activation_locks")
private const val LEGACY_GRACE_WINDOW_MILLIS = 5 * 60_000L
private val LANDING_COMPLETED_KEY = booleanPreferencesKey("landing_completed")
private val LAST_DISABLED_KEY = stringSetPreferencesKey("last_disabled_timestamps")

data class ActivationLockEntry(
    val lockUntilMillis: Long,
    val graceUntilMillis: Long
)

class AppBlockerPreferences(private val context: Context) {

    val blockedPackages: Flow<Set<String>> = context.blockerDataStore
        .data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[BLOCKED_PACKAGES_KEY] ?: emptySet()
        }

    val temporaryAllowances: Flow<Map<String, Long>> = context.blockerDataStore
        .data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[TEMP_ALLOWANCES_KEY]
                ?.mapNotNull { entry ->
                    val parts = entry.split("|")
                    if (parts.size == 2) {
                        val expiry = parts[1].toLongOrNull()
                        if (expiry != null) {
                            parts[0] to expiry
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
                ?.toMap()
                ?: emptyMap()
        }

    val activationLocks: Flow<Map<String, ActivationLockEntry>> = context.blockerDataStore
        .data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ACTIVATION_LOCKS_KEY]
                ?.mapNotNull { entry ->
                    val parts = entry.split("|")
                    when (parts.size) {
                        2 -> {
                            val expiry = parts[1].toLongOrNull() ?: return@mapNotNull null
                            parts[0] to ActivationLockEntry(
                                lockUntilMillis = expiry,
                                graceUntilMillis = expiry - LEGACY_GRACE_WINDOW_MILLIS
                            )
                        }
                        3 -> {
                            val lockUntil = parts[1].toLongOrNull() ?: return@mapNotNull null
                            val graceUntil = parts[2].toLongOrNull() ?: return@mapNotNull null
                            parts[0] to ActivationLockEntry(
                                lockUntilMillis = lockUntil,
                                graceUntilMillis = graceUntil
                            )
                        }
                        else -> null
                    }
                }
                ?.toMap()
                ?: emptyMap()
        }

    val lastDisabled: Flow<Map<String, Long>> = context.blockerDataStore
        .data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LAST_DISABLED_KEY]
                ?.mapNotNull { entry ->
                    val parts = entry.split("|")
                    if (parts.size == 2) {
                        val time = parts[1].toLongOrNull()
                        if (time != null) parts[0] to time else null
                    } else {
                        null
                    }
                }
                ?.toMap()
                ?: emptyMap()
        }

    val landingCompleted: Flow<Boolean> = context.blockerDataStore
        .data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LANDING_COMPLETED_KEY] ?: false
        }

    suspend fun setBlocked(packageName: String, blocked: Boolean) {
        context.blockerDataStore.edit { preferences ->
            val current = preferences[BLOCKED_PACKAGES_KEY]?.toMutableSet() ?: mutableSetOf()
            if (blocked) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            preferences[BLOCKED_PACKAGES_KEY] = current
        }
    }

    suspend fun grantTemporaryAllowance(packageName: String, durationMinutes: Int) {
        val expiryMillis = System.currentTimeMillis() + durationMinutes * 60_000L
        context.blockerDataStore.edit { preferences ->
            val entries = preferences[TEMP_ALLOWANCES_KEY]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { it.startsWith("$packageName|") }
            entries.add("$packageName|$expiryMillis")
            preferences[TEMP_ALLOWANCES_KEY] = entries
        }
    }

    suspend fun clearTemporaryAllowance(packageName: String) {
        context.blockerDataStore.edit { preferences ->
            val entries = preferences[TEMP_ALLOWANCES_KEY]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { it.startsWith("$packageName|") }
            preferences[TEMP_ALLOWANCES_KEY] = entries
        }
    }

    suspend fun setActivationLock(packageName: String, lockUntilMillis: Long, graceUntilMillis: Long) {
        context.blockerDataStore.edit { preferences ->
            val entries = preferences[ACTIVATION_LOCKS_KEY]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { it.startsWith("$packageName|") }
            entries.add("$packageName|$lockUntilMillis|$graceUntilMillis")
            preferences[ACTIVATION_LOCKS_KEY] = entries
        }
    }

    suspend fun clearActivationLock(packageName: String) {
        context.blockerDataStore.edit { preferences ->
            val entries = preferences[ACTIVATION_LOCKS_KEY]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { it.startsWith("$packageName|") }
            preferences[ACTIVATION_LOCKS_KEY] = entries
        }
    }

    suspend fun setLastDisabled(packageName: String, timestamp: Long) {
        context.blockerDataStore.edit { preferences ->
            val entries = preferences[LAST_DISABLED_KEY]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { it.startsWith("$packageName|") }
            entries.add("$packageName|$timestamp")
            preferences[LAST_DISABLED_KEY] = entries
        }
    }

    suspend fun markLandingCompleted() {
        context.blockerDataStore.edit { preferences ->
            preferences[LANDING_COMPLETED_KEY] = true
        }
    }
}

fun Context.appBlockerPreferences(): AppBlockerPreferences = AppBlockerPreferences(this)
