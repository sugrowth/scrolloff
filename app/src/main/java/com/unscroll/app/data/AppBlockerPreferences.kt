package com.unscroll.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
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
}

fun Context.appBlockerPreferences(): AppBlockerPreferences = AppBlockerPreferences(this)
