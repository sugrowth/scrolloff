package com.unscroll.app.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unscroll.app.viewmodel.AppViewModel
import com.unscroll.app.viewmodel.AppToggle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBlockerScreen(
    viewModel: AppViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Unscroll Blocker") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            PermissionBanner()

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AppList(
                    apps = uiState.apps,
                    onToggle = { toggle, checked ->
                        viewModel.setBlocked(toggle.app.packageName, checked)
                    },
                    iconLoader = { packageName ->
                        viewModel.getAppIcon(packageName)
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionBanner() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        Text(
            text = "To block apps, enable overlay and accessibility permissions.",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PermissionChip(
                label = "Overlay",
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )
            PermissionChip(
                label = "Accessibility",
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )
        }
    }
}

@Composable
private fun PermissionChip(
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(text = label)
    }
}

@Composable
private fun AppList(
    apps: List<AppToggle>,
    onToggle: (AppToggle, Boolean) -> Unit,
    iconLoader: (String) -> Drawable?
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps, key = { it.app.packageName }) { toggle ->
            AppRow(
                toggle = toggle,
                onToggle = onToggle,
                iconLoader = iconLoader
            )
        }
    }
}

@Composable
private fun AppRow(
    toggle: AppToggle,
    onToggle: (AppToggle, Boolean) -> Unit,
    iconLoader: (String) -> Drawable?
) {
    val iconBitmap = iconLoader(toggle.app.packageName)?.toBitmap()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap.asImageBitmap(),
                contentDescription = "${toggle.app.label} icon",
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp)
                    .background(MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = toggle.app.label.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = toggle.app.label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = toggle.app.packageName,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
        }

        Switch(
            checked = toggle.isBlocked,
            onCheckedChange = {
                onToggle(toggle, it)
            }
        )
    }
}
