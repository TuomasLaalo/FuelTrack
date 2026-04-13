# FuelTrack Localization Plan

## Purpose

This document defines how localization should be implemented in FuelTrack.

The goal is to move from mostly hardcoded UI text to a proper Android string-resource system that supports:

- English
- Finnish

The plan is intentionally staged so localization can be added safely without disrupting current UI work.

---

## 1. Current Status

### Already present

- `values/strings.xml` exists
- `values-fi/strings.xml` exists
- theme and app structure already support a normal Android resource-based localization approach

### Current limitation

Most user-facing strings are still hardcoded inside Compose UI files.

This includes:

- screen titles
- subtitles
- field labels
- placeholders
- button text
- menu text
- dialog titles and messages
- helper text
- empty states
- metric labels

So localization is not just “adding translations”.

It is a UI-wide refactor from hardcoded strings to `stringResource(...)`.

---

## 2. Localization Goals

FuelTrack localization should:

- support English and Finnish cleanly
- remove hardcoded user-facing text from composables where practical
- keep wording consistent across screens
- preserve the existing design tone
- avoid introducing string duplication or naming chaos

Version 1 target:

- system-language-based localization is enough
- in-app manual language switching can be deferred unless explicitly prioritized

---

## 3. Recommended Scope

### In scope for first pass

- all visible screen text on core flows
- dialog text
- menu text
- form labels and placeholders
- error messages shown to users
- section headers
- empty-state text
- card labels

### Out of scope for first pass

- runtime language override in settings
- advanced pluralization cleanup unless needed
- region-specific wording variants
- full copywriting redesign

---

## 4. Recommended Implementation Strategy

Localization should be done in four stages.

### Stage 1. String extraction pass

Move hardcoded user-facing strings from Compose files into `strings.xml`.

Rules:

- use clear, feature-based string names
- avoid generic keys like `title_1` or `button_text`
- prefer names that describe where and why the string is used

Examples:

- `dashboard_latest_refuel_title`
- `fuel_entry_station_label`
- `trip_summary_end_trip_button`
- `trips_delete_dialog_title`

### Stage 2. English resource baseline

Fill `values/strings.xml` with the English source-of-truth strings.

This should become the primary maintained reference file.

### Stage 3. Finnish translation pass

Add matching strings to `values-fi/strings.xml`.

Important:

- translation should preserve the app tone
- avoid overly formal wording
- keep labels compact enough for mobile layouts

### Stage 4. Verification and polish

Check:

- text overflow
- button width issues
- card spacing changes
- dark/light theme readability after translation
- consistency of recurring terms

---

## 5. Recommended Execution Order By Screen

Do localization in a stable, high-impact order.

### Phase A. Most important screens

1. Dashboard
2. Add Fuel Entry
3. Fuel History
4. Active Trip / Trip Summary
5. Trips

These are the main user-facing screens and should be localized first.

### Phase B. Supporting core flows

6. Start Trip
7. End Trip
8. Car Setup
9. Edit Fuel Entry

### Phase C. Shared components and dialogs

10. confirmation dialogs
11. reusable form labels where applicable
12. shared helper and empty-state wording

---

## 6. File Areas Likely Affected

Primary files likely to change:

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-fi/strings.xml`

Core UI files likely to need extraction work:

- `feature/dashboard/DashboardRoute.kt`
- `feature/fuelentry/FuelEntryFormContent.kt`
- `feature/fuelentry/AddFuelEntryRoute.kt`
- `feature/fuelentry/EditFuelEntryRoute.kt`
- `feature/fuelhistory/FuelHistoryRoute.kt`
- `feature/trip/TripSummaryRoute.kt`
- `feature/trip/TripsRoute.kt`
- `feature/trip/StartTripRoute.kt`
- `feature/trip/EndTripRoute.kt`
- `feature/carsetup/CarSetupRoute.kt`

Shared UI that may also be touched:

- `core/ui/component/ConfirmationDialog.kt`
- any shared cards/components with visible hardcoded copy

---

## 7. String Naming Convention

Use feature-first naming.

Recommended pattern:

`<feature>_<purpose>_<type>`

Examples:

- `dashboard_view_all_action`
- `fuel_entry_quantity_label`
- `fuel_entry_quantity_placeholder`
- `fuel_history_delete_dialog_message`
- `trip_summary_all_trips_action`
- `trips_empty_state_body`

Benefits:

- easy to find
- easy to review
- low chance of accidental reuse with wrong meaning

---

## 8. Wording Consistency Rules

Keep terminology consistent across the app.

Use the same wording for recurring concepts:

- `Fuel entry`
- `Trip meter`
- `Odometer`
- `Station`
- `Quantity`
- `Price / Unit`
- `Start trip`
- `End trip`
- `Active trip`
- `All trips`
- `Latest refuel`

Avoid mixing variants unless there is a good UX reason.

---

## 9. Technical Guidance

### In Compose

Use:

- `stringResource(id = ...)`

For formatted strings:

- use string arguments instead of string concatenation where practical

For example:

- dates or values that are already formatted can be inserted via placeholders

### What to avoid

- leaving visible text hardcoded in composables
- building full user-facing sentences through raw string concatenation
- using unclear resource names

---

## 10. Risks

### 1. Layout drift

Translations can change spacing and line breaks.

This is especially relevant for:

- buttons
- section headers
- compact stat cards
- modal controls

### 2. Inconsistent terms

If strings are extracted gradually without naming discipline, wording can drift.

### 3. Mixed localization state

Partially localized screens can feel inconsistent if some text comes from resources and some remains hardcoded.

This is why localization should be completed in screen-sized slices, not random single-string edits.

---

## 11. Testing / Verification

After each localization slice:

- build the app
- open the affected screens in English
- open the affected screens in Finnish
- verify that labels still fit
- verify dialogs, buttons, and menus remain readable

Recommended final verification:

- `assembleDebug`
- quick manual pass on main screens in both languages

---

## 12. Recommended Delivery Strategy

Do localization in 2 or 3 commits, not dozens.

Recommended commit structure:

1. `Extract core dashboard and fuel entry strings`
2. `Localize trip and history screens`
3. `Add Finnish translations and polish wording`

That keeps history readable while still splitting the work logically.

---

## 13. Recommendation

Localization is worth doing soon.

Reason:

- the app already has enough UI that delaying too long will increase cleanup cost
- it improves product quality directly
- it will make later settings and language features easier

The best next implementation order is:

1. Dashboard + Add Fuel Entry
2. Fuel History + Trips + Trip Summary
3. Start Trip + End Trip + Car Setup + Edit Fuel Entry
4. Final translation polish and verification
