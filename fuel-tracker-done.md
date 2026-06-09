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
- Added a real app-language setting with persisted selection, startup locale application, and a dashboard hamburger-menu entry for language choice
- Switched the main activity to `AppCompatActivity` so app-wide locale overrides are applied consistently across the Compose app
- Verified the language-selection checkpoint with successful `assembleDebug` and `testDebugUnitTest`
- Completed the remaining trip-flow localization for trip summary, active trip details, start trip, and end trip so Finnish language selection no longer drops back to English on trip screens
- Localized trip-specific dialogs, metrics, helper text, and ViewModel-owned formatted labels across the trip flow
- Verified the trip-flow localization checkpoint with successful `assembleDebug` and `testDebugUnitTest`

### 2026-04-16

- Polished the public repo surface by rewriting `README.md`, removing an empty dashboard design artifact, and adding targeted orientation comments in non-obvious core logic files
- Refined Finnish fuel-entry wording by replacing `odometri` with `mittarilukema` in the visible app strings
- Verified the polish checkpoint with successful `assembleDebug` and `testDebugUnitTest`

### 2026-04-17

- Audited the newer Compose UI for hardcoded colors and moved the modal add/start/end trip palette into a shared theme-level `FuelTrackModalColors` helper
- Replaced remaining one-off active-state and hero-card colors in Dashboard, Trips, and Trip Summary with theme-driven colors so future dark-theme work is centralized instead of screen-local
- Verified the theme-readiness checkpoint with successful `assembleDebug` and `testDebugUnitTest`

### 2026-04-26

- Added minimal Trip time tracking with persisted trip breaks, manual break correction, trip start/end time editing, domain-level elapsed/break/driving time calculations, average speed including/excluding breaks, and a chronological trip timeline
- Added localized English and Finnish Trip Summary UI for the time split, break actions, break editing, and timeline
- Added Room schema version 3 with a `trip_breaks` table and migration from version 2
- Added trip time summary and break validation unit coverage, and verified the checkpoint with successful `testDebugUnitTest`

### 2026-05-03

- Separated purchase-based trip fuel ratio from actual trip average consumption so linked refuels are no longer presented as fuel used
- Updated Trip Summary hero copy to label purchase-based values explicitly, hide misleading ratios, and explain the limitation with subtle helper text
- Updated trip summary calculation tests and verified the checkpoint with successful `assembleDebug` and `testDebugUnitTest`
- Extracted shared dialog action rows for Save/Cancel, Delete/Cancel, and Save/Delete/Cancel flows into core UI components, then reused them in Trips, Trip Summary, and confirmation dialogs
- Verified the dialog-action refactor with successful `assembleDebug` and `testDebugUnitTest`
- Split the large Trip Summary route file into focused Compose files for route/dialog orchestration, header/actions, hero, time card, timeline, linked refuels, and date-time picker controls
- Verified the Trip Summary split with successful `assembleDebug` and `testDebugUnitTest`
- Extracted Trip Summary dialogs into a dedicated sibling file and removed the unused Trip Summary route callback from navigation
- Verified the cleanup with successful `assembleDebug` and `testDebugUnitTest`
- Added a shared `CompactSectionHeader` core UI component and replaced the local Trip Summary section-header duplicate with it
- Verified the shared compact section header cleanup with successful `assembleDebug` and `testDebugUnitTest`
- Extracted Trip Summary domain-to-UI mapping into an injectable `TripSummaryUiMapper`, leaving the ViewModel focused on state flows and user actions
- Verified the mapper refactor with successful `assembleDebug` and `testDebugUnitTest`
- Localized remaining Car Setup and Edit Fuel Entry screen copy and validation messages through English/Finnish string resources
- Verified the localization cleanup with successful `assembleDebug` and `testDebugUnitTest`
- Added Room migration test infrastructure and instrumentation coverage for database migrations from v1 to v2 and v2 to v3
- Verified migration coverage with successful `compileDebugAndroidTestKotlin`, filtered `connectedDebugAndroidTest`, and the regular `assembleDebug` plus `testDebugUnitTest` checkpoint
- Added a testable string-resource abstraction, bound it to the Android implementation through Hilt, and introduced the first ViewModel unit tests for car setup validation and save events
- Verified the ViewModel test coverage checkpoint with successful `assembleDebug`, `testDebugUnitTest`, and `compileDebugAndroidTestKotlin`
- Added Add Fuel Entry ViewModel tests for missing-car handling, localized validation errors, trip-meter odometer derivation, active-trip attachment, direct odometer mode, and persisted unit conversion
- Reused a shared test string-resource provider for ViewModel tests and verified the checkpoint with successful `assembleDebug`, `testDebugUnitTest`, and `compileDebugAndroidTestKotlin`
- Added remaining ViewModel coverage gaps to the post-review hardening backlog for edit fuel entry, start/end trip, and trip summary break actions
- Split the oversized Dashboard route file into route orchestration, header, cards, and language-dialog files while preserving existing dashboard behavior
- Verified the Dashboard split with successful `assembleDebug`, `testDebugUnitTest`, and `compileDebugAndroidTestKotlin`
- Added Edit Fuel Entry ViewModel tests for loading existing entries, baseline selection, localized validation errors, successful update mutation, dependent trip-meter recalculation, persisted unit conversion, and typed mutation-error UI mapping
- Verified the edit fuel entry ViewModel coverage with successful `assembleDebug`, `testDebugUnitTest`, and `compileDebugAndroidTestKotlin`
- Added edit-screen fuel-entry deletion using the Trip Summary destructive dialog style, routed deletion through `DeleteFuelEntryUseCase`, and covered the recalculation/delete event path in Edit Fuel Entry ViewModel tests
- Verified the edit delete checkpoint with successful `assembleDebug`, `testDebugUnitTest`, and `compileDebugAndroidTestKotlin`
- Added Room DAO instrumentation tests for car, fuel entry, trip, trip break, chronological ordering, active-trip/active-break queries, and trip-to-refuel/trip-break foreign-key behavior
- Added Room-backed repository instrumentation tests for domain mapping, fuel-entry mutation transactions, trip active lookup, and trip-break update/delete behavior
- Updated the test plan status to reflect the current automated coverage and verified the persistence test checkpoint with successful `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, and full `connectedDebugAndroidTest`
- Added Start Trip, End Trip, and Trip Summary ViewModel tests for validation, active-trip handling, derived odometer behavior, break actions, trip time correction errors, delete flow, and save/end events
- Updated the test plan/backlog to close the ViewModel coverage item and verified the checkpoint with successful `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, and full `connectedDebugAndroidTest`
- Added the first Compose interaction instrumentation test for clean app onboarding, car creation, dashboard entry, and opening the add-refuel sheet
- Updated AndroidX test dependencies to `androidx.test:core:1.7.0`, `androidx.test.ext:junit:1.3.0`, and `espresso-core:3.7.0` to avoid the emulator `InputManager.getInstance` Espresso compatibility failure
- Verified the UI/system test checkpoint with successful `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, and full `connectedDebugAndroidTest` with 12 connected tests

### 2026-05-05

- Expanded the Compose interaction instrumentation flow to cover fuel entry save, history navigation, edit, and edit-screen delete confirmation against the real app stack
- Added shared test tags for fuel-entry fields/actions, fuel-history row actions, and destructive dialog confirmation so future UI tests can use stable selectors instead of visible copy
- Updated the UI/system test backlog to close the fuel-entry save/edit/delete item and verified the checkpoint with successful `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, and full `connectedDebugAndroidTest` with 12 connected tests
- Expanded the same Compose interaction flow to cover trip start, active trip summary navigation, end-trip confirmation, ending trip by trip meter distance, and completed trip summary display
- Added shared test tags for dashboard trip start, start/end trip fields, trip summary end action, and end-trip sheet controls
- Updated the UI/system test backlog to close the trip start/end item and verified the checkpoint with successful `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, and full `connectedDebugAndroidTest` with 12 connected tests
- Expanded the Compose interaction flow to cover active-trip break correction by starting a break, ending it, opening the timeline break editor, saving the correction dialog, and deleting the break through confirmation
- Added localization-switching UI coverage through the dashboard language menu and verified that selecting Finnish updates visible dashboard copy
- Updated the UI/system test backlog to close break correction and localization switching, leaving only manual release acceptance, and verified the checkpoint with successful `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, and full `connectedDebugAndroidTest` with 12 connected tests
- Fixed actual-device polish findings by making ViewModel-owned strings follow the selected app locale immediately, refining dark-theme containers/hero gradients away from default purple, tightening the active-break button layout, moving break-edit cancel to the dialog header, and adding trip distance to the hero route line
- Required a nonblank start location when starting a trip so newly created trips do not persist a null start location through the normal UI flow
- Added a backlog item for post-trip refuel linking and verified the checkpoint with successful `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, targeted app-flow instrumentation, and full `connectedDebugAndroidTest` with 12 connected tests
