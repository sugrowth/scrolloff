package com.unscroll.app.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unscroll.app.data.appBlockerPreferences
import com.unscroll.app.service.AppBlockService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.Volatile

data class InstalledApp(
    val packageName: String,
    val label: String
)

data class AppToggle(
    val app: InstalledApp,
    val isBlocked: Boolean,
    val allowUntilMillis: Long?,
    val lockUntilMillis: Long?
)

data class AppUiState(
    val apps: List<AppToggle> = emptyList(),
    val isLoading: Boolean = true,
    val overlayGranted: Boolean = false,
    val accessibilityGranted: Boolean = false,
    val showLanding: Boolean = true
)

sealed interface AppUiEvent {
    data class LockEngaged(val appLabel: String, val unlockAtMillis: Long) : AppUiEvent
    data class ToggleLocked(val appLabel: String, val unlockAtMillis: Long) : AppUiEvent
    data class ShowPaywall(val appLabel: String) : AppUiEvent
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val LOCK_DURATION_MINUTES = 30
        private val LOCK_DURATION_MILLIS = LOCK_DURATION_MINUTES * 60_000L
    }

    private val context = getApplication<Application>()
    private val blockerPreferences = context.appBlockerPreferences()
    private val packageManager: PackageManager = context.packageManager

    private val overlayPermissionState = MutableStateFlow(checkOverlayPermission())
    private val accessibilityPermissionState = MutableStateFlow(isAccessibilityEnabled())
    private val landingState = MutableStateFlow(true)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AppUiEvent>()
    val events: SharedFlow<AppUiEvent> = _events.asSharedFlow()

    @Volatile
    private var currentActivationLocks: Map<String, Long> = emptyMap()

    init {
        viewModelScope.launch {
            val baselineFlow = combine(
                loadInstalledApps(),
                blockerPreferences.blockedPackages,
                blockerPreferences.temporaryAllowances,
                blockerPreferences.activationLocks
            ) { apps, blocked, allowances, activationLocks ->
                val now = System.currentTimeMillis()

                val activeAllowances = allowances.filterValues { it > now }
                val expiredAllowances = allowances.filterValues { it <= now }.keys
                if (expiredAllowances.isNotEmpty()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        expiredAllowances.forEach { blockerPreferences.clearTemporaryAllowance(it) }
                    }
                }

                val activeLocks = activationLocks.filterValues { it > now }
                val expiredLocks = activationLocks.filterValues { it <= now }.keys
                if (expiredLocks.isNotEmpty()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        expiredLocks.forEach { blockerPreferences.clearActivationLock(it) }
                    }
                }
                currentActivationLocks = activeLocks

                apps.map { app ->
                    AppToggle(
                        app = app,
                        isBlocked = blocked.contains(app.packageName),
                        allowUntilMillis = activeAllowances[app.packageName],
                        lockUntilMillis = activeLocks[app.packageName]
                    )
                }.sortedBy { it.app.label.lowercase() }
            }

            combine(
                baselineFlow,
                overlayPermissionState,
                accessibilityPermissionState,
                landingState
            ) { toggles, overlayGranted, accessibilityGranted, showLanding ->
                AppUiState(
                    apps = toggles,
                    isLoading = false,
                    overlayGranted = overlayGranted,
                    accessibilityGranted = accessibilityGranted,
                    showLanding = showLanding
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun loadInstalledApps() = flow {
        val apps = withContext(Dispatchers.IO) {
            val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledApplications(0)
            }
            installedApps
                .filter { info ->
                    info.packageName != context.packageName &&
                        packageManager.getLaunchIntentForPackage(info.packageName) != null
                }
                .map { info ->
                    val label = packageManager.getApplicationLabel(info)?.toString().orEmpty()
                    InstalledApp(
                        packageName = info.packageName,
                        label = label.ifBlank { info.packageName }
                    )
                }
                .distinctBy { it.packageName }
        }
        emit(apps)
    }

    fun onToggleChanged(toggle: AppToggle, shouldBlock: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val packageName = toggle.app.packageName
            val now = System.currentTimeMillis()
            if (shouldBlock) {
                blockerPreferences.setBlocked(packageName, true)
                val lockExpiry = now + LOCK_DURATION_MILLIS
                blockerPreferences.setActivationLock(packageName, lockExpiry)
                _events.emit(AppUiEvent.LockEngaged(toggle.app.label, lockExpiry))
            } else {
                val lockUntil = currentActivationLocks[packageName]
                if (lockUntil != null && lockUntil > now) {
                    _events.emit(AppUiEvent.ToggleLocked(toggle.app.label, lockUntil))
                    return@launch
                }
                blockerPreferences.clearActivationLock(packageName)
                blockerPreferences.setBlocked(packageName, false)
            }
        }
    }

    fun requestEarlyUnlock(toggle: AppToggle) {
        viewModelScope.launch {
            _events.emit(AppUiEvent.ShowPaywall(toggle.app.label))
        }
    }

    fun refreshPermissions() {
        overlayPermissionState.value = checkOverlayPermission()
        accessibilityPermissionState.value = isAccessibilityEnabled()
    }

    fun dismissLanding() {
        landingState.value = false
    }

    fun getAppIcon(packageName: String): Drawable? {
        return runCatching {
            packageManager.getApplicationIcon(packageName)
        }.getOrNull()
    }

    private fun checkOverlayPermission(): Boolean =
        Settings.canDrawOverlays(context)

    private fun isAccessibilityEnabled(): Boolean {
        val expectedComponent = ComponentName(context, AppBlockService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
    }
}
