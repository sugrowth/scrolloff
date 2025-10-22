package com.unscroll.app.data

import android.content.Context
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

    val activationLocks: Flow<Map<String, Long>> = context.blockerDataStore
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

    suspend fun setActivationLock(packageName: String, expiryMillis: Long) {
        context.blockerDataStore.edit { preferences ->
            val entries = preferences[ACTIVATION_LOCKS_KEY]?.toMutableSet() ?: mutableSetOf()
            entries.removeAll { it.startsWith("$packageName|") }
            entries.add("$packageName|$expiryMillis")
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
}

fun Context.appBlockerPreferences(): AppBlockerPreferences = AppBlockerPreferences(this)
