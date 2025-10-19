# Product Requirements Document (PRD)
**Product:** Unscroll — Break the Reflex Loop  
**Version:** MVP v2.0 (Enhanced with Behavioral Intelligence)  
**Date:** October 19, 2025 (America/Toronto)  
**Owner:** You (Founder)  
**Prepared for:** AI Coding Agent / Small Dev Team  

---

## 0. Executive Summary
Unscroll eliminates mindless doomscrolling by **intercepting reflex app opens** and **rewarding real-world focus** with limited "scroll windows." Users mark distraction apps (e.g., Instagram, YouTube, TikTok, etc.), which remain **locked by default.** They **earn credits** by staying scroll-free or completing short focus sessions; these credits unlock distraction apps for a fixed duration before auto-locking again.

**What's New in v2.0:**
- **Intelligent notification handling** prevents users from missing important messages
- **Habit pattern recognition** reveals when impulses strike most
- **Intent logging** deepens behavioral awareness
- **Contextual auto-bypass** reduces friction for legitimate use cases
- **Credit management system** prevents hoarding and encourages healthy usage

The app is built with **cross-platform parity** in mind — Android MVP first (Kotlin + Jetpack Compose), followed by iOS (Swift + SwiftUI) using Apple's **Screen Time APIs (DeviceActivity / ManagedSettings)**. Shared logic will be built using **Kotlin Multiplatform (KMM)** to ensure consistent behavior across both platforms.

**MVP Outcome:**  
A reliable, battery-friendly app that:
1. Intercepts launches of selected apps intelligently
2. Handles notifications without full app unlocks
3. Grants time-boxed unlocks based on earned credits
4. Re-locks on expiry with smart context awareness
5. Displays streaks, patterns, and behavioral insights
6. Adapts to user context (time, location, calendar)

---

## 1. Problem, Goals, Non-Goals
### 1.1 Problem
People reflexively open "scroll apps" dozens of times a day — not by choice, but by habit. Existing blockers either over-restrict (causing frustration) or require manual activation (which users forget). They also fail to account for legitimate notifications and context, leading to abandonment.

Unscroll bridges the gap by combining **behavioral awareness** with **reward-based discipline** while respecting the user's real-world needs.

### 1.2 Goals (MVP v2.0)
- **G1:** Reduce reflex app opens by **≥50%** within 14 days.
- **G2:** Achieve **≥70%** intercept acceptance rate (Skip/Earn vs Scroll Anyway) by week 2 (increased from 60%).
- **G3:** Keep **battery overhead <3%/day**.
- **G4:** Reach **Day-7 retention ≥50%** (increased from 40%).
- **G5:** **≤5% notification-related bypasses** in week 1 (new metric).
- **G6:** Users discover **≥1 pattern insight** within 7 days (new metric).

### 1.3 Non-Goals (MVP)
- No AI chatbot or productivity planner.
- No full-screen blockers or VPN-based controls.
- No browser or desktop extension (future phase).
- No social leaderboards (1-on-1 accountability only in Phase 1.5).

---

## 2. Users & Personas
- **Busy Builder (25–40):** Constantly context-switching, opens socials between work tasks. Wants gentle accountability without missing work notifications.
- **Student (16–24):** Procrastinates with short, frequent scrolls. Wants game-like streaks and understanding of when they're weakest.
- **Professional Parent (30–45):** Wants reduced distraction without cutting off critical apps like calls, messaging, or family coordination apps.

**New Consideration:** All personas need **notification-aware blocking** to avoid feeling "locked out" of important communications.

Accessibility-first: large tap targets, readable typography, high contrast, and screen reader labels.

---

## 3. Core Concepts & Definitions
- **Watched Apps:** User-selected distraction apps. Locked by default.
- **Intercept:** Overlay (Android) or Shield (iOS) shown when a watched app launches.
- **Credits:** Units earned by staying scroll-free or completing focus sessions. **Max cap: 180 minutes** to prevent hoarding.
- **Credit Decay:** Credits older than 48 hours expire at 50% rate to encourage regular usage.
- **Unlock Window:** Temporary duration where watched apps are usable.
- **Slip:** A recorded event when user overrides an intercept.
- **Notification Peek:** 2-minute mini-unlock granted when user clicks a notification from a watched app.
- **Intent Tag:** User-selected reason for opening an app (Bored, Checking notification, Looking for something, Habit).
- **Pattern Insight:** AI-detected trend in user behavior (e.g., "Most slips happen after 8pm").
- **Context Rule:** Time/location/calendar-based auto-bypass condition.

---

## 4. User Stories & Acceptance Criteria

### US-1: Mark distraction apps
**As a** user, **I want** to choose which apps are controlled **so that** only those distractors are gated.
- **AC1.1:** Can select ≥10 apps from suggestions + search installed apps.
- **AC1.2:** App moves under control immediately after toggle.
- **AC1.3:** Can mark certain apps as "notification-safe" (quick peek allowed).

### US-2: Intercept reflex opens
**As a** user, **I want** a nudge when opening a watched app **so I can decide consciously.**
- **AC2.1:** Intercept appears ≤250ms after app launch.
- **AC2.2:** Options: **Skip**, **Earn scroll time**, **Scroll once** (with 5-second delay after week 1).
- **AC2.3:** Skip returns to home screen smoothly.
- **AC2.4:** **NEW:** Optional intent tag selection ("Why did you open this?").

### US-3: Handle notifications intelligently
**As a** user, **I want** to respond to notifications without unlocking the full app **so that** I stay connected without falling into scrolling.
- **AC3.1:** Tapping a notification from a watched app shows mini-intercept: "Quick peek (2 min)" or "Full unlock".
- **AC3.2:** Quick peek grants 2-minute access, then auto-locks.
- **AC3.3:** Notification badge previews visible on intercept screen ("3 new messages").
- **AC3.4:** Can enable "smart peek" for specific contacts/keywords (Phase 1.5).

### US-4: Earn-to-unlock
**As a** user, **I want** to earn credits for staying scroll-free **so that** I can unlock apps responsibly.
- **AC4.1:** Default ratio 60 min focus = 20 min unlock (configurable 30–120 ⇒ 10–40).
- **AC4.2:** Manual focus sessions (5, 15, 25 min) grant credits.
- **AC4.3:** Unlock windows start on demand; all watched apps open freely until timer ends.
- **AC4.4:** On expiry, apps re-lock automatically.
- **AC4.5:** **NEW:** Max credit cap of 180 minutes displayed prominently.
- **AC4.6:** **NEW:** Credits older than 48h decay by 50% (shown with "expiring soon" badge).

### US-5: Discover habit patterns
**As a** user, **I want** to see when I'm most vulnerable to impulses **so that** I can plan better.
- **AC5.1:** Show heatmap of intercepts by hour of day.
- **AC5.2:** Display insights like "You're strongest 9am-12pm" or "Most slips happen after 8pm".
- **AC5.3:** Track intent tags over time ("You opened Instagram 12 times 'out of boredom' this week").
- **AC5.4:** Weekly summary notification with top pattern.

### US-6: Set contextual rules
**As a** user, **I want** apps to auto-unlock in certain contexts **so that** I'm not blocked during legitimate use.
- **AC6.1:** Time-based rules: "Allow news apps 7-8am only".
- **AC6.2:** Calendar integration: Auto-bypass 10 min before/after events with location/video links.
- **AC6.3:** Location-based (optional): "Unlock YouTube at gym".
- **AC6.4:** High-priority app auto-bypass (camera, maps, ride-sharing) for 10 minutes.

### US-7: Insights & streaks
**As a** user, **I want** visual stats **so that** I can see my progress.
- **AC7.1:** Show daily intercepts, skips, slips, minutes earned/used.
- **AC7.2:** Display scroll-free streaks (days in a row with ≥1 skip).
- **AC7.3:** **NEW:** Pattern insights section with top vulnerability times.
- **AC7.4:** **NEW:** Intent breakdown chart.

### US-8: Emergency bypass
**As a** user, **I want** a quick bypass for emergencies **so that** I'm never locked out when needed.
- **AC8.1:** Temporary unlock (e.g., 60 mins) with confirmation.
- **AC8.2:** Event logged in analytics for transparency.
- **AC8.3:** Costs 30 minutes of future credits (prevents abuse).

---

## 5. UX / UI

### 5.1 Screens

#### 1. Onboarding (Enhanced)
- Explain concept → ask permissions (Accessibility, Overlay, Notifications, **Calendar**, **Location [optional]**) → choose watched apps → mark notification-safe apps → set ratio → enable context rules.

#### 2. Home (Enhanced)
- **Credits balance** with cap indicator (e.g., "120/180 min")
- **Expiring credits** badge if any credits >36h old
- Unlock window status
- Streak indicator
- Focus start buttons (5/15/25 min)
- **NEW:** "Your strongest time today: 9am-12pm 💪" insight card
- **NEW:** Quick access to pattern insights

#### 3. Intercept (Android Overlay / iOS Shield) - Enhanced
- Title: "Impulse detected 👀"
- Subtitle: "Open {AppName} or skip for now?"
- **NEW:** Notification preview if triggered by notification tap: "📩 3 new messages"
- **NEW:** Quick intent tags: "Why?" → [Bored] [Notification] [Looking for X] [Habit]
- Buttons:
  - **Skip** (primary)
  - **Quick Peek (2 min)** (if notification-triggered)
  - **Earn** (opens focus picker)
  - **Scroll once** (5-second delay after week 1, shows "−15 min credits" cost)
- Secondary: "Emergency Bypass (−30 min credits)"

#### 4. Unlock Bar (Enhanced)
- Persistent timer showing remaining unlock time
- **NEW:** "Time added from [Focus Session / Credits spent]" context
- Tap to extend by spending more credits

#### 5. Insights (Significantly Enhanced)
- **Daily graph:** Skips vs scrolls
- **Heatmap:** Intercepts by hour (24h grid, color-coded)
- **Pattern cards:**
  - "You skip 80% of intercepts in the morning ☀️"
  - "Most slips happen 8-10pm 🌙"
  - "When bored, you open Instagram most often"
- **Intent breakdown:** Pie chart of why user opened apps
- **Weekly summary:** Streak, top time, improvement areas

#### 6. Context Rules (New Screen)
- **Time Rules:** Add "Allow [App] during [Time Range]"
- **Calendar Sync:** Toggle "Auto-unlock near calendar events"
- **Location Rules:** (Optional permission) "Unlock [App] at [Place]"
- **High-Priority Apps:** Auto-set for maps, camera, emergency apps

#### 7. Settings (Enhanced)
- App list with notification-safe toggles
- Ratios and sensitivity
- Credit cap (120-240 min)
- Credit decay toggle
- Progressive strictness (enable "Challenge Mode")
- Themes
- **NEW:** Data export/delete

---

### 5.2 Copy Guidelines
- Tone: Empathetic, self-aware, encouraging (no guilt).  
- Use insights positively: Focus on strengths, not failures.

**Examples:**  
> "You opened Instagram 6 times today. Skip for now?"  
> "Focus earned you 20m of mindful scrolling."  
> "You've been scroll-free for 3 hours 🧠."  
> "You're crushing it in the mornings! 9am-12pm is your power window."  
> "Most slips happen after 8pm. Consider a wind-down routine?"  
> "Quick peek expires in 1:45. Respond and get back to focus!"

---

## 6. System Requirements

### 6.1 Android (MVP)
- **Language:** Kotlin + Jetpack Compose
- **Key APIs:** 
  - AccessibilityService (intercepts)
  - Overlay (intercept UI)
  - UsageStatsManager (pattern tracking)
  - NotificationListenerService (notification handling)
  - ForegroundService (credit accrual)
  - CalendarContract (calendar sync)
  - FusedLocationProviderClient (optional, location rules)
- **DB:** Room / SQLite
- **Architecture:** MVVM + Clean + Hilt

### 6.2 iOS (Phase 2)
- **Language:** Swift + SwiftUI
- **Frameworks:** 
  - FamilyControls (app selection)
  - DeviceActivity (shields)
  - ManagedSettings (blocking)
  - UserNotifications (notification handling)
  - EventKit (calendar sync)
  - CoreLocation (optional, location rules)
- **Behavior:** Use shields instead of overlays; mirror Android flow.

### 6.3 Shared Core (Cross-Platform)
- **Language:** Kotlin Multiplatform (KMM)
- **Shared modules:**
  - RewardEngine (ratios, accrual, cap, decay)
  - StateMachine (Locked → Intercepted → Peek → Unlocked)
  - PatternAnalyzer (heatmap, insights)
  - Ledger + Event Bus
  - Data models (Settings, ImpulseEvent, UnlockWindow, ContextRule, Pattern)

---

## 7. Data Models (Shared)

```kotlin
// Existing models
WatchedApp(
    packageName: String, 
    label: String, 
    enabled: Boolean,
    notificationSafe: Boolean = false  // NEW
)

CreditLedger(
    id: Int, 
    type: String,  // EARNED_PASSIVE, EARNED_FOCUS, SPENT_UNLOCK, DECAYED
    amountSeconds: Long, 
    createdTs: Long,
    expiresTs: Long  // NEW: 48h from creation
)

UnlockWindow(
    id: Int, 
    startTs: Long, 
    endTs: Long, 
    active: Boolean,
    type: String  // FULL, PEEK, EMERGENCY, CONTEXT_AUTO
)

ImpulseEvent(
    id: Int, 
    ts: Long, 
    packageName: String, 
    action: String,  // SKIP, EARN, ALLOW_ONCE, PEEK
    intentTag: String? = null,  // NEW: BORED, NOTIFICATION, LOOKING, HABIT
    triggeredByNotification: Boolean = false  // NEW
)

Settings(
    id: Int = 1, 
    ratio: Float, 
    sensitivity: String, 
    strictMode: Boolean,
    creditCapMinutes: Int = 180,  // NEW
    creditDecayEnabled: Boolean = true,  // NEW
    progressiveStrictness: Boolean = false,  // NEW
    weekNumber: Int = 1  // NEW: tracks user's week for progressive strictness
)

// NEW models
ContextRule(
    id: Int,
    type: String,  // TIME_RANGE, CALENDAR_EVENT, LOCATION, HIGH_PRIORITY
    packageName: String?,  // null = all watched apps
    enabled: Boolean,
    config: String  // JSON: {startTime, endTime} or {lat, lng, radius} etc.
)

Pattern(
    id: Int,
    type: String,  // HOURLY_HEATMAP, INTENT_BREAKDOWN, STREAK_SUMMARY
    data: String,  // JSON array of data points
    insight: String?,  // Human-readable insight
    generatedTs: Long
)
```

---

## 8. Algorithms & Logic

### 8.1 Intercept Logic (Enhanced)
```kotlin
onAccessibilityEvent(event) {
    if (event.type == TYPE_WINDOW_STATE_CHANGED) {
        val pkg = event.packageName
        
        // Check context rules first
        if (hasActiveContextRule(pkg)) {
            logEvent(pkg, "CONTEXT_AUTO_BYPASS")
            return  // Don't intercept
        }
        
        if (isWatched(pkg) && !unlockWindow.active) {
            // Check if triggered by notification
            val fromNotification = wasRecentNotification(pkg)
            showOverlay(pkg, fromNotification)
        }
    }
}

fun showOverlay(pkg: String, fromNotification: Boolean) {
    // Show intent tag options
    displayIntentTags()
    
    onSkip {
        performGlobalAction(GLOBAL_ACTION_BACK)
        log(pkg, "SKIP", selectedIntent)
    }
    
    onPeek {  // NEW: Only if fromNotification or app.notificationSafe
        startPeekWindow(pkg, 120.seconds)
        log(pkg, "PEEK", selectedIntent)
    }
    
    onEarn {
        startFocusSession()
        log(pkg, "EARN", selectedIntent)
    }
    
    onScrollOnce {
        // Week 1: Immediate
        // Week 2+: 5-second countdown
        if (settings.weekNumber > 1) {
            showCountdown(5.seconds)
        }
        whitelistTemp(pkg, 60.seconds)
        deductCredits(15.minutes)  // NEW: Costs credits
        log(pkg, "ALLOW_ONCE", selectedIntent)
    }
    
    onEmergencyBypass {
        showConfirmation("This will cost 30 minutes of future credits")
        if (confirmed) {
            startEmergencyUnlock(60.minutes)
            deductCredits(30.minutes)
            log(pkg, "EMERGENCY_BYPASS")
        }
    }
}
```

### 8.2 Credit Accrual (Enhanced)
```kotlin
// Passive accrual
fun onTimeOutsideWatchedApps(seconds: Long) {
    val credits = seconds / settings.ratio
    addCredits(credits, "EARNED_PASSIVE")
}

// Active accrual
fun onFocusSessionComplete(minutes: Int) {
    val credits = minutes * (settings.ratio / 60)
    addCredits(credits, "EARNED_FOCUS")
}

// NEW: Credit management
fun addCredits(amount: Long, type: String) {
    val newTotal = currentCredits + amount
    val cappedAmount = min(newTotal, settings.creditCapMinutes * 60) - currentCredits
    
    creditLedger.insert(
        type = type,
        amountSeconds = cappedAmount,
        createdTs = now(),
        expiresTs = now() + 48.hours
    )
    
    if (newTotal > creditCapMinutes * 60) {
        showNotification("Credit cap reached! Spend some time or increase your cap.")
    }
}

// NEW: Credit decay
fun decayCredits() {
    val expiredCredits = creditLedger.findExpired(now())
    expiredCredits.forEach { credit ->
        val decayAmount = credit.amountSeconds * 0.5
        creditLedger.insert(
            type = "DECAYED",
            amountSeconds = -decayAmount,
            createdTs = now(),
            expiresTs = now()
        )
        creditLedger.markDecayed(credit.id)
    }
}
```

### 8.3 Notification Handling (NEW)
```kotlin
onNotificationPosted(notification: StatusBarNotification) {
    val pkg = notification.packageName
    if (isWatched(pkg)) {
        // Store recent notification for context
        recentNotifications[pkg] = now()
        
        // If app is notification-safe, show preview on intercept
        if (watchedApps[pkg].notificationSafe) {
            notificationPreviews[pkg] = notification.getText()
        }
    }
}

fun wasRecentNotification(pkg: String): Boolean {
    return (now() - recentNotifications[pkg]) < 5.seconds
}
```

### 8.4 Pattern Analysis (NEW)
```kotlin
fun generatePatterns() {
    // Hourly heatmap
    val hourlyData = impulseEvents
        .groupBy { it.ts.hourOfDay }
        .mapValues { it.value.size }
    
    patterns.insert(
        type = "HOURLY_HEATMAP",
        data = hourlyData.toJson(),
        insight = generateHourlyInsight(hourlyData)
    )
    
    // Intent breakdown
    val intentData = impulseEvents
        .filter { it.intentTag != null }
        .groupBy { it.intentTag }
        .mapValues { it.value.size }
    
    patterns.insert(
        type = "INTENT_BREAKDOWN",
        data = intentData.toJson(),
        insight = generateIntentInsight(intentData)
    )
}

fun generateHourlyInsight(data: Map<Int, Int>): String {
    val strongestHour = data.maxByOrNull { it.value }?.key
    val weakestHour = data.filter { it.value > 2 }.minByOrNull { 
        impulseEvents.filter { e -> 
            e.ts.hourOfDay == it.key && e.action == "SKIP" 
        }.size.toFloat() / it.value 
    }?.key
    
    return when {
        strongestHour != null && weakestHour != null ->
            "You're strongest at ${strongestHour}:00. Most slips happen around ${weakestHour}:00."
        strongestHour != null ->
            "You're crushing it around ${strongestHour}:00! 💪"
        else -> "Keep building data for insights!"
    }
}
```

### 8.5 Context Rules (NEW)
```kotlin
fun hasActiveContextRule(pkg: String): Boolean {
    val rules = contextRules.findActive(pkg)
    
    return rules.any { rule ->
        when (rule.type) {
            "TIME_RANGE" -> {
                val config = rule.config.parseJson<TimeRangeConfig>()
                val now = now().timeOfDay
                now in config.startTime..config.endTime
            }
            "CALENDAR_EVENT" -> {
                val upcomingEvents = calendarProvider.getUpcomingEvents(30.minutes)
                upcomingEvents.any { event ->
                    event.hasVideoLink() || event.hasLocation()
                }
            }
            "LOCATION" -> {
                val config = rule.config.parseJson<LocationConfig>()
                val currentLocation = locationProvider.getLastLocation()
                currentLocation.distanceTo(config) < config.radiusMeters
            }
            "HIGH_PRIORITY" -> {
                pkg in listOf("com.google.android.apps.maps", "android.camera", "com.uber.app")
            }
            else -> false
        }
    }
}
```

### 8.6 Progressive Strictness (NEW)
```kotlin
fun updateWeekNumber() {
    val installDate = settings.installDate
    val weeksSinceInstall = (now() - installDate).days / 7
    settings.weekNumber = weeksSinceInstall + 1
}

fun getScrollOnceDelay(): Int {
    return if (settings.progressiveStrictness && settings.weekNumber > 1) {
        min(settings.weekNumber * 2, 10)  // 2s per week, max 10s
    } else {
        0
    }
}
```

---

## 9. Privacy & Security
- 100% on-device processing.
- No data sent externally by default.
- Notification content never stored (only count/presence).
- Location data (if enabled) processed locally only.
- Calendar events accessed read-only for auto-bypass logic.
- Users can export or delete all logs anytime.
- Accessibility disclosure included in onboarding.
- **NEW:** Explicit consent for calendar and location permissions with clear use case explanations.

---

## 10. Analytics (Local-First)

### Core Events
- `intercept_shown {package, fromNotification}`
- `intercept_action {action, intentTag}`
- `focus_completed {minutes}`
- `credits_earned {amount, type}`
- `credits_decayed {amount}`
- `unlock_started {duration, type}` / `unlock_expired`
- `bypass_used {duration, cost}`

### NEW Events
- `notification_peek_started {package}`
- `notification_peek_expired`
- `context_rule_triggered {ruleType, package}`
- `pattern_generated {patternType, insight}`
- `intent_tagged {package, tag}`
- `credit_cap_reached`
- `credits_expiring_soon {amount}`

---

## 11. Edge Cases

### Existing
- Overlay denied → fallback in-app alert.
- Accessibility revoked → prompt re-enable.
- Clock tampering → pause accrual, show warning.
- Battery saver → notify reduced accuracy.

### NEW
- **Notification spam:** If >20 notifications/hour from one app, prompt user to adjust notification-safe setting.
- **Calendar permission denied:** Disable calendar auto-bypass, show impact in settings.
- **Location permission denied:** Disable location rules, continue normally.
- **Credit cap reached during accrual:** Show persistent notification; excess credits discarded.
- **Negative credit balance:** Allow debt up to −30 minutes; block all unlocks until positive.
- **Pattern generation fails:** Skip insight, show generic encouragement.
- **Context rule conflict:** Priority order: HIGH_PRIORITY > CALENDAR_EVENT > LOCATION > TIME_RANGE.

---

## 12. Test Plan (MVP v2.0)

### Functional
- Verify intercept shows for all selected apps.
- Buttons trigger correct flow (Skip, Peek, Earn, Scroll once).
- Unlock ends on schedule; peek windows expire correctly.
- **NEW:** Notification-triggered intercepts show preview and peek option.
- **NEW:** Intent tags recorded and displayed in insights.
- **NEW:** Credit cap prevents over-accrual.
- **NEW:** Credit decay runs correctly after 48h.
- **NEW:** Context rules trigger auto-bypass when conditions met.
- **NEW:** Pattern insights generated daily.

### Performance
- Overlay latency ≤250ms.
- Battery usage <3% daily.
- **NEW:** Pattern analysis runs in <2s.
- **NEW:** Notification listener overhead <1% battery.

### Accessibility
- Screen reader support verified for all new UI elements.
- High-contrast mode passes AA.
- Intent tag buttons have minimum 48dp tap targets.

### Edge Cases
- Credit cap behavior under rapid accrual.
- Notification peek during active unlock window.
- Context rule conflicts resolve correctly.
- Pattern generation with sparse data (<7 days).

---

## 13. Roadmap

### Phase 1 — Android MVP v2.0 (8 Weeks)
- **W1:** Architecture setup + Enhanced permissions flow
- **W2:** AccessibilityService + NotificationListener + Overlay
- **W3:** RewardEngine with cap/decay + Focus Timer
- **W4:** Unlock Logic + Peek Windows + Context Rules
- **W5:** Pattern Analyzer + Intent Logging + Insights UI
- **W6:** Calendar/Location Integration + Progressive Strictness
- **W7:** Polish + Edge Case Handling
- **W8:** Beta Testing + Launch Prep

### Phase 1.5 — Retention Boosters (4 Weeks)
- Social accountability (1-on-1 buddy system)
- Wellness integration (Health/Fit API export)
- Advanced context rules (app-specific intent patterns)
- Customizable credit decay rates

### Phase 2 — iOS Parity Build (10 Weeks)
- Implement Shared Core (KMM) with new v2.0 modules
- Integrate Screen Time APIs (Shields + DeviceActivity)
- iOS-specific notification handling (UserNotifications framework)
- Match Android design system via SwiftUI
- Parity Testing (state transitions, patterns, context rules)

### Phase 3 — Advanced Features (Future)
- Web dashboard for long-term analytics
- Browser extension (Chrome/Safari)
- Team/family plans with shared accountability
- ML-powered personalized insights

---

## 14. Monetization

### Free Tier
- Up to 3 watched apps
- Basic insights (daily graph, streaks)
- Fixed ratios (60:20 only)
- 120-minute credit cap
- Time-based context rules only

### Pro Tier ($4.99/mo or $39.99/year)
- Unlimited watched apps
- Custom ratios (30-120 → 10-40)
- Advanced pattern insights (heatmap, intent breakdown)
- Custom themes
- Calendar + location context rules
- 240-minute credit cap
- Configurable credit decay
- 1-on-1 accountability buddy (Phase 1.5)
- Wellness app integration (Phase 1.5)
- Priority support

### Optional Add-Ons (Phase 2+)
- Family plan (5 accounts): $12.99/mo
- Lifetime Pro: $99.99 one-time

---

## 15. Definition of Done (MVP v2.0)

### Core Functionality
- All user stories (US1–US8) validated on 3 Android test devices (different OS versions).
- Intercept latency ≤250ms in 95% of launches.
- Battery <3%/day verified over 7-day test period.

### Enhanced Features
- Notification peek works correctly for 5+ popular apps (Instagram, WhatsApp, Gmail, Slack, Discord).
- Credit cap and decay tested with 100+ accrual events.
- Pattern insights generate correctly with 7+ days of data.
- Context rules tested for time, calendar, and high-priority scenarios.
- Intent tagging UI tested with 20+ users for usability.

### Quality Bars
- Privacy, accessibility, and onboarding flows validated.
- All edge cases documented and handled gracefully.
- iOS parity spec updated with v2.0 features.
- Launch-ready marketing materials prepared (screenshots, video demo, store listing).

---

## 16. Success Metrics (90 Days Post-Launch)

### Engagement
- **Daily Active Users (DAU):** 60%+ of installs
- **Day-7 Retention:** ≥50%
- **Day-30 Retention:** ≥30%

### Behavioral Impact
- **Intercept acceptance rate:** ≥70% (Skip/Earn/Peek vs Scroll Anyway)
- **Average daily skips:** ≥8 per user
- **Credit utilization:** 60%+ of earned credits spent within 48h

### Feature Adoption
- **Notification peek usage:** ≥40% of intercepts when available
- **Intent tag completion:** ≥50% of intercepts include a tag
- **Context rules enabled:** ≥30% of users set ≥1 rule by Day 14
- **Pattern insight views:** ≥2 views per week per user

### Monetization
- **Free-to-Pro conversion:** ≥8% by Day 30
- **Pro retention:** ≥70% monthly renewal rate

---

## 17. Open Questions & Decisions Needed

1. **Credit decay rate:** 50% after 48h is aggressive. Consider 25% after 72h for gentler approach?
   - **Recommendation:** A/B test both; lean toward 25% at 72h for MVP to reduce early friction.

2. **Peek window duration:** 2 minutes may be too short for complex responses. Test 3 or 5 minutes?
   - **Recommendation:** Start with 3 minutes; allow Pro users to configure 2–10 minutes.

3. **Progressive strictness opt-in:** Should this be default or require explicit activation?
   - **Recommendation:** Default ON with clear explanation during onboarding; allow toggle in settings.

4. **Intent tag requirement:** Should users be required to select an intent, or keep it optional?
   - **Recommendation:** Optional for MVP; encourage via gentle prompts ("Help us learn your patterns?").

5. **Location permission timing:** Ask during onboarding or wait until user tries to create location rule?
   - **Recommendation:** Wait until user attempts to create location rule to reduce permission fatigue.

6. **Calendar auto-bypass window:** 10 minutes before/after event may be too broad. Consider 5 minutes?
   - **Recommendation:** Make configurable (5/10/15 min) in Pro; default 10 min for Free.

7. **Negative credit debt limit:** −30 minutes may feel punishing. Consider −15 minutes?
   - **Recommendation:** Start with −15 min; monitor user feedback and adjust if needed.

---

## 18. Technical Architecture Details

### 18.1 Android Component Structure

```
app/
├── presentation/
│   ├── onboarding/
│   ├── home/
│   ├── intercept/
│   │   ├── InterceptOverlay.kt
│   │   ├── IntentTagSelector.kt
│   │   └── NotificationPreview.kt
│   ├── insights/
│   │   ├── PatternHeatmap.kt
│   │   ├── IntentBreakdown.kt
│   │   └── InsightCards.kt
│   ├── context_rules/
│   └── settings/
├── domain/
│   ├── usecases/
│   │   ├── InterceptUseCase.kt
│   │   ├── AccrueCreditsUseCase.kt
│   │   ├── ManageUnlockWindowUseCase.kt
│   │   ├── GeneratePatternsUseCase.kt
│   │   └── EvaluateContextRulesUseCase.kt
│   ├── models/
│   └── repositories/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entities/
│   │   └── UnscrollDatabase.kt
│   ├── services/
│   │   ├── InterceptAccessibilityService.kt
│   │   ├── NotificationListenerService.kt
│   │   ├── CreditAccrualService.kt
│   │   └── PatternAnalyzerService.kt
│   └── repositories/
└── shared/  (KMM modules)
    ├── RewardEngine.kt
    ├── StateMachine.kt
    ├── PatternAnalyzer.kt
    └── models/
```

### 18.2 Key Android Services

#### InterceptAccessibilityService
- **Purpose:** Detect app launches and show intercept overlay
- **Permissions:** BIND_ACCESSIBILITY_SERVICE
- **Battery Impact:** Minimal (event-driven, not polling)
- **Implementation Notes:**
  - Listen for TYPE_WINDOW_STATE_CHANGED events
  - Check against watched apps list
  - Evaluate context rules before showing intercept
  - Handle notification-triggered launches differently

#### NotificationListenerService
- **Purpose:** Track notifications from watched apps
- **Permissions:** BIND_NOTIFICATION_LISTENER_SERVICE
- **Battery Impact:** <1% (passive listener)
- **Implementation Notes:**
  - Store notification timestamps for context
  - Extract preview text for notification-safe apps
  - Respect user privacy (never log sensitive content)
  - Clean up old notification data (>1 hour) automatically

#### CreditAccrualService
- **Purpose:** Track time outside watched apps and accrue credits
- **Type:** Foreground Service (required for background processing)
- **Battery Impact:** ~2% (uses UsageStatsManager efficiently)
- **Implementation Notes:**
  - Poll every 60 seconds (not real-time to save battery)
  - Check current app against watched list
  - Apply credit decay logic every 6 hours
  - Enforce credit cap before adding new credits

#### PatternAnalyzerService
- **Purpose:** Generate insights from user behavior data
- **Type:** Background Job (WorkManager, runs daily at 3am)
- **Battery Impact:** <0.5% (runs once daily)
- **Implementation Notes:**
  - Aggregate last 7 days of impulse events
  - Generate hourly heatmap data
  - Calculate intent distribution
  - Create human-readable insights
  - Cache results to avoid repeated computation

### 18.3 iOS Architecture (Phase 2)

```
Unscroll-iOS/
├── Views/
│   ├── Onboarding/
│   ├── Home/
│   ├── ShieldConfiguration/
│   ├── Insights/
│   └── Settings/
├── ViewModels/
├── Services/
│   ├── DeviceActivityMonitor.swift
│   ├── ShieldActionHandler.swift
│   └── NotificationHandler.swift
├── Shared/  (KMM framework)
│   └── UnscrollCore.framework
└── Models/
```

**Key iOS Components:**
- **DeviceActivityMonitor:** Replaces AccessibilityService, monitors app usage
- **ShieldActionHandler:** Handles user actions on shields (similar to intercept overlay)
- **ManagedSettings:** Blocks apps when not in unlock window
- **FamilyControls:** Provides app selection UI

---

## 19. Data Flow Diagrams

### 19.1 Intercept Flow (Enhanced)

```
User taps app icon
       ↓
AccessibilityService detects launch
       ↓
Check: Is app watched? → NO → Allow launch
       ↓ YES
Check: Active unlock window? → YES → Allow launch
       ↓ NO
Check: Context rule active? → YES → Log + Allow launch
       ↓ NO
Check: Recent notification? → YES → Mark as notification-triggered
       ↓
Show InterceptOverlay
       ↓
User selects intent tag (optional)
       ↓
       ├─→ SKIP → Close app + Log event
       ├─→ PEEK (if notification) → Start 3-min window + Log
       ├─→ EARN → Show focus picker → Start focus session
       └─→ SCROLL ONCE → Deduct 15 min credits → Allow 60s → Log
```

### 19.2 Credit Management Flow

```
Time passes outside watched apps
       ↓
CreditAccrualService checks every 60s
       ↓
Calculate earned credits (time * ratio)
       ↓
Check: Would exceed cap? 
       ├─→ YES → Add only up to cap + Show notification
       └─→ NO → Add to ledger with 48h expiry
       ↓
Every 6 hours: Check for expired credits
       ↓
Apply 50% decay to credits >48h old
       ↓
Update UI with current balance
```

### 19.3 Pattern Analysis Flow

```
Daily at 3am (WorkManager scheduled job)
       ↓
Fetch last 7 days of ImpulseEvents
       ↓
Generate hourly heatmap (0-23h buckets)
       ↓
Calculate intent tag distribution
       ↓
Identify strongest/weakest hours
       ↓
Generate human-readable insights
       ↓
Store in Patterns table
       ↓
Trigger notification if new insight found
```

---

## 20. Security & Privacy Deep Dive

### 20.1 Data Storage
- **Location:** Local SQLite database only
- **Encryption:** Android Keystore for sensitive settings
- **Backup:** Excluded from cloud backups by default (user can opt-in)
- **Retention:** User-controlled; default 90 days, then auto-delete old events

### 20.2 Permissions Justification

| Permission | Justification | When Requested |
|------------|---------------|----------------|
| Accessibility Service | Required to detect app launches and show intercepts | Onboarding |
| Overlay | Required to show intercept UI over other apps | Onboarding |
| Notifications | Optional; enables pattern insights notifications | Onboarding (skippable) |
| Usage Stats | Required for passive credit accrual | Onboarding |
| Notification Listener | Required for notification peek feature | When enabling notification-safe apps |
| Calendar | Optional; enables calendar-based context rules | When creating calendar rule |
| Location | Optional; enables location-based context rules | When creating location rule |

### 20.3 Data We NEVER Collect
- Notification content (only timestamps and counts)
- Actual app usage content (e.g., what you viewed in Instagram)
- Location history (only current location when rule evaluates)
- Calendar event details (only presence of event + video/location flags)
- Analytics sent to external servers (unless user explicitly opts in)

### 20.4 User Control
- **Data Export:** One-tap export to JSON for external analysis
- **Data Deletion:** Permanent delete with confirmation (cannot be undone)
- **Selective Logging:** Can disable intent tagging or pattern analysis
- **Audit Log:** View all intercept actions taken in past 30 days

---

## 21. Performance Optimization Strategies

### 21.1 Battery Optimization
- Use WorkManager for non-critical background tasks (pattern analysis)
- Leverage Doze mode exemptions carefully (only for CreditAccrualService)
- Batch database writes (max 1 write per minute)
- Cache context rule evaluations (re-evaluate only on config change)
- Use Foreground Service only when necessary (show persistent notification)

### 21.2 Memory Optimization
- Limit in-memory notification cache to last 20 notifications
- Use Paging3 for insights history (load on demand)
- Lazy load heatmap data (render only visible hours)
- Release overlay resources immediately after dismissal

### 21.3 Intercept Latency Optimization
- Pre-load watched apps list in memory (no DB query on intercept)
- Pre-compute context rule eligibility on config change
- Use Compose for overlay (faster rendering than XML views)
- Target <150ms latency (vs 250ms requirement) for premium feel

---

## 22. Accessibility Requirements

### 22.1 Screen Reader Support
- All buttons have contentDescription labels
- Intercept announces: "Impulse detected for [AppName]. Choose an action."
- Credit balance announced as: "You have [X] minutes of unlock time available"
- Pattern insights read aloud with full context

### 22.2 Visual Accessibility
- Minimum 4.5:1 contrast ratio (WCAG AA)
- Supports system font scaling up to 200%
- No information conveyed by color alone (use icons + text)
- Heatmap has alternative list view for colorblind users

### 22.3 Motor Accessibility
- Minimum 48dp touch targets for all interactive elements
- No gestures required (all actions have button alternatives)
- Adjustable timing for "Scroll once" delay (5-30 seconds)

### 22.4 Cognitive Accessibility
- Clear, simple language (no jargon)
- Consistent button placement across all screens
- Visual progress indicators for focus sessions
- Option to disable complex insights (show simple counts only)

---

## 23. Localization Strategy (Future)

### Phase 1 (Launch)
- English only

### Phase 2 (3 months post-launch)
- Spanish (Español)
- French (Français)
- German (Deutsch)
- Portuguese (Português)

### Phase 3 (6 months post-launch)
- Simplified Chinese (简体中文)
- Japanese (日本語)
- Korean (한국어)
- Hindi (हिन्दी)

**Translation Guidelines:**
- Keep tone empathetic and encouraging (avoid literal translations)
- Localize time formats and date displays
- Adapt idioms (e.g., "crushing it" → culturally appropriate equivalent)
- Test with native speakers before release

---

## 24. Support & Documentation

### 24.1 In-App Help
- Contextual tooltips on first use (dismissible)
- "What's This?" icons on complex features (intent tags, credit decay)
- Video tutorials embedded in settings (optional, Wi-Fi only)

### 24.2 FAQ Coverage
- Why can't I override an intercept immediately?
- How do credits work?
- Why did my app unlock automatically?
- How do I handle emergencies?
- Can I temporarily disable Unscroll?
- How do I add/remove watched apps?
- What happens to my data if I uninstall?

### 24.3 Support Channels
- In-app feedback form (attaches logs if user consents)
- Email: support@unscroll.app
- FAQ website: help.unscroll.app
- Pro users: Priority response within 24 hours

---

## 25. Launch Checklist

### Pre-Launch (4 Weeks Before)
- [ ] Play Store listing prepared (screenshots, video, description)
- [ ] Privacy policy published at unscroll.app/privacy
- [ ] Terms of service published at unscroll.app/terms
- [ ] Beta testing group recruited (50+ users across 5+ device models)
- [ ] Press kit prepared (logo assets, founder bio, demo video)
- [ ] Analytics dashboard set up (local-only, privacy-compliant)

### Beta Testing (2-3 Weeks)
- [ ] 7-day retention measured ≥50%
- [ ] Battery usage validated <3% daily
- [ ] Intercept latency measured <250ms in 95% cases
- [ ] Accessibility tested with screen reader users
- [ ] Edge cases validated (10+ scenarios)
- [ ] User feedback collected and prioritized

### Launch Week
- [ ] Final QA pass on 3+ devices
- [ ] Play Store submission (allow 2-3 days for review)
- [ ] Launch announcement prepared (blog post, social media)
- [ ] Support email inbox monitored 24/7
- [ ] App store optimization (ASO) keywords set
- [ ] Crashlytics / Firebase configured for monitoring

### Post-Launch (First 30 Days)
- [ ] Daily monitoring of crash-free rate (target >99.5%)
- [ ] Weekly user feedback review
- [ ] A/B test credit decay rates
- [ ] Measure success metrics against goals
- [ ] Plan next feature iteration based on data

---

## 26. Risk Mitigation

### 26.1 Technical Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Accessibility permission revoked by user | High | Critical | Detect immediately + show re-enable flow |
| Android OS update breaks AccessibilityService | Medium | High | Monitor Android beta releases; maintain compatibility layer |
| Battery drain exceeds 3% | Low | High | Extensive profiling before launch; add battery usage settings |
| Overlay blocked by malware scanners | Low | Medium | Whitelist with major antivirus vendors |
| Database corruption | Very Low | High | Implement automatic backups; add database validation |

### 26.2 Product Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Users find intercepts annoying and uninstall | Medium | Critical | A/B test intercept frequency; add "sensitivity" setting |
| Free-to-Pro conversion too low (<5%) | Medium | Medium | Add compelling Pro-only features (wellness integration); improve onboarding |
| Notification peek abused to bypass blocking | Low | Medium | Track abuse patterns; add adaptive limits |
| Context rules too complex for users | Medium | Low | Provide templates; hide advanced options by default |

### 26.3 Business Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| App Store rejects for privacy concerns | Low | High | Proactive disclosure; detailed privacy policy; user control emphasis |
| Competitor launches similar feature | Medium | Medium | Speed to market; focus on behavioral insights differentiation |
| Regulatory changes affect accessibility usage | Low | Critical | Monitor GDPR/CCPA updates; maintain compliance flexibility |

---

## 27. Glossary of Terms

| Term | Definition |
|------|------------|
| **Watched App** | An app selected by the user to be controlled by Unscroll |
| **Intercept** | The overlay/shield shown when launching a watched app |
| **Credit** | Time currency earned by staying focused; 1 credit = 1 second of unlock time |
| **Unlock Window** | Time period when all watched apps are freely accessible |
| **Peek Window** | Short 3-minute unlock granted for notification responses |
| **Slip** | Event when user chooses "Scroll once" to bypass an intercept |
| **Intent Tag** | User-selected reason for opening an app (Bored, Notification, etc.) |
| **Pattern Insight** | AI-generated observation about user's scrolling habits |
| **Context Rule** | Condition that auto-bypasses blocking (time, location, calendar) |
| **Credit Decay** | Automatic reduction of old credits to prevent hoarding |
| **Progressive Strictness** | Gradual increase in friction after first week of use |
| **Emergency Bypass** | Temporary override costing future credits |

---

## 28. Appendix: Example User Journeys

### Journey 1: New User - First Day
1. Downloads Unscroll from Play Store
2. Onboarding: Grants accessibility + overlay permissions
3. Selects 5 watched apps (Instagram, TikTok, YouTube, Twitter, Reddit)
4. Sets 2 apps as notification-safe (Instagram, Twitter)
5. Completes 15-minute focus session → Earns 5 minutes of unlock time
6. Tries to open Instagram → Intercept appears
7. Selects "Bored" intent tag → Chooses "Skip"
8. Returns to work, earns passive credits
9. Evening: Spends 10 minutes scrolling Instagram guilt-free
10. Views insights: "You skipped 8 times today! 🎉"

### Journey 2: Power User - Week 3
1. Has 120 minutes of credits (at cap)
2. Gets notification reminder: "Credits expiring soon!"
3. Spends 20 minutes scrolling before dinner
4. Sets calendar context rule: "Auto-unlock during lunch break"
5. Creates time-based rule: "Allow news apps 7-8am"
6. Reviews weekly pattern: "You're strongest 9am-12pm"
7. Adjusts focus ratio to 45:15 (more aggressive)
8. Shares streak (21 days) with accountability buddy
9. Upgrades to Pro for custom themes + wellness integration
10. Exports data to analyze in personal productivity dashboard

### Journey 3: Struggling User - Recovery
1. Slips frequently in first 3 days (frustration building)
2. App shows gentle insight: "You're doing better than 60% of users on Day 3"
3. Enables progressive strictness (gradual difficulty increase)
4. Sets notification-safe apps to reduce friction
5. Discovers peak weakness time: "Most slips happen 8-10pm"
6. Creates evening context rule: "Auto-unlock 8-9pm" (harm reduction)
7. By week 2: Skip rate improves to 65%
8. Adds location rule: "Unlock at gym for workout videos"
9. Week 4: Hits 7-day streak for first time
10. Renews commitment after seeing progress graph

---

## 29. Final Notes for Development Team

### Code Quality Standards
- Minimum 80% unit test coverage for shared KMM modules
- UI tests for critical flows (onboarding, intercept, unlock)
- Lint-free codebase (Detekt for Kotlin, SwiftLint for iOS)
- Documented public APIs and complex algorithms
- Git commit messages follow Conventional Commits spec

### Development Workflow
- Feature branches off `develop`
- Pull requests require 1 approval + CI pass
- Weekly release tags (e.g., `v1.0.1-beta.3`)
- Automated builds on push to `develop`
- Manual Play Store uploads (no CI/CD yet for MVP)

### Communication Protocols
- Daily async standups (Slack/Discord)
- Weekly sync video call (30 min max)
- Bug reports in GitHub Issues (label: `bug`)
- Feature requests in GitHub Discussions
- Emergency hotfixes: Direct to founder + immediate PR

---

**End of PRD v2.0**

This document is a living specification. As development progresses and user feedback arrives, expect iterative updates. Always reference the latest version at: [internal doc link]

**Questions or suggestions?** Contact: [founder email]

**Last Updated:** October 19, 2025  
**Next Review:** Week 4 of development (before Unlock Logic milestone)