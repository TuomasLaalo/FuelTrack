# FuelTrack

FuelTrack is an Android app for tracking fuel purchases, fuel costs, and trip-specific refuels for a single car.

The project is built as a local-first Android application with emphasis on:

- clean data modeling
- reliable fuel and trip calculations
- explicit handling of uncertain data
- practical mobile UX
- maintainable architecture

## Status

This is an active personal project.

Current implemented areas include:

- car setup
- dashboard
- add fuel entry modal
- edit fuel entry
- fuel history
- trip start and trip end
- active trip workspace
- trip history
- app language selection

## Why This Project Is Interesting

FuelTrack is not just a CRUD app with a few screens. The core engineering work is in:

- supporting both odometer and trip-meter input while storing a normalized odometer history
- separating all-entry totals from reliable full-tank consumption metrics
- protecting historical edits and deletes by recalculating dependent trip-meter-based entries
- keeping calculation logic in domain services instead of UI code
- presenting uncertain values honestly instead of implying false precision

## Stack

- Kotlin
- Jetpack Compose
- Navigation Compose
- Hilt
- Room
- Coroutines and Flow

## Architecture

The app is structured into:

- UI layer for screens, input handling, and state rendering
- Domain layer for validation, calculations, and trip/fuel rules
- Data layer for Room entities, DAOs, repositories, and mappings

Important implementation choices:

- money is stored as cents
- fuel is stored as milliliters
- price per unit is stored as micros
- trip-meter input is normalized into a stored odometer value
- reliable consumption is calculated only from completed full-tank segments

## Repository Layout

```text
FuelTrack/
  app/                             Android application source
  design/                          UI references and design notes
  gradle/                          Gradle wrapper files
  fuel-tracker-product-spec.md
  fuel-tracker-architecture-plan.md
  fuel-tracker-test-plan.md
  fuel-tracker-review-checklist.md
  fuel-tracker-done.md
  FuelTrack-presentation.md
```

## Documentation

Key project documents:

- `fuel-tracker-product-spec.md`
- `fuel-tracker-architecture-plan.md`
- `fuel-tracker-test-plan.md`
- `fuel-tracker-review-checklist.md`
- `FuelTrack-presentation.md`

## Local Development

Requirements:

- Android Studio
- Android SDK
- JDK 21, or the JDK bundled with Android Studio

Build from terminal:

```powershell
cd D:\FuelTank
$env:JAVA_HOME='D:\Ohjelmat\Android\jbr'
$env:GRADLE_USER_HOME='D:\FuelTank\.gradle-user-home'
.\gradlew.bat assembleDebug
```

Run unit tests:

```powershell
cd D:\FuelTank
$env:JAVA_HOME='D:\Ohjelmat\Android\jbr'
$env:GRADLE_USER_HOME='D:\FuelTank\.gradle-user-home'
.\gradlew.bat testDebugUnitTest
```

## License

MIT
