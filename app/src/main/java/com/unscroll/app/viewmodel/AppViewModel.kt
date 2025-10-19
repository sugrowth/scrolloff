package com.unscroll.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unscroll.shared.domain.model.InterceptContext
import com.unscroll.shared.domain.model.InterceptOutcome
import com.unscroll.shared.domain.model.IntentTag
import com.unscroll.shared.domain.model.WatchedApp
import com.unscroll.shared.domain.model.AppCategory
import com.unscroll.shared.di.SharedContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AppViewModel(
    private val container: SharedContainer,
    private val clock: Clock = Clock.System
) : ViewModel() {

    private val suggestions = listOf(
        AppSuggestion(packageName = "com.instagram.android", displayName = "Instagram", categoryLabel = "Social"),
        AppSuggestion(packageName = "com.zhiliaoapp.musically", displayName = "TikTok", categoryLabel = "Short Video"),
        AppSuggestion(packageName = "com.google.android.youtube", displayName = "YouTube", categoryLabel = "Video"),
        AppSuggestion(packageName = "com.twitter.android", displayName = "X (Twitter)", categoryLabel = "Social"),
        AppSuggestion(packageName = "com.reddit.frontpage", displayName = "Reddit", categoryLabel = "Communities")
    )

    private val _uiState = MutableStateFlow(
        AppUiState(
            isLoading = true,
            suggestions = suggestions
        )
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        observeDashboard()
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            container.observeDashboardStateUseCase()
                .collect { snapshot ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            watchedApps = snapshot.watchedApps,
                            dashboardSnapshot = snapshot,
                            onboardingComplete = snapshot.watchedApps.isNotEmpty(),
                            lastError = null
                        )
                    }
                }
        }
    }

    fun toggleWatchedApp(suggestion: AppSuggestion) {
        viewModelScope.launch {
            val current = container.watchedAppRepository.observeWatchedApps().first()
            val existing = current.firstOrNull { it.packageName == suggestion.packageName }
            if (existing == null) {
                container.watchedAppRepository.addApp(
                    WatchedApp(
                        packageName = suggestion.packageName,
                        displayName = suggestion.displayName,
                        category = AppCategory.SOCIAL,
                        createdAt = clock.now()
                    )
                )
            } else {
                container.watchedAppRepository.removeApp(existing.packageName)
            }
        }
    }

    fun recordFocusSession(targetMinutes: Int, completedMinutes: Int, intentTag: IntentTag?) {
        viewModelScope.launch {
            container.registerFocusSessionUseCase(
                targetMinutes = targetMinutes,
                completedMinutes = completedMinutes,
                intentTag = intentTag
            )
        }
    }

    fun simulateIntercept(app: WatchedApp, notificationTriggered: Boolean = false) {
        viewModelScope.launch {
            val ledger = container.creditRepository.observeLedger().first()
            val context = InterceptContext(
                app = app,
                timestamp = clock.now(),
                availableCreditsSeconds = ledger.clampedTotal,
                streakDays = container.observeDashboardStateUseCase().first().streak.current,
                activeContextRule = container.contextRuleRepository.observeRules().first().firstOrNull(),
                notificationTriggered = notificationTriggered,
                recentSlipCount = container.interceptRepository.observeRecent(limit = 5).first().count { it.isSlip }
            )
            val decision = container.evaluateInterceptDecisionUseCase(context)
            _uiState.update {
                it.copy(
                    activeIntercept = InterceptUiState(
                        context = context,
                        decision = decision
                    )
                )
            }
        }
    }

    fun resolveIntercept(outcome: InterceptOutcome) {
        val interceptState = _uiState.value.activeIntercept ?: return
        viewModelScope.launch {
            container.recordInterceptOutcomeUseCase(
                context = interceptState.context,
                decision = interceptState.decision,
                outcome = outcome
            )
            if (outcome == InterceptOutcome.START_FOCUS) {
                recordFocusSession(targetMinutes = 5, completedMinutes = 5, intentTag = interceptState.context.intentTag)
            }
            _uiState.update {
                it.copy(activeIntercept = null)
            }
        }
    }

    fun dismissIntercept() {
        _uiState.update { it.copy(activeIntercept = null) }
    }

    fun completeOnboarding() {
        _uiState.update { it.copy(onboardingComplete = true) }
    }
}

class AppViewModelFactory(
    private val container: SharedContainer
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(container) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
