package com.unscroll.app.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unscroll.app.data.ActivationLockEntry
import com.unscroll.app.data.RewardRepository
import com.unscroll.app.data.appBlockerPreferences
import com.unscroll.app.data.formattedRewardMinutes
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

private const val FREE_PLAN_MAX_BLOCKED_APPS = 5
private const val BASE_LOCK_MINUTES = 240
private const val GRACE_WINDOW_MINUTES = 5
private val GRACE_WINDOW_MILLIS = GRACE_WINDOW_MINUTES * 60_000L

enum class AppCategory(
    val displayName: String,
    val lockBonusMinutes: Int,
    private val keywords: List<String>
) {
    SOCIAL("Social", 0, listOf("instagram", "facebook", "whatsapp", "snap", "reddit", "twitter", "x", "telegram")),
    ENTERTAINMENT("Entertainment", 30, listOf("youtube", "netflix", "prime", "spotify", "music", "tiktok", "disney", "hulu")),
    GAMING("Gaming", 60, listOf("game", "clash", "royale", "pubg", "genshin", "freefire", "callofduty", "roblox")),
    SHOPPING("Shopping", 30, listOf("amazon", "flipkart", "shop", "myntra", "nykaa", "walmart")),
    NEWS("News", 15, listOf("news", "cnn", "bbc", "nyt", "inshorts", "guardian")),
    PRODUCTIVITY("Productivity", 0, listOf("calendar", "notes", "drive", "docs")),
    OTHER("Other", 0, emptyList());

    fun matches(target: String): Boolean = keywords.any { target.contains(it) }
}

data class InstalledApp(
    val packageName: String,
    val label: String,
    val category: AppCategory,
    val lockDurationMinutes: Int
)

data class AppToggle(
    val app: InstalledApp,
    val isBlocked: Boolean,
    val allowUntilMillis: Long?,
    val lockInfo: ActivationLockEntry?
) {
    val lockUntilMillis: Long? = lockInfo?.lockUntilMillis
    val graceUntilMillis: Long? = lockInfo?.graceUntilMillis
}

data class AppUiState(
    val apps: List<AppToggle> = emptyList(),
    val isLoading: Boolean = true,
    val overlayGranted: Boolean = false,
    val accessibilityGranted: Boolean = false,
    val showLanding: Boolean = true,
    val blockedCount: Int = 0,
    val rewardMinutes: Int = 0
)

sealed interface AppUiEvent {
    data class LockEngaged(val appLabel: String, val unlockAtMillis: Long, val durationMinutes: Int) : AppUiEvent
    data class ToggleLocked(val appLabel: String, val unlockAtMillis: Long) : AppUiEvent
    data class ShowPaywall(val appLabel: String) : AppUiEvent
    data class FreeLimitReached(val limit: Int) : AppUiEvent
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()
    private val blockerPreferences = context.appBlockerPreferences()
    private val rewardRepository = RewardRepository(context)
    private val packageManager: PackageManager = context.packageManager

    private val overlayPermissionState = MutableStateFlow(checkOverlayPermission())
    private val accessibilityPermissionState = MutableStateFlow(isAccessibilityEnabled())

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AppUiEvent>()
    val events: SharedFlow<AppUiEvent> = _events.asSharedFlow()

    @Volatile
    private var currentActivationLocks: Map<String, ActivationLockEntry> = emptyMap()
    @Volatile
    private var lastDisabledMap: Map<String, Long> = emptyMap()

    init {
        viewModelScope.launch {
            val baseFlow = combine(
                loadInstalledApps(),
                blockerPreferences.blockedPackages,
                blockerPreferences.temporaryAllowances,
                blockerPreferences.activationLocks,
                blockerPreferences.lastDisabled
            ) { apps, blocked, allowances, activationLocks, lastDisabled ->
                CombinedSnapshot(
                    apps = apps,
                    blocked = blocked,
                    allowances = allowances,
                    activationLocks = activationLocks,
                    lastDisabled = lastDisabled
                )
            }

            val preparedFlow = combine(baseFlow, rewardRepository.rewardState) { baseSnapshot, rewards ->
                lastDisabledMap = baseSnapshot.lastDisabled

                val now = System.currentTimeMillis()

                val activeAllowances = baseSnapshot.allowances.filterValues { it > now }
                val expiredAllowances = baseSnapshot.allowances.filterValues { it <= now }.keys
                if (expiredAllowances.isNotEmpty()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        expiredAllowances.forEach { blockerPreferences.clearTemporaryAllowance(it) }
                    }
                }

                val activeLocks = baseSnapshot.activationLocks.filterValues { it.lockUntilMillis > now }
                val expiredLocks = baseSnapshot.activationLocks.filterValues { it.lockUntilMillis <= now }.keys
                if (expiredLocks.isNotEmpty()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        expiredLocks.forEach { blockerPreferences.clearActivationLock(it) }
                    }
                }
                currentActivationLocks = activeLocks

                val toggles = baseSnapshot.apps.map { app ->
                    AppToggle(
                        app = app,
                        isBlocked = baseSnapshot.blocked.contains(app.packageName),
                        allowUntilMillis = activeAllowances[app.packageName],
                        lockInfo = activeLocks[app.packageName]
                    )
                }.sortedWith(compareBy<AppToggle> { it.app.category.ordinal }
                    .thenBy { it.app.label.lowercase() })

                PreparedState(
                    toggles = toggles,
                    blockedCount = toggles.count { it.isBlocked },
                    rewardMinutes = rewards.formattedRewardMinutes()
                )
            }

            combine(
                preparedFlow,
                overlayPermissionState,
                accessibilityPermissionState,
                blockerPreferences.landingCompleted
            ) { prepared, overlayGranted, accessibilityGranted, landingCompleted ->
                AppUiState(
                    apps = prepared.toggles,
                    isLoading = false,
                    overlayGranted = overlayGranted,
                    accessibilityGranted = accessibilityGranted,
                    showLanding = !landingCompleted,
                    blockedCount = prepared.blockedCount,
                    rewardMinutes = prepared.rewardMinutes
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
                    val labelLower = label.lowercase()
                    val packageLower = info.packageName.lowercase()
                    val category = categorizeApp(labelLower, packageLower)
                    val lockMinutes = BASE_LOCK_MINUTES + category.lockBonusMinutes
                    InstalledApp(
                        packageName = info.packageName,
                        label = label.ifBlank { info.packageName },
                        category = category,
                        lockDurationMinutes = lockMinutes
                    )
                }
                .distinctBy { it.packageName }
        }
        emit(apps)
    }

    fun onToggleChanged(toggle: AppToggle, shouldBlock: Boolean) {
        viewModelScope.launch {
            val packageName = toggle.app.packageName
            val now = System.currentTimeMillis()
            if (shouldBlock) {
                if (!toggle.isBlocked) {
                    val blockedCount = uiState.value.blockedCount
                    if (blockedCount >= FREE_PLAN_MAX_BLOCKED_APPS) {
                        _events.emit(AppUiEvent.FreeLimitReached(FREE_PLAN_MAX_BLOCKED_APPS))
                        return@launch
                    }
                }
                val graceEligible = lastDisabledMap[packageName]?.let { now - it <= GRACE_WINDOW_MILLIS } ?: false
                val lockDurationMinutes = toggle.app.lockDurationMinutes
                val lockExpiry = now + lockDurationMinutes * 60_000L
                val graceExpiry = if (graceEligible) now + GRACE_WINDOW_MILLIS else now - 1
                withContext(Dispatchers.IO) {
                    blockerPreferences.setBlocked(packageName, true)
                    blockerPreferences.setActivationLock(packageName, lockExpiry, graceExpiry)
                    rewardRepository.markBlocked(now)
                }
                currentActivationLocks = currentActivationLocks + (packageName to ActivationLockEntry(lockExpiry, graceExpiry))
                _events.emit(AppUiEvent.LockEngaged(toggle.app.label, lockExpiry, lockDurationMinutes))
            } else {
                val lockInfo = currentActivationLocks[packageName]
                if (lockInfo != null && now > lockInfo.graceUntilMillis) {
                    _events.emit(AppUiEvent.ToggleLocked(toggle.app.label, lockInfo.lockUntilMillis))
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    blockerPreferences.clearActivationLock(packageName)
                    blockerPreferences.setBlocked(packageName, false)
                    blockerPreferences.setLastDisabled(packageName, now)
                    rewardRepository.recordActivity(now)
                }
                currentActivationLocks = currentActivationLocks - packageName
                lastDisabledMap = lastDisabledMap + (packageName to now)
            }
        }
    }

    fun refreshPermissions() {
        overlayPermissionState.value = checkOverlayPermission()
        accessibilityPermissionState.value = isAccessibilityEnabled()
    }

    fun dismissLanding() {
        viewModelScope.launch(Dispatchers.IO) {
            blockerPreferences.markLandingCompleted()
        }
    }

    fun requestEarlyUnlock(toggle: AppToggle) {
        viewModelScope.launch {
            _events.emit(AppUiEvent.ShowPaywall(toggle.app.label))
        }
    }

    fun getAppIcon(packageName: String): Drawable? =
        runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()

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

    private fun categorizeApp(labelLower: String, packageLower: String): AppCategory =
        AppCategory.values().firstOrNull { category ->
            category != AppCategory.OTHER && (category.matches(labelLower) || category.matches(packageLower))
        } ?: AppCategory.OTHER
}

private data class CombinedSnapshot(
    val apps: List<InstalledApp>,
    val blocked: Set<String>,
    val allowances: Map<String, Long>,
    val activationLocks: Map<String, ActivationLockEntry>,
    val lastDisabled: Map<String, Long>
)

private data class PreparedState(
    val toggles: List<AppToggle>,
    val blockedCount: Int,
    val rewardMinutes: Int
)
