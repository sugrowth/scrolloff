# Unscroll MVP v2.0

Android-first implementation of the Unscroll "Break the Reflex Loop" MVP described in `project.md`. The app demonstrates the interception, credit reward, focus, and insight flows defined in the PRD and is structured so the core logic can later be shared with iOS via Kotlin Multiplatform.

## Project structure

- `app/` – Jetpack Compose application layer (navigation shell, onboarding, dashboard, intercept sheet, and view model).
- `shared/` – Kotlin Multiplatform shared logic (domain models, in-memory repositories, use cases, dependency container, and common tests).
- `project.md` – Product Requirements Document provided by the founder.

The `shared` module currently targets Android only but is organised so additional targets (iOS, desktop) can be added without restructuring.

## Getting started

1. Install Android Studio Ladybug or newer with the Android Gradle Plugin 8.5+ toolchain and JDK 17.
2. From the project root run `gradle wrapper` (or create one via Android Studio) to generate the Gradle wrapper files; they are not bundled in this repo snapshot.
3. Open the project in Android Studio and sync Gradle. The default launch configuration builds `app`.
4. Run on an Android device/emulator (API 26+) to interact with the onboarding, dashboard, credits, and intercept simulation flows.

## Core flows implemented

- **Watched apps selection** – Onboarding card list of common distraction apps with toggling backed by the shared repository.
- **Dashboard snapshot** – Aggregated view of credits, recent intercepts, and insights exposed through `ObserveDashboardStateUseCase`.
- **Credits engine** – Earn/spend/decay plumbing via `UpdateCreditsUseCase`, including unit tests.
- **Focus sessions** – `RegisterFocusSessionUseCase` stores sessions and rewards credits.
- **Intercept decisioning** – `EvaluateInterceptDecisionUseCase` produces contextual recommendations powering the intercept sheet.
- **Intercept logging** – `RecordInterceptOutcomeUseCase` records outcomes and spends credits when needed.

Repository implementations are in-memory for the MVP so that product flows can be exercised without storage dependencies. Interfaces are in place to swap in DataStore/Room implementations later.

## Testing

- Unit tests live in `shared/src/commonTest`. Run with `./gradlew shared:allTests` once the Gradle wrapper is generated.
- Additional UI tests are not yet implemented; hooks are present for future Compose testing.

## Known gaps vs PRD

- Accessibility, notifications, context rules, and analytics use stubs (no runtime integration yet).
- Credit decay runs when triggered manually; background scheduling is not wired.
- No device admin/accessibility services – current build is a functional prototype without OS-level enforcement.
- Insights are not generated automatically; repositories expose hooks for later ML/analytics layers.
- No persistence beyond app lifetime because repositories are in-memory.
- iOS target is not configured yet; the shared module is structured to support it once required.

## Next steps

1. Implement persistent repositories (DataStore/Room) and background tasks for decay/scheduling.
2. Integrate Android AccessibilityService & notification listeners to intercept launches and notification peeks.
3. Add detailed instrumentation/UI tests for onboarding, intercept, and focus flows.
4. Expand shared module targets (iOS) and extract platform-neutral services as needed.
5. Enhance the UI with timelines, streak charts, and pattern insights called for in the PRD.
