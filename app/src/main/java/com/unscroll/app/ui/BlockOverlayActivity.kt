package com.unscroll.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unscroll.app.data.RewardRepository
import com.unscroll.app.data.RewardState
import com.unscroll.app.data.appBlockerPreferences
import com.unscroll.app.theme.UnscrollTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class BlockOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)

        val blockedPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        if (blockedPackage.isNullOrEmpty()) {
            finish()
            return
        }
        val blockedAppLabel = intent.getStringExtra(EXTRA_APP_LABEL) ?: "This app"
        val lockUntilMillis = intent.getLongExtra(EXTRA_LOCK_UNTIL, -1L).takeIf { it > 0 }

        setContent {
            UnscrollTheme {
                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xDD000000),
                        Color(0xFF161616)
                    )
                )
                val scope = rememberCoroutineScope()
                val minuteOptions = listOf(1, 5, 15)
                val selectedMinutes = remember { mutableStateOf(5) }
                val rewardRepository = remember { RewardRepository(applicationContext) }
                val rewardState by rewardRepository.rewardState.collectAsState(initial = RewardState(0L, 0L, 0L))
                val rewardMinutes = rewardState.availableRewardSeconds.toInt() / 60
                val selectedRewardMinutes = remember { mutableIntStateOf(1) }
                var remainingSeconds by remember(lockUntilMillis) {
                    mutableLongStateOf(
                        lockUntilMillis?.let { ((it - System.currentTimeMillis()).coerceAtLeast(0L)) / 1_000L } ?: 0L
                    )
                }
                LaunchedEffect(lockUntilMillis) {
                    lockUntilMillis?.let { target ->
                        while (true) {
                            val secondsLeft = ((target - System.currentTimeMillis()).coerceAtLeast(0L)) / 1_000L
                            remainingSeconds = secondsLeft
                            if (secondsLeft <= 0L) break
                            delay(1_000L)
                        }
                    }
                }
                LaunchedEffect(rewardMinutes) {
                    if (rewardMinutes <= 0) {
                        selectedRewardMinutes.intValue = 1
                    } else if (selectedRewardMinutes.intValue > rewardMinutes) {
                        selectedRewardMinutes.intValue = rewardMinutes.coerceAtLeast(1)
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradient),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = blockedAppLabel,
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Paused for now. Choose focus or claim a mindful window.",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            lockUntilMillis?.let {
                                Text(
                                    text = "${blockedAppLabel} unlocks in ${formatCountdown(remainingSeconds)}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Earned minutes",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                minuteOptions.forEach { minutes ->
                                    val isSelected = selectedMinutes.value == minutes
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clickable {
                                                selectedMinutes.value = minutes
                                            },
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.12f),
                                        contentColor = if (isSelected) Color.Black else Color.White,
                                        shape = RoundedCornerShape(18.dp),
                                        tonalElevation = if (isSelected) 4.dp else 0.dp
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$minutes min",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Rewards bank: $rewardMinutes min",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    if (rewardMinutes >= 1) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val chips = listOf(1, 2, 3, 4, 5).filter { it <= rewardMinutes }
                                            chips.forEach { chipValue ->
                                            Surface(
                                                modifier = Modifier
                                                    .height(40.dp)
                                                    .weight(1f),
                                                shape = CircleShape,
                                                color = if (selectedRewardMinutes.intValue == chipValue) Color.White else Color.White.copy(alpha = 0.12f),
                                                contentColor = if (selectedRewardMinutes.intValue == chipValue) Color.Black else Color.White
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 8.dp)
                                                        .clickable { selectedRewardMinutes.intValue = chipValue },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = "$chipValue min")
                                                }
                                            }
                                            }
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                val minutesToSpend = selectedRewardMinutes.intValue
                                                scope.launch {
                                                    val success = rewardRepository.consumeReward(minutesToSpend * 60L)
                                                    if (success) {
                                                        rewardRepository.markBlocked(System.currentTimeMillis())
                                                        applicationContext.appBlockerPreferences().grantTemporaryAllowance(
                                                            blockedPackage,
                                                            minutesToSpend
                                                        )
                                                        this@BlockOverlayActivity.finish()
                                                    }
                                                }
                                            },
                                            shape = CircleShape,
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color.White,
                                                contentColor = Color.Black
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(text = "Use reward")
                                        }
                                    } else {
                                        Text(
                                            text = "Earn 5 minutes by staying off watched apps for an hour.",
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    scope.launch {
                                        rewardRepository.markBlocked(System.currentTimeMillis())
                                        applicationContext.appBlockerPreferences()
                                            .grantTemporaryAllowance(
                                                blockedPackage,
                                                selectedMinutes.value
                                            )
                                        this@BlockOverlayActivity.finish()
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Unlock for ${selectedMinutes.value} minutes",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    this@BlockOverlayActivity.startActivity(homeIntent)
                                    this@BlockOverlayActivity.finish()
                                },
                                shape = CircleShape,
                                border = BorderStroke(1.5.dp, Color.White),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Dismiss",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_APP_LABEL = "extra_app_label"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_LOCK_UNTIL = "extra_lock_until"
    }
}

private fun formatTime(epochMillis: Long): String {
    val instant = Instant.ofEpochMilli(epochMillis)
    val zoned = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    return formatter.format(zoned)
}

private fun formatCountdown(seconds: Long): String {
    val clamped = seconds.coerceAtLeast(0L)
    val hours = clamped / 3600
    val minutes = (clamped % 3600) / 60
    val sec = clamped % 60
    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%dh %02dm %02ds", hours, minutes, sec)
        minutes > 0 -> String.format(Locale.getDefault(), "%dm %02ds", minutes, sec)
        else -> String.format(Locale.getDefault(), "%ds", sec)
    }
}
