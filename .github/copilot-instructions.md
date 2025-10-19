<!-- Short, actionable instructions for AI coding agents working on Unscroll -->
# Copilot instructions — Unscroll (Android-first, KMM-ready)

Target: Help maintainers and AI agents make small, safe, high-impact changes. Use the project's PRD (`project.md`) as the authoritative source.

- Big picture: this repo is an Android-first mobile app with a planned KMM shared core. Key runtime pieces (described in `project.md`) are: AccessibilityService (intercept), NotificationListenerService (peeks), CreditAccrual (foreground service), PatternAnalyzer (WorkManager). When making changes, map work to one of: presentation (Compose UI), domain (use-cases), data (Room + services), or shared KMM modules.

- Build & tests: there are no build files in this workspace. Assume a standard Android/Kotlin Gradle project layout when creating files (module `app/`, `shared/` for KMM). Use Gradle wrapper (`./gradlew`) and Android Studio for builds unless the repo later adds different tooling. Add CI config only when a reproducible local build succeeds.

- Conventions evident in PRD:
  - MVVM + Clean architecture split: `presentation/`, `domain/`, `data/`.
  - Services live under `data/services/` (e.g. `InterceptAccessibilityService.kt`).
  - Shared models live under `shared/` (KMM): `RewardEngine`, `StateMachine`, `PatternAnalyzer`.
  - Prefers Jetpack Compose for UI and WorkManager for scheduled background jobs.

- Data & privacy: the app is local-first (Room DB), no external telemetry by default. Don't add network calls or telemetry without explicit user opt-in and an updated `project.md` privacy section.

- Small change guidance (what to do first):
  1. Read `project.md` for design assumptions (every PR should reference relevant US# or section).
  2. When adding files follow the directory layout in section 18.1 (presentation/domain/data/shared).
  3. For intercept logic, prefer `InterceptAccessibilityService` patterns: pre-load watched apps in memory, evaluate context rules before showing overlay, respect `notificationSafe` flag.
  4. For credit logic, store ledger entries with `createdTs` and `expiresTs` (48h expiry) and enforce `creditCapMinutes` in `Settings`.

- Examples to reference in edits:
  - Use the `Data Models` snippet in `project.md` for entity shapes (`WatchedApp`, `CreditLedger`, `UnlockWindow`, `ImpulseEvent`).
  - Use the `Intercept Logic` pseudocode in `project.md` when implementing overlay behavior or AccessibilityService handlers.

- Tests & verification: unit-test shared KMM modules first. For Android-only behavior (Accessibility, NotificationListener), write instrumentation tests where possible and add small local harnesses that simulate events. Do not push CI changes until a reproducible local Gradle build passes.

- When unsure, ask these targeted questions in PR descriptions:
  1. Which PRD section or user story (e.g., US-2 Intercept reflex opens) does this change implement or modify?
  2. Does this touch user privacy (notifications, calendar, location)? If yes, show consent/opt-in text and update `project.md`.
  3. Does this alter credit calculations, caps, or decay? Include a short rationale and test vectors.

- Forbidden/flagged changes for automated agents:
  - Do not introduce network telemetry or third-party SDKs that exfiltrate user data without explicit PRD updates.
  - Do not change permissions (Accessibility, Overlay, Notification) without updating onboarding flows in `project.md` and adding tests.

Keep changes minimal and reference `project.md` section numbers in every commit message.

If you need more repository context (source files, Gradle config, CI), request access or a project scaffold and I'll adapt these instructions into the code layout.
