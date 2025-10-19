package com.unscroll.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unscroll.app.viewmodel.InterceptUiState
import com.unscroll.shared.domain.model.InterceptAction
import com.unscroll.shared.domain.model.InterceptOutcome

@Composable
fun InterceptSheet(
    intercept: InterceptUiState,
    onOutcomeSelected: (InterceptOutcome) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = intercept.context.app.displayName,
            style = MaterialTheme.typography.titleLarge
        )

        val recommendation = when (val action = intercept.decision.recommendedAction) {
            is InterceptAction.Allow -> "Suggested: Spend credits for a ${action.durationSeconds / 60} minute window."
            is InterceptAction.NotificationPeek -> "Suggested: Quick notification peek (${action.durationSeconds / 60} min)."
            is InterceptAction.PromptFocus -> "Suggested: Run a ${action.durationMinutes} minute focus."
            InterceptAction.Block -> "Suggested: Stay focused for now."
        }
        Text(text = recommendation, style = MaterialTheme.typography.bodyMedium)
        intercept.decision.reasoning.forEach { reason ->
            Text(text = "- $reason", style = MaterialTheme.typography.bodySmall)
        }

        if (intercept.decision.recommendedAction is InterceptAction.NotificationPeek) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOutcomeSelected(InterceptOutcome.NOTIFICATION_PEEK) }
            ) {
                Text(text = "Open briefly for notifications")
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onOutcomeSelected(InterceptOutcome.SKIP) }
        ) {
            Text(text = "Skip and stay focused")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onOutcomeSelected(InterceptOutcome.START_FOCUS) }
        ) {
            Text(text = "Start a focus session")
        }

        if (intercept.decision.creditCostSeconds > 0) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOutcomeSelected(InterceptOutcome.USE_CREDITS) }
            ) {
                Text(text = "Spend ${(intercept.decision.creditCostSeconds / 60)} minutes of credits")
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onOutcomeSelected(InterceptOutcome.EMERGENCY_BYPASS) }
        ) {
            Text(text = "Emergency bypass")
        }
    }
}
