package com.unscroll.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unscroll.app.viewmodel.AppUiState
import com.unscroll.shared.domain.model.WatchedApp

@Composable
fun DashboardScreen(
    state: AppUiState,
    onStartFocus: (Int) -> Unit,
    onSimulateIntercept: (WatchedApp) -> Unit
) {
    val snapshot = state.dashboardSnapshot

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Credits available: ${(snapshot?.creditLedger?.clampedTotal ?: 0) / 60} min",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        item {
            FocusCard(onStartFocus = onStartFocus)
        }

        if (state.watchedApps.isNotEmpty()) {
            item {
                WatchedAppsCard(
                    watchedApps = state.watchedApps,
                    onSimulateIntercept = onSimulateIntercept
                )
            }
        }

        if (!snapshot?.intercepts.isNullOrEmpty()) {
            item {
                InterceptHistoryCard(state)
            }
        }

        if (!snapshot?.insights.isNullOrEmpty()) {
            item {
                InsightsCard(state)
            }
        }
    }
}

@Composable
private fun FocusCard(onStartFocus: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Need a quick reset?", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Start a 5 minute focus timer to earn credits.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { onStartFocus(5) }) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Start Focus")
            }
        }
    }
}

@Composable
private fun WatchedAppsCard(
    watchedApps: List<WatchedApp>,
    onSimulateIntercept: (WatchedApp) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Watched Apps", style = MaterialTheme.typography.titleMedium)
            watchedApps.forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = app.displayName, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = app.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = { onSimulateIntercept(app) }) {
                        Text(text = "Intercept")
                    }
                }
                Divider()
            }
        }
    }
}

@Composable
private fun InterceptHistoryCard(state: AppUiState) {
    val intercepts = state.dashboardSnapshot?.intercepts ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Recent Decisions", style = MaterialTheme.typography.titleMedium)
            intercepts.takeLast(5).reversed().forEach { event ->
                Column {
                    Text(text = event.context.app.displayName, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Outcome: ${event.outcome.name.lowercase().replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun InsightsCard(state: AppUiState) {
    val insights = state.dashboardSnapshot?.insights ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Insights", style = MaterialTheme.typography.titleMedium)
            insights.forEach { insight ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = insight.title, style = MaterialTheme.typography.bodyLarge)
                    Text(text = insight.description, style = MaterialTheme.typography.bodySmall)
                }
                Divider()
            }
        }
    }
}
