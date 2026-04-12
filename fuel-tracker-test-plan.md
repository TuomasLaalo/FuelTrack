# Fuel Tracker App Test Plan

This file defines how the app should be tested during development.

## 1. Test Strategy

Use a layered testing approach:

- unit tests for calculation and business rules
- repository tests for Room behavior
- ViewModel tests for validation and state transitions
- manual acceptance tests for key user flows

## 2. Testing Priorities

Highest priority:

- fuel calculation correctness
- reliable segment behavior
- trip summary correctness
- historical edit and delete recalculation behavior

Secondary priority:

- form validation
- localization coverage
- navigation stability
- dark theme usability

## 3. Unit Tests

Focus first on domain services:

- `ConsumptionSegmentBuilder`
- `FuelCalculationService`
- `TripSummaryService`

Critical scenarios:

- full-tank to full-tank segment
- partial fill between full anchors
- multiple partial fills between anchors
- trip meter input derives correct normalized odometer
- trip meter input is blocked when no previous odometer exists
- missing ending anchor
- missing starting anchor
- starting anchor excluded from fuel used
- ending anchor included in fuel used
- reliable segment cost calculation
- reliable distance calculation
- reliable cost per kilometer calculation
- confidence state is `HIGH_CONFIDENCE`
- confidence state becomes `NOT_AVAILABLE` when anchor data is missing

## 4. Repository Tests

Test the Room layer for:

- create and read car
- create and read fuel entries
- create and read trips
- query fuel entries in chronological order
- query active trip
- update fuel entry
- delete fuel entry
- relation between trip and fuel entries

## 5. ViewModel Tests

Test:

- onboarding validation
- add fuel entry validation
- distance input mode switching
- trip meter mode derives normalized odometer correctly
- trip start validation
- trip end validation
- only one active trip allowed
- recalculation triggered after edit
- recalculation triggered after delete

## 6. Manual Acceptance Tests

### 6.1 Car Setup

- Create one car
- Verify home screen opens
- Verify current odometer starts from onboarding value

### 6.2 Fuel Logging

- Add a normal full-tank entry
- Add a partial-fill entry
- Add an entry using direct odometer input
- Add an entry using trip meter input
- Edit an entry
- Delete an entry
- Verify history updates correctly
- Verify normalized odometer history remains correct

### 6.3 Reliable Consumption

- Add enough entries to form one reliable segment
- Verify average consumption is shown
- Remove anchor data and verify reliable consumption is no longer shown
- Verify fallback explanation text is displayed

### 6.4 Trip Flow

- Start a trip
- Add one or more linked fuel entries
- End the trip
- Verify trip summary values
- Verify linked entries still appear in normal fuel history

### 6.5 Localization

- Switch app language to English
- Switch app language to Finnish
- Verify key screens use translated strings

## 7. Regression Rules

Run calculation-related unit tests whenever:

- numeric storage units change
- calculation services change
- trip-meter normalization logic changes
- fuel-entry edit or delete logic changes
- trip summary logic changes

Run manual regression for core flows whenever:

- database schema changes
- navigation changes
- onboarding changes

## 8. Done Criteria For A Feature

A feature is not done until:

- implementation is complete
- relevant automated tests are added or updated
- manual acceptance path is checked if UI is involved
- review checklist has been checked

## 9. Test Status

Use this section later to track actual execution.

### Not Started

- No implementation tests yet
