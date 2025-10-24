# ScrollOff – Project Snapshot (v1)

## Overview
ScrollOff interrupts reflexive app opens, awards intentional unlocks, and keeps distraction-heavy apps behind an adaptive overlay. The Android MVP is built with Jetpack Compose, Kotlin, and an accessibility-based intercept service that remains Play Store–compliant.

## Core Loop
- Surface every launchable app and let users toggle protections.  
- Free tier guards up to **5 apps**; categories (Gaming, Entertainment, Social, etc.) influence lock duration (4–6 hours).  
- When a protected app launches, an overlay offers either a timed unlock (1/5/15 minutes) or a hard stop that routes users back to the launcher.  
- Temporary unlocks persist in DataStore and are honoured by the accessibility service so overlays stay quiet until the window expires.  
- Unlock dismissals and minute grants clear the target app and exit back to home to remove temptation.

## State & Persistence
- **DataStore** keeps blocked packages, temporary allowances, and activation locks (lock + grace expiry).  
- Activation locks enforce tier rules while offering a 5‑minute grace period to undo accidental toggles.  
- Landing screen state, permission status, and category-derived metadata are coordinated via `AppViewModel`.

## UI Highlights
- Onboarding landing screen with brand message (“Give yourself more time”) and focus-first copy.  
- Gradient dashboard with snackbar feedback, category headers, and lock-duration chips.  
- Permission card that hides once overlay and accessibility privileges are granted.  
- Overlay redesign featuring earned-minute pills, scheduled unlock reminder, and dismiss-to-home behaviour.

## Plans & Monetisation Hooks
- Free plan limits (5 apps, 4–6 hour locks) enforced in view-model logic with user messaging.  
- Early unlock attempts outside the grace window surface a Pro upsell event (placeholder for billing integration).  
- Assist chips, info cards, and snackbars communicate plan restrictions and upgrade paths.

## Platform Considerations
- Uses Play Store–friendly intent queries (MAIN/LAUNCHER) instead of broad package visibility.  
- Overlay relies on `SYSTEM_ALERT_WINDOW`; intercept logic uses a scoped AccessibilityService with dedicated XML config.  
- Tested behaviour optimised for modern Android (API 26+) with vector icons and adaptive themes.

## Next Steps
- Wire real billing flow for Pro unlocks.  
- Expand category taxonomy and allow user-defined priorities.  
- Add analytics and usage insights per the longer-term PRD roadmap.  
- Bring feature parity to iOS via Screen Time APIs once Android MVP stabilises.
