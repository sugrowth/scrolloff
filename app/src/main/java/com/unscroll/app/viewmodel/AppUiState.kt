package com.unscroll.app.viewmodel

import com.unscroll.shared.domain.model.DashboardSnapshot
import com.unscroll.shared.domain.model.InterceptContext
import com.unscroll.shared.domain.model.InterceptDecision
import com.unscroll.shared.domain.model.WatchedApp

data class AppUiState(
    val isLoading: Boolean = true,
    val suggestions: List<AppSuggestion> = emptyList(),
    val watchedApps: List<WatchedApp> = emptyList(),
    val dashboardSnapshot: DashboardSnapshot? = null,
    val onboardingComplete: Boolean = false,
    val activeIntercept: InterceptUiState? = null,
    val lastError: String? = null
)

data class AppSuggestion(
    val packageName: String,
    val displayName: String,
    val categoryLabel: String
)

data class InterceptUiState(
    val context: InterceptContext,
    val decision: InterceptDecision
)
