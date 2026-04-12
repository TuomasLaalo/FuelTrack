# Fuel Tracker App Review Checklist

Use this checklist when reviewing implementation work for the project.

## 1. Product Alignment

- Does the change match the product spec?
- Does the change stay within MVP scope?
- Did the change introduce hidden scope creep?

## 2. Architecture Alignment

- Does the change follow the architecture plan?
- Does UI code avoid business-logic leakage?
- Are repositories only handling persistence concerns?
- Are calculations handled in domain services instead of ViewModels or DAOs?

## 3. Data Model And Persistence

- Are persisted numeric values stored in safe units, not risky floating-point fields?
- Is `carId` preserved where required?
- Are trip location fields treated as optional?
- Does the change respect Room schema and migration readiness?

## 4. Calculation Rules

- Does `isFullTank` still mean practically full, not perfect measurement?
- Are reliable segments built only between two full-tank anchors?
- Is the starting anchor excluded from segment fuel used?
- Is the ending anchor included in segment fuel used?
- Are all entries still counted in total cost and total liters bought?
- Are only completed reliable segments used for reliable consumption metrics?
- Is confidence or fallback messaging handled correctly?

## 5. Edit/Delete Safety

- If a historical fuel entry changes, are affected derived values recalculated?
- If a historical fuel entry is deleted, are affected trip summaries recalculated?

## 6. Testing

- Were relevant unit tests added or updated?
- Were repository tests added or updated if persistence changed?
- Were ViewModel tests added or updated if state logic changed?
- Was manual acceptance checked if the UI changed?

## 7. UX And Localization

- Is user-facing wording clear about reliability versus totals?
- Are fallback states understandable?
- Are all strings in resources?
- Does the UI still fit the minimal dark-theme direction?

## 8. Merge Readiness

- Is the change understandable without hidden assumptions?
- Are risks or open questions documented?
- Can this be accepted without forcing a later architecture refactor?
