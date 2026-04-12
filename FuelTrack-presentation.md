# FuelTrack

An Android app for tracking car fuel usage, fuel costs, and trip-specific refuels.

---

## 1. Project Overview

FuelTrack started from a real personal need: tracking how much fuel a car uses, how much driving costs, and how much a specific trip ends up costing.

The goal was not just to build a UI for entering refuels. The goal was to design a small but serious product where:

- data is stored cleanly
- calculations are trustworthy
- uncertain values are not presented as facts
- user input remains practical for real life

Version 1 is intentionally scoped as:

- Android-only
- single user
- single car
- local-first
- offline

This kept the project focused and gave room to do the core logic properly instead of overbuilding accounts, cloud sync, or multi-car features too early.

---

## 2. Product Idea

FuelTrack is meant to help answer everyday questions such as:

- How much money have I spent on fuel?
- How much fuel has the car consumed over time?
- What is my average fuel consumption?
- What did this specific trip cost me?
- How much fuel did I buy during that trip?

It combines two connected workflows:

1. fuel logging
2. trip tracking

The important product decision was to make these use the same core data model rather than building separate systems for “fuel history” and “trip tracking”.

---

## 3. Planning Approach

Before coding, the project was planned in multiple steps:

1. product planning
2. technical architecture planning
3. MVP backlog definition
4. testing plan
5. review checklist
6. done tracking

This was intentional. I wanted the project to be guided by decisions, not by random feature implementation.

The planning work focused especially on:

- scope control
- data model quality
- fuel calculation rules
- edit/delete safety
- realistic MVP boundaries

The core principle was:

> define what the numbers mean before implementing the screens that show them

That matters in this kind of app because the expensive refactor risk is not mainly the UI. It is the semantics of consumption, cost per kilometer, and trip-linked fuel totals.

---

## 4. Main Product Decisions

The first version intentionally stays conservative.

### Included in MVP

- car setup
- add fuel entry
- odometer and trip-meter based input
- fuel history
- edit/delete fuel entries
- dashboard metrics
- trip start
- trip end
- active trip view
- trip history

### Explicitly deferred

- cloud sync
- multi-user
- multiple cars
- receipt scanning
- export/import
- advanced tank-state estimation
- station autocomplete / geolocation integration

This helped protect the project from scope creep and kept the implementation centered around reliable tracking rather than feature inflation.

---

## 5. Technology Stack

FuelTrack is built with:

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM with a real domain layer
- **Navigation:** Navigation Compose
- **Dependency Injection:** Hilt
- **Persistence:** Room
- **Async / State:** Coroutines + Flow

This stack was chosen because it fits a local-first Android app well and keeps version 1 maintainable.

---

## 6. Architecture

The app is structured into three main layers:

### UI layer

Responsible for:

- screen rendering
- form input
- navigation
- user interaction
- mapping domain results into display states

### Domain layer

Responsible for:

- business rules
- validation
- fuel calculations
- trip summary logic
- confidence / reliability decisions

### Data layer

Responsible for:

- Room entities
- DAOs
- repositories
- mapping between persistence and domain models

This separation was important because calculation-heavy logic should not leak into ViewModels or composables.

---

## 7. Best-Practice Decisions

One of the strongest themes in this project was trying to follow sound engineering practices even though the app is relatively small.

### 7.1 Persist safe numeric units

I did not want money and fuel values stored as plain floating-point fields.

So the app uses safe persisted units such as:

- liters as milliliters
- total cost as cents
- price per unit as micros

This reduces rounding drift and protects future calculations.

### 7.2 Normalize odometer data

The app supports two input modes:

- direct odometer
- trip meter

But calculations should not depend on two competing history systems.

So trip-meter input is treated as a UX convenience only. Every fuel entry is normalized into a stored odometer value.

That is a good example of:

- user-friendly input
- clean stored model

at the same time.

### 7.3 Separate totals from reliable metrics

The app deliberately separates:

- all-entry totals
- reliable consumption metrics

For example:

- total money spent uses all entries
- total fuel bought uses all entries
- reliable consumption only uses completed full-tank segments

This avoids presenting precise-looking but misleading results.

### 7.4 Keep calculation logic out of UI

Fuel calculations and trip summary logic are handled in explicit domain services, not scattered through screens or repositories.

Examples include:

- `ConsumptionSegmentBuilder`
- `FuelCalculationService`
- `TripSummaryService`
- mutation planning / recalculation logic for historical edits

### 7.5 Protect historical edits

Historical fuel entries can affect later trip-meter-derived odometer values.

So edit/delete operations were not treated as simple CRUD only. They required recalculation rules so that later normalized data stays consistent.

That was one of the more serious engineering parts of the project.

---

## 8. Data Model

FuelTrack is built around three main stored models:

### Car

- id
- name
- tank size
- initial odometer
- timestamps

### FuelEntry

- id
- carId
- optional tripId
- date/time
- distance input mode
- normalized odometer
- optional trip meter value
- liters
- total cost
- price per unit
- station
- practically full tank flag
- optional note
- timestamps

### Trip

- id
- carId
- title
- start location
- end location
- start odometer
- end odometer
- start time
- end time
- status
- timestamps

This model keeps the app simple enough for MVP, but still structured for future expansion.

---

## 9. Calculation Model

This is the part of the app I would highlight most in a technical discussion.

Fuel tracking looks simple at first, but the calculation rules matter a lot.

### Reliable consumption

Reliable consumption is calculated only between two refuels that are both marked as practically full.

Example:

- entry A = full tank
- entry B = partial fill
- entry C = full tank

Then:

- distance = odometer(C) - odometer(A)
- fuel used = liters(B) + liters(C)

Important rule:

- the starting anchor is excluded
- the ending anchor is included

This is deliberate and documented.

### Practical reliability

The app does not pretend the user has laboratory-grade measurement accuracy.

`isFullTank` is treated as:

- practically full
- user-confirmed
- good enough for reliable everyday tracking

That makes the app more realistic and honest.

### Trip summaries

Trip summaries are currently based primarily on refuels linked to the trip.

That means the app favors:

- real tracked purchases
- clear limitations

instead of trying to invent complicated tank-state estimation too early.

---

## 10. UX and Design Direction

The UI direction is meant to feel:

- calm
- soft
- premium utility
- editorial but practical

Design influences and goals were documented before implementation. The guiding idea was to avoid a generic Material demo look.

Main principles:

- warm neutral backgrounds
- restrained green accents
- soft rounded corners
- tonal layering instead of heavy borders
- subtle depth instead of aggressive shadows
- clearer hierarchy through spacing and typography

The project uses:

- **Manrope** for major titles and emphasis
- **Inter** for body text, labels, and supporting content

Several screens were iterated toward this style, including:

- dashboard
- add fuel entry modal
- fuel history
- active trip workspace
- trip history
- start/end trip modals
- car setup

---

## 11. Current Feature Set

At the current stage, the app includes:

- car setup
- dashboard
- add fuel entry modal
- edit fuel entry
- fuel history
- start trip modal
- end trip modal
- active trip workspace
- trip history

The app also supports:

- trip-linked fuel entries
- station field
- optional note field
- both odometer and trip-meter based input
- historical entry edit/delete with recalculation handling

---

## 12. Quality and Governance

Another important part of this project is that I tried to keep implementation disciplined.

Project control files were created for:

- backlog
- done log
- testing plan
- review checklist
- architecture alignment

This helped maintain consistency across changes.

The review mindset for the app emphasizes:

- calculation correctness
- architecture boundaries
- persistence safety
- edit/delete safety
- avoiding false precision in UI

That governance layer is unusual for a small personal app, but I considered it worthwhile because the app’s value depends on trustworthy data behavior.

---

## 13. Testing and Validation

The project includes automated tests around the higher-risk parts of the system, especially:

- fuel segment calculation rules
- mutation planning
- historical odometer recalculation behavior
- trip summary semantics
- trip start/end use cases

In addition, repeated verification has been done through:

- `assembleDebug`
- `testDebugUnitTest`
- manual UI acceptance checks on emulator

The test strategy reflects the fact that the risky part of the app is not just form validation. It is the correctness of derived values over time.

---

## 14. What Makes This Project Interesting

FuelTrack is not just “a CRUD app with some screens.”

The interesting part is the combination of:

- real personal product need
- practical data modeling
- local-first architecture
- calculation correctness
- UX decisions shaped by domain rules

Good examples:

- allowing trip-meter input while storing normalized odometer history
- distinguishing between all-entry totals and reliable fuel metrics
- preventing historical edits from silently corrupting later derived values
- presenting confidence and fallbacks honestly instead of implying fake precision

---

## 15. My Engineering Priorities In This Project

If I had to summarize my approach in a few points, it would be:

- define the product rules before coding the UI
- keep business logic out of UI where possible
- store important values in stable units
- protect future maintainability
- avoid overbuilding version 1
- iterate the design deliberately instead of shipping the default framework look

In other words, I tried to treat even a personal app with professional discipline.

---

## 16. Current Status

FuelTrack is already functional as a real MVP foundation.

The app can:

- create a car
- log refuels
- track costs
- show fuel history
- start trips
- end trips
- link refuels to trips
- show active trip information
- keep a trip history

It is not finished as a polished production product yet, but the architecture, core data model, and key interaction flows are in place.

---

## 17. Next Likely Steps

The next useful improvements would likely be:

- further UI consistency passes across all screens
- localization cleanup
- better trip editing actions
- stronger navigation consistency
- polishing remaining screens into the same visual system
- additional tests around newer UI/state flows

Longer term, the architecture also leaves room for:

- multi-car support
- station analytics
- export/import
- richer fuel-price comparison features
- more advanced trip/fuel estimation

---

## 18. Closing Summary

FuelTrack is a local-first Android app for fuel and trip tracking built with a strong emphasis on:

- sound architecture
- data-model quality
- careful calculation rules
- practical UX
- maintainable implementation

The most important thing about the project is not just that it works.
It is that the app was intentionally planned and implemented in a way that tries to keep the numbers meaningful, the scope controlled, and the codebase refactor-resistant as the product grows.
