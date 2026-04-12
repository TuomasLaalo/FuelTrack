# Fuel Tracker App MVP Task Backlog

This file breaks the project into concrete implementation tasks for version 1.

## 1. Foundation

### 1.1 Create Android Project

- Create a new Android app project in Kotlin
- Use Jetpack Compose
- Set minimum SDK version
- Configure package name
- Verify the app runs on emulator or device

### 1.2 Add Core Dependencies

- Add Navigation Compose
- Add Room
- Add Hilt
- Add Coroutines
- Add Flow support where needed
- Add testing dependencies

### 1.3 Configure Project Standards

- Enable Room `exportSchema = true`
- Set initial database version
- Create migration strategy placeholder
- Set up dark theme
- Add English and Finnish string resource folders

## 2. App Shell

### 2.1 Create Base App Structure

- Create package structure for `app`, `core`, `data`, `domain`, and features
- Set up Hilt application class
- Set up navigation host
- Add basic app theme and scaffold

### 2.2 Create Shared UI Foundation

- Build reusable screen scaffold
- Build reusable top app bar
- Build reusable form field components
- Build reusable confirmation dialog component
- Define spacing, typography, and color tokens

## 3. Domain Model And Persistence

### 3.1 Define Core Domain Models

- Create `Car`
- Create `FuelEntry`
- Create `Trip`
- Create `FuelStats`
- Create `TripSummary`
- Create `ConsumptionSegment`
- Create `CalculationConfidence`

### 3.2 Decide Persisted Numeric Units

- Store tank size in milliliters
- Store fuel entry liters in milliliters
- Store total cost in cents
- Store price per liter in micros or use a safe converter
- Store odometer in whole kilometers for v1 unless finer precision is required

### 3.3 Create Room Entities

- Create `CarEntity`
- Create `FuelEntryEntity`
- Create `TripEntity`
- Add indexes
- Add type converters if needed

### 3.4 Create DAOs

- Car DAO
- Fuel entry DAO
- Trip DAO

### 3.5 Create Repositories

- Car repository
- Fuel entry repository
- Trip repository
- Mapping between entities and domain models

## 4. Car Setup

### 4.1 Create Onboarding Flow

- Build create car screen
- Add validation for name, tank size, and initial odometer
- Save first car
- Route to home screen after success

### 4.2 Derived Current Odometer Logic

- Store `initialOdometerKm` on the car
- Define rule for deriving current odometer from newest available trip or fuel-entry data
- Expose derived current odometer from domain layer

## 5. Fuel Entry Feature

### 5.1 Build Add Fuel Entry Screen

- Add date input
- Add input mode switch for odometer or trip meter
- Add odometer input
- Add trip meter input
- Add liters input
- Add total cost input
- Add price per liter input
- Add full tank checkbox
- Add note field
- Add optional attach-to-trip behavior

### 5.2 Add Input Rules

- Support auto-calculation of price per liter from total cost and liters
- Support auto-calculation of total cost from price per liter and liters
- Validate non-negative and required values
- Derive normalized odometer when trip meter mode is used
- Disable trip meter mode if there is no previous known odometer

### 5.3 Save Fuel Entry

- Create use case for adding entry
- Persist entry
- Persist normalized odometer used by calculations
- Trigger recalculation of affected derived values

### 5.4 Edit And Delete Fuel Entry

- Build edit entry screen or reuse add-entry screen
- Add delete confirmation flow
- Ensure edits and deletions trigger recalculation of affected statistics and trip summaries
- Ensure edits and deletions also recalculate affected normalized odometer values when dependent trip-meter entries exist

### 5.5 Fuel History

- Build chronological list screen
- Show key values on each row
- Open details or edit actions
- Handle empty state

## 6. Calculation Engine

### 6.1 Create Calculation Services

- Create `ConsumptionSegmentBuilder`
- Create `FuelCalculationService`
- Create `TripSummaryService`

### 6.2 Define Reliable Segment Rules In Code

- `isFullTank = true` means practically full
- Reliable segment requires two full-tank anchors
- Starting anchor fuel is excluded
- Ending anchor fuel is included
- Partial fills between anchors are included

### 6.3 Define Calculation Scope Rules

- All entries count toward total cost
- All entries count toward total liters bought
- All entries appear in history
- Only completed reliable segments count toward average consumption
- Only completed reliable segments count toward reliable liters used
- Only completed reliable segments count toward reliable distance
- Only completed reliable segments count toward reliable cost per kilometer

### 6.4 Add Confidence Model

- Return `HIGH_CONFIDENCE`, `LOW_CONFIDENCE`, or `NOT_AVAILABLE`
- Use confidence to control what the UI displays
- Avoid showing misleading precision when data is incomplete

### 6.5 Add Unit Tests For Core Calculation Logic

- Full-tank to full-tank segment
- Partial fill between anchors
- Missing ending anchor
- Missing starting anchor
- Multiple partial fills
- Editing old entry changes later segment output
- Deleting anchor entry removes reliable segment
- Trip-meter-based entry derives correct odometer
- Editing previous entry updates dependent derived odometer chain

## 7. Home And Stats

### 7.1 Build Home Screen

- Show car summary
- Show derived current odometer
- Show recent fuel entries
- Show active trip if present
- Show key cost and consumption metrics

### 7.2 Build Stats Presentation

- Show total money spent
- Show total liters bought
- Show reliable average consumption
- Show reliable cost per kilometer
- Show average fuel price
- Show confidence or explanation text where needed

## 8. Trip Feature

### 8.1 Start Trip

- Build start trip screen
- Add optional title
- Add optional start location
- Capture starting odometer
- Enforce only one active trip

### 8.2 End Trip

- Build end trip flow
- Add optional end location
- Capture ending odometer
- Mark trip completed

### 8.3 Link Fuel Entries To Active Trip

- Allow adding fuel entries while trip is active
- Link those entries to the active trip
- Ensure linked entries still appear in fuel history

### 8.4 Build Trip Summary

- Show trip distance
- Show linked fuel bought
- Show linked fuel cost
- Show average consumption only when reliable data exists
- Show fallback message when reliable consumption is unavailable

### 8.5 Add Trip Tests

- Trip with no fuel entries
- Trip with one linked fuel entry
- Trip with multiple linked fuel entries
- Trip with insufficient full-tank data
- Trip summary recalculates after fuel-entry edit or delete

## 9. Settings And Localization

### 9.1 Localization

- Add English strings
- Add Finnish strings
- Move all user-visible text into resources

### 9.2 Settings

- Add language selection
- Add placeholder for future settings if needed

## 10. UI Polish

### 10.1 Dark Theme

- Implement dark-first theme
- Keep style simple and minimal
- Aim for calm Google Clock-like feel

### 10.2 UX Polish

- Add empty states
- Add loading states
- Add error messages
- Improve form usability
- Review navigation flow

## 11. Database Evolution

### 11.1 Migration Readiness

- Keep Room schema versioned
- Export schema files
- Add migration path when schema changes

### 11.2 Future-Proofing

- Keep `carId` in all relevant models
- Keep trip location optional
- Keep calculation services separate from persistence and UI

## 12. Suggested Build Order

Recommended implementation sequence:

1. Foundation
2. App shell
3. Domain model and Room schema
4. Car setup
5. Fuel entry CRUD
6. Calculation engine and tests
7. Home and stats
8. Trip feature
9. Localization and settings
10. UI polish

## 13. Definition Of Done For MVP

The MVP is done when the user can:

- create one car
- add, edit, and delete fuel entries
- view fuel history
- see total cost and fuel totals
- see reliable consumption metrics based on full-tank segments
- start and end a trip
- view trip summary based on linked entries
- use the app in English or Finnish
- use the app fully offline

## 14. Recommended First Coding Sprint

The first sprint should focus on:

- project creation
- app shell
- Room schema
- domain models
- onboarding car setup
- add fuel entry flow

Do not delay the persisted-unit decisions or calculation-engine design too long. Those are core structural choices.

Do not treat trip meter as a separate calculation model. It is an input mode layered on top of normalized odometer storage.
