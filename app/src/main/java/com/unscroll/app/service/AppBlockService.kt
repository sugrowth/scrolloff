package com.unscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.unscroll.app.data.appBlockerPreferences
import com.unscroll.app.ui.BlockOverlayActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AppBlockService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        val isBlocked = runBlocking {
            applicationContext.appBlockerPreferences()
                .blockedPackages
                .first()
                .contains(packageName)
        }

        if (!isBlocked) {
            if (lastBlockedPackage == packageName) {
                lastBlockedPackage = null
                lastShownTimestamp = 0L
            }
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (lastBlockedPackage == packageName && now - lastShownTimestamp < 1000L) {
            return
        }
        lastBlockedPackage = packageName
        lastShownTimestamp = now

        val label = runCatching {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(info).toString()
        }.getOrElse {
            packageName
        }

        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockOverlayActivity.EXTRA_APP_LABEL, label)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
    }

    companion object {
        private var lastBlockedPackage: String? = null
        private var lastShownTimestamp: Long = 0L
    }
}
