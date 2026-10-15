# FuelTrack

**A local-first Android app for tracking fuel expenses, consumption and trips for one car.**

Built with Kotlin and Jetpack Compose, FuelTrack focuses on dependable calculations, clear handling of incomplete data and a straightforward mobile experience.

**Status:** Active personal project

## Features

- Record and edit fuel purchases, including odometer or trip-meter readings
- Review fuel history, costs and consumption
- Start and end trips, record refuels during a trip and review trip history
- View a dashboard and choose the app language

<!-- Add 2–3 real app screenshots here when available. Suggested: dashboard, fuel entry, trip view. -->

## Technical highlights

- **Consistent mileage history.** Trip-meter readings are normalized to odometer values, and edits to older entries recalculate dependent readings.
- **Meaningful consumption figures.** Fuel and cost totals are kept separate from consumption estimates, which use completed full-tank segments rather than misleading partial data.
- **Predictable data and calculations.** Money is stored as cents, fuel as milliliters and unit prices as micros; business rules stay outside the UI layer.

## Built with

Kotlin · Jetpack Compose · Navigation Compose · Hilt · Room · Coroutines & Flow

The app separates **UI** (screens and state), **domain** (validation and calculations) and **data** (Room, repositories and mapping) responsibilities.

## Getting started

Open the project in **Android Studio** with the Android SDK and JDK 21 (or a compatible bundled JDK), then run the app on an emulator or Android device.

From the repository root:

```bash
# macOS / Linux
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

```powershell
# Windows
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

## Documentation

- [Product specification](fuel-tracker-product-spec.md)
- [Architecture plan](fuel-tracker-architecture-plan.md)
- [Test plan](fuel-tracker-test-plan.md)
- [Review checklist](fuel-tracker-review-checklist.md)
- [Project presentation](FuelTrack-presentation.md)

## License

[MIT](LICENSE)
