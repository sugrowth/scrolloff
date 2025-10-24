package com.scrolloff.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.scrolloff.app.data.ActivationLockEntry
import com.scrolloff.app.data.RewardRepository
import com.scrolloff.app.data.appBlockerPreferences
import com.scrolloff.app.ui.BlockOverlayActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AppBlockService : AccessibilityService() {

    private val rewardRepository by lazy { RewardRepository(applicationContext) }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        val preferences = applicationContext.appBlockerPreferences()
        val (blockedPackages, allowances, activationLocks) = runBlocking {
            val blockedDeferred = async { preferences.blockedPackages.first() }
            val allowancesDeferred = async { preferences.temporaryAllowances.first() }
            val locksDeferred = async { preferences.activationLocks.first() }
            Triple(blockedDeferred.await(), allowancesDeferred.await(), locksDeferred.await())
        }

        val nowRealtime = SystemClock.elapsedRealtime()
        val nowWall = System.currentTimeMillis()

        val allowanceExpiry = allowances[packageName]
        if (!blockedPackages.contains(packageName) || allowanceExpiry != null) {
            runBlocking { rewardRepository.recordActivity(nowWall) }
        } else {
            runBlocking { rewardRepository.markBlocked(nowWall) }
        }

        if (allowanceExpiry != null) {
            if (allowanceExpiry > nowWall) {
                val label = resolveLabel(packageName)
                scheduleAllowanceEnd(packageName, allowanceExpiry, label)
                return
            } else {
                runBlocking { preferences.clearTemporaryAllowance(packageName) }
                cancelAllowanceTimer(packageName)
            }
        } else {
            cancelAllowanceTimer(packageName)
        }

        val lockInfo: ActivationLockEntry? = activationLocks[packageName]

        if (!blockedPackages.contains(packageName)) {
            if (lastBlockedPackage == packageName) {
                lastBlockedPackage = null
                lastShownTimestamp = 0L
            }
            return
        }

        if (lastBlockedPackage == packageName && nowRealtime - lastShownTimestamp < 1000L) {
            return
        }
        lastBlockedPackage = packageName
        lastShownTimestamp = nowRealtime

        val label = resolveLabel(packageName)
        cancelAllowanceTimer(packageName)

        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockOverlayActivity.EXTRA_APP_LABEL, label)
            putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, packageName)
            lockInfo?.let {
                putExtra(BlockOverlayActivity.EXTRA_LOCK_UNTIL, it.lockUntilMillis)
            }
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
    }

    companion object {
        private var lastBlockedPackage: String? = null
        private var lastShownTimestamp: Long = 0L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val allowanceCallbacks = mutableMapOf<String, Runnable>()

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        allowanceCallbacks.clear()
    }

    private fun resolveLabel(packageName: String): String = runCatching {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, 0)
        }
        packageManager.getApplicationLabel(info).toString()
    }.getOrElse { packageName }

    private fun scheduleAllowanceEnd(packageName: String, expiry: Long, label: String) {
        val delay = expiry - System.currentTimeMillis()
        if (delay <= 0L) {
            launchOverlay(packageName, label)
            return
        }
        val task = Runnable {
            val preferences = applicationContext.appBlockerPreferences()
            val stillAllowed = runBlocking {
                preferences.temporaryAllowances.first()[packageName]?.let { it > System.currentTimeMillis() }
                    ?: false
            }
            if (stillAllowed) {
                val nextExpiry = runBlocking {
                    preferences.temporaryAllowances.first()[packageName] ?: expiry
                }
                scheduleAllowanceEnd(packageName, nextExpiry, label)
            } else {
                runBlocking { preferences.clearTemporaryAllowance(packageName) }
                launchOverlay(packageName, label)
            }
        }
        cancelAllowanceTimer(packageName)
        allowanceCallbacks[packageName] = task
        handler.postDelayed(task, delay)
    }

    private fun launchOverlay(packageName: String, label: String) {
        val lockInfo = runBlocking {
            applicationContext.appBlockerPreferences().activationLocks.first()[packageName]
        }
        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockOverlayActivity.EXTRA_APP_LABEL, label)
            putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, packageName)
            lockInfo?.let {
                putExtra(BlockOverlayActivity.EXTRA_LOCK_UNTIL, it.lockUntilMillis)
            }
        }
        startActivity(intent)
    }

    private fun cancelAllowanceTimer(packageName: String) {
        allowanceCallbacks.remove(packageName)?.let { handler.removeCallbacks(it) }
    }
}
