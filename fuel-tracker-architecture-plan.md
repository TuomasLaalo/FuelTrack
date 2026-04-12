# Fuel Tracker App Technical Architecture Plan

## 1. Recommended Stack

- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM with a clear domain layer
- Local storage: Room
- Navigation: Navigation Compose
- Dependency injection: Hilt
- Async work: Kotlin Coroutines + Flow
- Charts: defer until later unless a lightweight chart library becomes necessary

This stack fits a local-first Android app and keeps version 1 maintainable.

## 2. High-Level Architecture

Recommended layers:

- UI layer
- Domain layer
- Data layer

### 2.1 UI Layer

Responsible for:

- screens
- state rendering
- user input
- navigation
- localization

Use:

- Compose screens
- ViewModels per feature
- immutable UI state models

### 2.2 Domain Layer

Responsible for:

- business rules
- validation
- fuel calculations
- trip calculations
- confidence and reliability decisions

Use cases should orchestrate work such as:

- create car
- add fuel entry
- edit fuel entry
- delete fuel entry
- start trip
- end trip
- load fuel statistics
- load trip summary

The core calculation logic should not live only inside generic use cases. It should be implemented in explicit calculation services.

Recommended domain services:

- `FuelCalculationService`
- `TripSummaryService`
- `ConsumptionSegmentBuilder`

Recommended execution flow:

- use case requests data from repository
- calculation service computes derived values
- ViewModel maps domain result into UI state

### 2.3 Data Layer

Responsible for:

- Room entities
- DAOs
- repositories
- mapping between storage and domain models
- type converters where needed

## 3. Suggested Package Structure

```text
com.example.fueltracker
|-- app
|-- core
|-- data
|-- domain
|-- feature_car
|-- feature_fuel
|-- feature_trip
|-- feature_settings
`-- ui
```

Possible internal structure:

```text
data/
  local/
  repository/
domain/
  model/
  repository/
  service/
  usecase/
core/
  util/
  localization/
```

## 4. Data Model

The data model should prefer stable persisted units over floating-point storage. The app is calculation-heavy, so avoiding rounding drift early will reduce later refactoring.

### 4.1 Car

Fields:

- `id: Long`
- `name: String`
- `tankSizeMilliliters: Long`
- `initialOdometerKm: Long`
- `createdAtEpochMillis: Long`
- `updatedAtEpochMillis: Long`

Notes:

- `initialOdometerKm` is the onboarding baseline value.
- The current odometer shown in the app should be derived from the newest reliable trip or fuel-entry data whenever possible.
- Keep `carId` everywhere even though version 1 supports only one car. This keeps the schema ready for multi-car expansion later.

### 4.2 FuelEntry

Fields:

- `id: Long`
- `carId: Long`
- `tripId: Long?`
- `dateTimeEpochMillis: Long`
- `distanceInputMode: DistanceInputMode`
- `odometerKm: Long`
- `tripMeterKm: Long?`
- `litersMilliliters: Long`
- `totalCostCents: Long`
- `pricePerLiterMicros: Long` or `BigDecimal` via converter
- `isFullTank: Boolean`
- `note: String?`
- `createdAtEpochMillis: Long`
- `updatedAtEpochMillis: Long`

Notes:

- `odometerKm` is the normalized stored value used by calculations.
- `tripMeterKm` supports UX-friendly entry but should not replace normalized odometer storage.
- `isFullTank = true` means the user estimates the tank is practically full after the refuel.
- This is a user judgment, not a claim of perfect physical measurement.

### 4.3 Trip

Fields:

- `id: Long`
- `carId: Long`
- `title: String?`
- `startLocation: String?`
- `endLocation: String?`
- `startOdometerKm: Long`
- `endOdometerKm: Long?`
- `startTimeEpochMillis: Long`
- `endTimeEpochMillis: Long?`
- `status: TripStatus`
- `createdAtEpochMillis: Long`
- `updatedAtEpochMillis: Long`

Notes:

- Location fields should be optional because the user may want to track a trip without precise location labels.

### 4.4 Derived Models

These do not need to be stored directly in version 1:

- `FuelStats`
- `TripSummary`
- `ConsumptionSegment`

Additional helpful domain models:

- `CalculationConfidence`
- `CurrentOdometerSnapshot`
- `DistanceInputMode`

Suggested confidence enum:

- `HIGH_CONFIDENCE`
- `LOW_CONFIDENCE`
- `NOT_AVAILABLE`

Suggested distance input enum:

- `ODOMETER`
- `TRIP_METER`

## 5. Database Design

Room tables:

- `cars`
- `fuel_entries`
- `trips`

Relationships:

- one car to many fuel entries
- one car to many trips
- one trip to many fuel entries

Indexes to add:

- `fuel_entries(carId, dateTimeEpochMillis)`
- `fuel_entries(tripId)`
- `trips(carId, startTimeEpochMillis)`

Schema discipline from the beginning:

- versioned Room schema
- explicit migration strategy
- `exportSchema = true`

## 6. Calculation Strategy

This is the most important part of the app. The product should prefer practical usefulness, consistent rules, and clearly communicated estimates over false precision.

### 6.1 Practical Reliability

The app should not claim perfect measurement accuracy. It should instead calculate values that are practically reliable for normal user tracking.

`isFullTank = true` means the user considers the tank practically full after the refuel. This may be based on:

- the pump stopping automatically
- the car gauge showing full
- the user's own observation that the tank is effectively full even if a small amount could theoretically still fit

### 6.2 Reliable Calculation Segment

A reliable calculation segment is formed between two practically full anchor points.

Example:

- entry A is marked full tank
- entry B is a partial fill
- entry C is marked full tank

Then:

- `distance = odometer(C) - odometer(A)`
- `fuelUsed = liters(B) + liters(C)`
- `cost = cost(B) + cost(C)`

Rules:

- the starting anchor refuel itself is not counted into the segment's fuel usage
- the ending anchor refuel is counted into the segment
- all refuels after the starting anchor and up to and including the ending anchor are included

This produces a completed reliable segment.

### 6.3 Calculation Scope Rules

All fuel entries should always count toward:

- total cost
- total liters bought
- fuel history
- trip refuel totals

Only completed reliable segments should count toward:

- average consumption
- reliable cost per kilometer
- reliable liters used
- reliable distance

This distinction should be explicit in both domain logic and UI wording.

### 6.4 Partial Fill Handling

Partial fills should:

- appear in history normally
- count toward money spent
- count toward liters bought
- be included in reliable segment calculations when bounded by full-tank anchors

Partial fills should not automatically produce standalone consumption values if reliability is low.

### 6.5 Tank State

Version 1 should keep tank-state logic conservative.

Recommended rules:

- if an entry is marked full tank, the system can assume the tank is practically full after that event
- if later entries are partial or otherwise incomplete, the app should avoid claiming exact remaining-fuel values unless supported by completed reliable segments

### 6.6 Odometer Normalization

Fuel-entry calculations should always use normalized stored odometer values.

Recommended rules:

- if the user enters direct odometer, store that value as `odometerKm`
- if the user enters trip meter distance, derive `odometerKm` from the previous known odometer plus `tripMeterKm`
- trip meter is an input convenience, not the primary historical measurement field
- if trip-meter-based historical entries are edited or deleted, later derived odometer values may need recalculation
- if there is no previous known odometer, trip meter mode must not be allowed for that entry

### 6.7 Cost Per Kilometer

Two scopes should be kept conceptually separate:

- total purchase cost
- reliable cost per kilometer

For MVP:

- total purchase cost can include all entries
- reliable cost per kilometer should be calculated only from completed reliable segments

### 6.8 Trip Summary

Trip summary in version 1 should be based primarily on actual fuel entries linked to the trip.

Recommended MVP rules:

- `tripDistance = endOdometer - startOdometer`
- `tripFuelCost = sum of fuel entries linked to the trip`
- `tripFuelBought = sum of liters linked to the trip`
- `tripAverageConsumption` is shown only when enough reliable data exists

If reliable trip consumption cannot be calculated, show a clear message such as:

- `Not enough full-tank data to calculate reliable consumption`

The architecture should still allow a later version to estimate fuel used from tank state and later full-tank reconciliation, but that should not be the v1 default behavior.

## 7. Screens And ViewModels

### 7.1 Onboarding / Car Setup

Screen:

- create car

ViewModel responsibilities:

- validate form
- save first car
- route to home

### 7.2 Home

Screen content:

- current car summary
- derived current odometer
- recent fuel entries
- active trip card if present
- key stats

ViewModel responsibilities:

- load dashboard data
- combine fuel history, stats, current odometer, and active trip

### 7.3 Add/Edit Fuel Entry

Screen content:

- form fields
- input mode switch for odometer or trip meter
- automatic total/price-per-liter calculation support
- optional attach to active trip

ViewModel responsibilities:

- validate input
- derive normalized odometer when trip meter mode is used
- save or update entry
- trigger recalculation of affected derived values

### 7.4 Fuel History

Screen content:

- chronological list
- item details
- edit/delete actions

ViewModel responsibilities:

- load entries
- delete entries safely

### 7.5 Start Trip / End Trip

Screen content:

- optional start location
- optional title
- current odometer
- optional end location when finishing

ViewModel responsibilities:

- enforce one active trip max
- create and complete trips

### 7.6 Trip Summary

Screen content:

- route details
- distance
- fuel bought during trip
- trip cost
- average consumption if available
- confidence or reliability messaging where needed

ViewModel responsibilities:

- build summary from trip and linked entries

### 7.7 Settings

Screen content:

- language selection
- theme preferences later if needed

## 8. Validation Rules

- tank size must be greater than zero
- odometer must be non-negative
- new odometer values should not go backwards without explicit correction workflow
- trip meter distance must be greater than or equal to zero when used
- trip meter mode requires a previous known odometer
- liters must be greater than zero
- total cost must be greater than or equal to zero
- price per liter must be greater than zero when entered directly
- only one trip may be active at once

## 9. Editing And Deletion Policy

This rule should be explicit because historical edits affect later derived values.

- editing or deleting a historical fuel entry must trigger recalculation of all affected derived statistics and trip summaries

This recalculation should be centralized in the domain layer, not spread across UI code.

## 10. Localization

Use Android string resources for:

- English
- Finnish

All user-facing text should come from resources from the start.

## 11. UI Direction

Visual direction:

- dark theme first
- clean spacing
- strong readability
- minimal chrome
- card-based summaries used carefully

Avoid:

- overly dense screens
- cluttered charts in v1
- too many accent colors

## 12. Development Phases

### Phase 1

- initialize Android project
- add Compose, Room, Hilt, Navigation
- create theme and app shell
- enable Room schema export and migration setup

### Phase 2

- implement car setup
- create Room schema
- add repositories and core domain models
- define persisted value units and converters

### Phase 3

- implement fuel entry CRUD
- implement history list
- implement recalculation triggers after edits and deletions

### Phase 4

- implement `ConsumptionSegmentBuilder`
- implement `FuelCalculationService`
- add stats screen or dashboard cards
- expose calculation confidence in domain output

### Phase 5

- implement trip start/end
- link fuel entries to active trip
- implement `TripSummaryService`
- build trip summary

### Phase 6

- localization
- UI polish
- validation hardening
- testing

## 13. Testing Strategy

Priority tests:

- calculation engine unit tests
- repository tests for Room
- ViewModel tests for validation and state transitions

Most critical test cases:

- full-tank to full-tank calculation
- partial fill between full anchors
- starting anchor excluded from segment fuel used
- ending anchor included in segment fuel used
- editing an old entry recalculates stats correctly
- deleting an entry updates trip and summary data correctly
- trip with no refuel during route
- trip with one or more refuels during route
- confidence state changes from available to unavailable when anchor data is broken

## 14. Main Technical Risks

- consumption logic may be misleading if reliability rules are not explicit in both code and UI
- trip-cost expectations may differ from actual implementation if estimated usage is mixed with purchased fuel without clear wording
- old-entry edits can invalidate later derived statistics

These risks should be handled by keeping calculation logic centralized in the domain layer and by separating all-entry totals from reliable-segment metrics.
