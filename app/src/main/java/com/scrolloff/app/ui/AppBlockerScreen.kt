package com.scrolloff.app.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrolloff.app.R
import com.scrolloff.app.viewmodel.AppCategory
import com.scrolloff.app.viewmodel.AppToggle
import com.scrolloff.app.viewmodel.AppUiEvent
import com.scrolloff.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBlockerScreen(
    viewModel: AppViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AppUiEvent.LockEngaged -> {
                    snackbarHostState.showSnackbar(
                        message = "${event.appLabel} locked for ${formatDuration(event.durationMinutes)} (until ${formatTime(event.unlockAtMillis)})."
                    )
                }
                is AppUiEvent.ToggleLocked -> {
                    snackbarHostState.showSnackbar(
                        message = "${event.appLabel} is locked until ${formatTime(event.unlockAtMillis)}. Early unlock requires ScrollOff Pro."
                    )
                }
                is AppUiEvent.ShowPaywall -> {
                    snackbarHostState.showSnackbar(
                        message = "${event.appLabel} early unlock is part of the upcoming Pro plan."
                    )
                }
                is AppUiEvent.FreeLimitReached -> {
                    snackbarHostState.showSnackbar(
                        message = "Free plan can protect up to ${event.limit} apps. Upgrade to Pro for more."
                    )
                }
            }
        }
    }

    if (uiState.showLanding) {
        LandingScreen(onContinue = viewModel::dismissLanding)
        return
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF050505),
                Color(0xFF0F0F0F)
        )
    )
    val permissionsSatisfied = uiState.overlayGranted && uiState.accessibilityGranted

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 36.dp
                )
            ) {
                if (!permissionsSatisfied) {
                    item {
                        PermissionCard(
                            overlayGranted = uiState.overlayGranted,
                            accessibilityGranted = uiState.accessibilityGranted,
                            onOverlayRequest = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            },
                            onAccessibilityRequest = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        )
                    }
                }

                item {
                    InfoCard()
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Protected apps by priority",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Select all the apps that don't let you focus.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                    }
                }

                if (uiState.isLoading) {
                    item { LoadingCard() }
                } else if (uiState.apps.isEmpty()) {
                    item { EmptyStateCard() }
                } else {
                    val grouped = uiState.apps.groupBy { it.app.category }
                    AppCategory.values().forEach { category ->
                        val toggles = grouped[category].orEmpty()
                        if (toggles.isNotEmpty()) {
                            item {
                                CategoryHeader(
                                    title = category.displayName,
                                    subtitle = "Typical lock ${formatDuration(toggles.first().app.lockDurationMinutes)}"
                                )
                            }
                            items(toggles, key = { it.app.packageName }) { toggle ->
                                AppRow(
                                    toggle = toggle,
                                    onToggle = { checked ->
                                        viewModel.onToggleChanged(toggle, checked)
                                    },
                                    onEarlyUnlock = { viewModel.requestEarlyUnlock(toggle) },
                                    iconLoader = { packageName ->
                                        viewModel.getAppIcon(packageName)
                                    }
                                )
                            }
                        }
                    }
                }

                item { RewardCard(uiState.rewardMinutes) }
                item { AboutCard() }
            }
        }
    }
}

@Composable
private fun LandingScreen(
    onContinue: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF090909),
            Color(0xFF121212)
        )
    )
    val messages = listOf(
        "Give yourself more time.",
        "Dopamine should come from real things, not endless feeds.",
        "Stop doomscrolling. Work toward your goals.",
        "Win your focus back, one unlock at a time."
    )

    val selectedMessage = remember { messages.random() }
    val dismissed = remember { mutableStateOf(false) }

    fun proceed() {
        if (!dismissed.value) {
            dismissed.value = true
            onContinue()
        }
    }

    LaunchedEffect(selectedMessage) {
        delay(10_000)
        proceed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .clickable { proceed() }
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(112.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_hourglass_logo),
                    contentDescription = "ScrollOff",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(20.dp)
                )
            }
            Text(
                text = selectedMessage,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PermissionCard(
    overlayGranted: Boolean,
    accessibilityGranted: Boolean,
    onOverlayRequest: () -> Unit,
    onAccessibilityRequest: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Turn on both permissions so ScrollOff can pause distraction apps.",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PermissionStatusPill(
                    label = "Overlay",
                    granted = overlayGranted,
                    onRequest = onOverlayRequest
                )
                PermissionStatusPill(
                    label = "Accessibility",
                    granted = accessibilityGranted,
                    onRequest = onAccessibilityRequest
                )
            }
        }
    }
}

@Composable
private fun PermissionStatusPill(
    label: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    if (granted) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = CircleShape
        ) {
            Text(
                text = "$label enabled",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    } else {
        FilledTonalButton(
            onClick = onRequest,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = CircleShape
        ) {
            Text(text = "Enable $label")
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Free plan protects up to 5 apps. Locks last 4–6 hours depending on the category.",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "Need to turn something off sooner? You have a 5-minute grace window after enabling. Beyond that, early unlock will require the upcoming ScrollOff Pro upgrade.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun RewardCard(rewardMinutes: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Earned unlock minutes",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "Stay off your watched apps for an hour to earn a 5-minute reward. Spend it on any locked app.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = "$rewardMinutes min available",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = if (rewardMinutes >= 5) "Open a blocked app and tap 'Use reward' to spend minutes." else "Earn 5 minutes to unlock once without credits.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No apps selected yet.",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Toggle any app to place it under ScrollOff’s care.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.White.copy(alpha = 0.8f)
            )
        )
    }
}

@Composable
private fun AppRow(
    toggle: AppToggle,
    onToggle: (Boolean) -> Unit,
    onEarlyUnlock: () -> Unit,
    iconLoader: (String) -> Drawable?
) {
    val now = System.currentTimeMillis()
    val iconBitmap = iconLoader(toggle.app.packageName)?.toBitmap()
    val allowUntil = toggle.allowUntilMillis?.takeIf { it > now }
    val lockInfo = toggle.lockInfo
    val lockUntil = lockInfo?.lockUntilMillis?.takeIf { it > now }
    val isInGraceWindow = lockInfo?.let { now <= it.graceUntilMillis } ?: false
    val switchEnabled = lockInfo == null || isInGraceWindow
    val allowanceText = allowUntil?.let { formatTime(it) }
    val lockText = lockUntil?.let { formatTime(it) }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap.asImageBitmap(),
                        contentDescription = "${toggle.app.label} icon",
                        modifier = Modifier
                            .height(48.dp)
                            .width(48.dp)
                    )
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = toggle.app.label.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = toggle.app.label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = toggle.app.packageName,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(toggle.app.category.displayName) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "${formatDuration(toggle.app.lockDurationMinutes)} lock",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                val switchColors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    disabledCheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                )
                Switch(
                    checked = toggle.isBlocked,
                    onCheckedChange = onToggle,
                    enabled = switchEnabled,
                    colors = switchColors
                )
            }

            if (allowanceText != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "Unlocked until $allowanceText",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            if (lockUntil != null) {
                if (isInGraceWindow) {
                    val graceText = lockInfo?.graceUntilMillis?.let { formatTime(it) } ?: "soon"
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Grace window active — adjust before $graceText",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Locked until $lockText",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    AssistChip(
                        onClick = onEarlyUnlock,
                        label = {
                            Text("Unlock early (Pro)")
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            } else {
                Text(
                    text = "Custom lock duration with ScrollOff Pro.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "About ScrollOff",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "ScrollOff disrupts reflex opens and rewards real-world focus. Earn minutes by staying present, trade them for intentional unlocks, and let the app handle graceful relocks.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = "MVP v2.0 introduces smarter intercepts, notification-aware controls, and context-friendly bypasses so discipline feels supportive—not punitive.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

private fun formatTime(expiryMillis: Long): String {
    val instant = Instant.ofEpochMilli(expiryMillis)
    val zoned = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    return formatter.format(zoned)
}

private fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) {
        if (remainingMinutes > 0) "${hours}h ${remainingMinutes}m" else "${hours}h"
    } else {
        "${remainingMinutes}m"
    }
}
