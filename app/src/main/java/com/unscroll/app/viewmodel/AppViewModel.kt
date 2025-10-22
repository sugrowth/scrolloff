package com.unscroll.app.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unscroll.app.data.appBlockerPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String
)

data class AppToggle(
    val app: InstalledApp,
    val isBlocked: Boolean
)

data class AppUiState(
    val apps: List<AppToggle> = emptyList(),
    val isLoading: Boolean = true
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    private val blockerPreferences = context.appBlockerPreferences()
    private val packageManager: PackageManager = context.packageManager

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                loadInstalledApps(),
                blockerPreferences.blockedPackages
            ) { apps, blocked ->
                val toggles = apps.map { app ->
                    AppToggle(app = app, isBlocked = blocked.contains(app.packageName))
                }
                AppUiState(
                    apps = toggles.sortedBy { it.app.label.lowercase() },
                    isLoading = false
                )
            }.collect { uiState ->
                _uiState.value = uiState
            }
        }
    }

    private fun loadInstalledApps() = flow {
        val apps = withContext(Dispatchers.IO) {
            val launchIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
            resolveInfos
                .map { info ->
                    val label = info.loadLabel(packageManager)?.toString() ?: info.activityInfo.packageName
                    InstalledApp(
                        packageName = info.activityInfo.packageName,
                        label = label
                    )
                }
                .distinctBy { it.packageName }
        }
        emit(apps)
    }

    fun setBlocked(packageName: String, blocked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            blockerPreferences.setBlocked(packageName, blocked)
        }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return runCatching {
            packageManager.getApplicationIcon(packageName)
        }.getOrNull()
    }
}
