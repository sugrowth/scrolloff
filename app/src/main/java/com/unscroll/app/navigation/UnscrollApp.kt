package com.unscroll.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.unscroll.app.ui.DashboardScreen
import com.unscroll.app.ui.OnboardingScreen
import com.unscroll.app.ui.components.InterceptSheet
import com.unscroll.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnscrollApp(appViewModel: AppViewModel) {
    val uiState by appViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (!uiState.onboardingComplete) {
            OnboardingScreen(
                state = uiState,
                onToggleSuggestion = appViewModel::toggleWatchedApp,
                onContinue = appViewModel::completeOnboarding
            )
        } else {
            DashboardScreen(
                state = uiState,
                onStartFocus = { targetMinutes ->
                    appViewModel.recordFocusSession(targetMinutes, targetMinutes, null)
                },
                onSimulateIntercept = appViewModel::simulateIntercept
            )
        }

        uiState.activeIntercept?.let { intercept ->
            ModalBottomSheet(
                onDismissRequest = appViewModel::dismissIntercept
            ) {
                InterceptSheet(
                    intercept = intercept,
                    onOutcomeSelected = appViewModel::resolveIntercept
                )
            }
        }
    }
}
