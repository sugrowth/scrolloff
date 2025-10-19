package com.unscroll.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unscroll.app.viewmodel.AppSuggestion
import com.unscroll.app.viewmodel.AppUiState

@Composable
fun OnboardingScreen(
    state: AppUiState,
    onToggleSuggestion: (AppSuggestion) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Break the Reflex",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Select the apps that trigger your mindless scrolling. You can always change this later.",
            style = MaterialTheme.typography.bodyMedium
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(state.suggestions) { suggestion ->
                val isWatched = state.watchedApps.any { it.packageName == suggestion.packageName }
                OnboardingSuggestionCard(
                    suggestion = suggestion,
                    isSelected = isWatched,
                    onToggle = { onToggleSuggestion(suggestion) }
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = state.watchedApps.isNotEmpty(),
            onClick = onContinue
        ) {
            Text(text = "Continue")
        }
    }
}

@Composable
private fun OnboardingSuggestionCard(
    suggestion: AppSuggestion,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = suggestion.displayName, style = MaterialTheme.typography.titleMedium)
            Text(text = suggestion.categoryLabel, style = MaterialTheme.typography.bodySmall)
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        }
    }
}
