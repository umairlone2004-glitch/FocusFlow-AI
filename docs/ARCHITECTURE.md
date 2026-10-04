# FocusFlow AI — Architecture Notes

## Layers

### Domain (`com.focusflow.ai.domain`)
Framework-free. Contains immutable models (`Task`, `Project`, `Note`, `FocusSession`,
`CalendarEvent`, `UserProfile`) and the enums `Priority`, `RecurrenceType`, `ThemeMode`.
Also holds pure logic:

- `AnalyticsCalculator` — completion rate, streaks, focus-minute aggregation and
  daily/weekly/monthly series. Takes an explicit `ZoneId` for deterministic tests.
- `TaskFiltering` — text search, priority/category filters and sorting.
- `Recurrence` — next due-date computation, including advancing overdue recurring tasks.
- `FocusTimer` — a deterministic countdown state machine.

Repository interfaces live here too, so the data layer depends on the domain, not the
reverse.

### Data (`com.focusflow.ai.data`)
Room entities, DAOs, converters and mappers, plus repository implementations. Entities are
kept separate from domain models; mappers translate between them. Repository methods
return `Flow`s for reactive reads and are `suspend` for writes.

### DI (`com.focusflow.ai.di`)
A single `AppModule` provides the Room database, DAOs and repository bindings.

### UI (`com.focusflow.ai.ui`)
Compose screens observe `StateFlow` from Hilt ViewModels. `Navigation.kt` defines the route
table, a bottom navigation bar for the five top-level destinations, and a `NavHost` for
detail/edit destinations. The bottom bar is hidden on non-top-level routes.

### Notifications (`com.focusflow.ai.notifications`)
`NotificationHelper` creates channels and posts notifications (guarded by the
`POST_NOTIFICATIONS` runtime permission on API 33+). `ReminderScheduler` uses
`AlarmManager`, falling back to an inexact alarm when exact alarms are unavailable.
`ReminderReceiver` and `FocusAlarmReceiver` deliver the alarms.

## Persistence

A single Room database (`focusflow.db`) with tables for tasks, projects, notes, focus
sessions, events and a single-row profile. Tags are stored as a unit-separator-joined
string via a `TypeConverter`. The database uses `fallbackToDestructiveMigration` at
version 1.

## Reactive data flow

```
Room DAO (Flow) ──▶ RepositoryImpl (maps to domain) ──▶ ViewModel (combine/map)
        ──▶ StateFlow ──▶ Composable (collectAsStateWithLifecycle)
```

`stateIn(..., SharingStarted.WhileSubscribed(5_000), initial)` is used so flows are only
active while the UI is subscribed, while surviving configuration changes.

## Testing strategy

Pure logic is tested directly with JUnit. Room and repository behaviour is tested under
Robolectric against an in-memory database. ViewModels are tested against in-memory fakes
with a test dispatcher. Critical user flows are covered by Compose instrumented tests.
