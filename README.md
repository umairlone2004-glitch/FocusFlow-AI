# FocusFlow AI

An offline-first, AI-branded productivity and study manager for Android, built with
Kotlin, Jetpack Compose, Material 3, Room, Hilt and modern Android architecture.

FocusFlow AI helps you plan tasks, group them into projects, protect your attention with
a Pomodoro-style focus timer, and see your progress through analytics — all without an
internet connection.

---

## Highlights

- **Onboarding & profile** — first-run setup for name, daily focus goal and theme, plus a
  dedicated profile editor with avatar colour.
- **Dashboard** — greeting, daily focus goal ring, streaks, today's tasks, overdue items,
  upcoming deadlines and project progress, with quick actions.
- **Task management** — create, edit, complete, delete; priority levels, due dates and
  times, categories, tags, recurring tasks, search, filtering, sorting and overdue
  detection. Everything persists across restarts.
- **Projects** — group tasks, track completion percentage and view project-scoped tasks.
- **Focus timer** — real 25/5 Pomodoro flow with custom durations, start/pause/reset,
  session history, daily focus statistics and completion notifications (including a
  background alarm so you are notified if the app is not in the foreground).
- **Calendar** — month grid with task/event indicators, day navigation and event creation.
- **Analytics** — tasks completed/created, focus time, completion rate, streaks and
  daily/weekly/monthly bar charts.
- **Global search** — one query across tasks, projects, notes and events.
- **Notes** — create, edit, delete, pin and search notes.
- **Settings** — theme (light/dark/system), notification toggle, timer defaults, default
  priority, data reset and an about screen.
- **Offline-first** — all data lives in a local Room database; no network is required.

---

## Architecture

The project follows a pragmatic clean-architecture layering with a single Gradle module.

```
com.focusflow.ai
├── data
│   ├── local        Room database, entities, DAOs, type converters
│   ├── mapper       Entity ⇄ domain model mappers
│   └── repository   Repository implementations (Flow-based)
├── domain
│   ├── model        Immutable domain models and enums
│   ├── repository   Repository interfaces (abstractions)
│   ├── analytics    Pure analytics + statistics calculator
│   ├── filter       Pure task search/filter/sort logic
│   ├── focus        Deterministic focus-timer state machine
│   └── recurrence   Recurring-task date computation
├── di               Hilt modules
├── notifications    Channels, alarm scheduler and receivers
├── ui
│   ├── theme        Material 3 colour, typography and theme
│   ├── components   Reusable Compose components
│   ├── navigation   Navigation-Compose graph and bottom bar
│   ├── screens      One composable screen per feature
│   └── viewmodel    Hilt ViewModels exposing StateFlow
└── util             Date/formatting helpers
```

**Data flow.** Screens observe `StateFlow` from Hilt `ViewModel`s, which read from
repository interfaces. Repository implementations map Room entities to domain models and
expose `Flow`s, so the UI updates reactively as data changes. Business logic that can be
pure — analytics, filtering, recurrence, the timer — is kept free of Android and
framework types so it can be tested directly.

**Dependency injection.** Hilt provides the database, DAOs, repositories and ViewModels.

---

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.0.20 |
| UI | Jetpack Compose (BOM 2024.09.02), Material 3 |
| Architecture | MVVM + repositories + clean domain layer |
| Persistence | Room 2.6.1 |
| DI | Hilt 2.52 (KSP) |
| Async | Coroutines & Flow 1.9.0 |
| Navigation | Navigation-Compose 2.8.0 |
| Build | AGP 8.5.2, Gradle 8.7, JDK 17 |
| SDK | compileSdk 34, targetSdk 34, minSdk 26 |

---

## Building

Requirements: JDK 17 and the Android SDK (compileSdk 34).

```bash
# Unit tests
./gradlew testDebugUnitTest

# Static analysis
./gradlew lintDebug

# Debug APK  ->  app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleDebug

# Instrumented tests (requires a connected device or emulator)
./gradlew connectedDebugAndroidTest
```

---

## Testing

- **Unit tests** (`app/src/test`) cover analytics calculations, streak logic, the focus
  timer state machine, recurrence rules, task search/filter/sort, the app-startup
  onboarding state machine, and a task-list ViewModel against an in-memory fake repository.
- **Database/repository tests** run under Robolectric against an in-memory Room database to
  verify persistence, tag conversion, completion timestamps and recurring-task generation.
- **Instrumented tests** (`app/src/androidTest`) drive the onboarding and task-creation
  flows on a device/emulator using Compose UI testing and Hilt.

---

## Continuous integration

`.github/workflows/android-ci.yml` runs on every push and pull request to `main` (and can
be triggered manually):

1. Checkout, JDK 17 setup and Gradle setup with dependency caching.
2. Gradle wrapper validation.
3. `lintDebug`, `testDebugUnitTest` and `assembleDebug`.
4. The debug APK is uploaded as the **FocusFlow-debug-apk** artifact, and lint/unit-test
   reports as **FocusFlow-reports**.
5. A second job, **Instrumented tests (manual)**, boots an Android emulator and runs
   `connectedDebugAndroidTest`, uploading the instrumented-test reports. It is triggered
   only by a manual `workflow_dispatch` run (Actions → Run workflow), because a hosted
   emulator is slow and flaky and should not gate ordinary pushes; the build and unit-test
   job above is the required check.

---

## Known limitations

- **Emulator tests are not green in hosted CI.** The instrumented tests in
  `app/src/androidTest` are real and runnable, but on a headless, software-rendered
  GitHub-hosted AVD the app never left its splash screen within the test timeout, and no
  crash or app log was produced. The same critical flows are covered on the JVM by
  `OnboardingFlowUiTest` and `TaskListUiTest`, which run in the unit-test job and pass.
- Reminder alarms are re-armed whenever the dashboard is opened. They are not restored by
  a boot-completed receiver, so a device reboot clears pending reminders until next launch.
- Project renaming is implemented in `ProjectsViewModel` but is not yet exposed in the
  projects UI.
- There is no calendar week view, and notes have no folder/tag organisation.
- Error states are minimal and there has been no dedicated accessibility audit.

---

## License

Released for educational and personal use.
