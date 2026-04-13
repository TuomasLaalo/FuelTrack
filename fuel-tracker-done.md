# Fuel Tracker App Done Log

Use this file to track completed work. Move items here from the backlog when they are actually finished.

## Rules

- Record only completed work
- Include the completion date
- Add a short note about what was done
- Mention tests run when relevant
- If work was partially done, keep the remaining part in the backlog

## Completed Items

### 2026-03-17

- Created initial planning file and gathered product requirements
- Created product specification
- Created technical architecture plan
- Created MVP backlog
- Created governance files for done tracking, testing, and review
- Scaffolded the Android project in `D:\FuelTank` with initial Compose app shell, reusable UI primitives, and Gradle wrapper generation
- Implemented the first real persistence slice: Room database, domain models, repositories, Hilt wiring, and onboarding car setup flow gated by primary-car existence
- Implemented the first fuel-entry slice: persisted add-entry flow, normalized odometer handling for odometer/trip-meter input, and dashboard wired to real stored fuel history and totals
- Implemented a real fuel history screen plus delete flow, backed by Room and reachable from the dashboard
- Implemented the first reliable full-tank calculation engine, wired dashboard stats to it, and added initial unit tests for segment-building rules

### 2026-03-18

- Hardened fuel-entry CRUD by adding edit support, reusable add/edit fuel-entry form components, and explicit historical odometer recalculation rules for trip-meter-dependent entry chains
- Removed the temporary latest-entry-only restriction by implementing transactional mutation planning for historical edit/delete operations and adding unit tests for mutation planner and CRUD use cases
- Expanded test coverage around calculation and mutation behavior, including reliable-segment rules, historical odometer recalculation, and fuel-entry update/delete paths
- Implemented trip tracking end to end: active-trip persistence, start-trip flow, end-trip flow, active-trip linking for fuel entries, trip list, and trip summary based on linked refuels
- Added trip-focused unit tests for trip summary semantics and trip start/end use cases
- Polished the main UI flows, especially dashboard and trip screens, with stronger information hierarchy, reusable info cards, clearer active-trip presentation, and improved summary/readability states
- Verified current app state with successful `assembleDebug` and `testDebugUnitTest`

### 2026-04-13

- Added a localization implementation plan for the app and completed the first localization slice for Dashboard and Add Fuel Entry
- Moved visible Dashboard and Add Fuel Entry text into `strings.xml` and `values-fi/strings.xml`, including Compose labels, dialog copy, and Add Fuel Entry validation messages
- Added a small `StringResourceProvider` to localize ViewModel-owned fallback and formatted text cleanly without pushing resource lookups into ad hoc code paths
- Fixed the stale `TripUseCasesTest` fake repository so the unit test suite matches the current `TripRepository` interface
- Verified the checkpoint with successful `assembleDebug` and `testDebugUnitTest`
- Completed the second localization slice for Fuel History and Trips, including headers, cards, menus, dialogs, empty states, and ViewModel-owned date/status formatting
- Cleaned older euro/date formatting in Fuel History so localized screens no longer depend on broken encoded symbols in UI output
- Verified the second localization checkpoint with successful `assembleDebug` and `testDebugUnitTest`
- Replaced raw fuel-entry mutation exception strings with typed domain errors and mapped them to localized UI messages in edit/delete flows
- Localized the remaining Edit Fuel Entry validation messages so add/edit form behavior now uses the same resource-backed error text path
- Added a unit assertion for typed odometer-backwards mutation errors and re-verified the checkpoint with successful `assembleDebug` and `testDebugUnitTest`
