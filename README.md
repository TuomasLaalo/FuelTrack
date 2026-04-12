# FuelTrack

FuelTrack is an Android app for tracking fuel usage, fuel costs, and trip-specific refuels for a single car.

The project is built as a local-first Android application with a focus on:

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
- trip start / trip end
- active trip workspace
- trip history

## Stack

- Kotlin
- Jetpack Compose
- Navigation Compose
- Hilt
- Room
- Coroutines + Flow

## Project Structure

```text
FuelTrack/
├─ app/                          Android application source
├─ design/                       UI references and design notes
├─ gradle/                       Gradle wrapper files
├─ fuel-tracker-planning.md
├─ fuel-tracker-product-spec.md
├─ fuel-tracker-architecture-plan.md
├─ fuel-tracker-mvp-tasks.md
├─ fuel-tracker-test-plan.md
├─ fuel-tracker-review-checklist.md
├─ fuel-tracker-done.md
├─ fueltrack-ui-refinement-plan.md
└─ FuelTrack-presentation.md
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
- JDK from Android Studio or compatible JDK 21

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
