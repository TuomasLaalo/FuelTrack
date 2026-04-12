# FuelTrack UI Refinement Plan

## Purpose

This document is for the current visual refinement phase of FuelTrack.

The goal is to make the app feel:

- softer
- calmer
- more editorial
- more premium
- less like a default utility app

This phase is mainly about visual direction, composition, and interaction weight.
It is not mainly about adding new product logic.

---

## Current Problem Summary

The app is functional, but the visual result still feels too generic compared with the dashboard reference.

Main issues:

- typography feels too default
- cards still feel like standard app components
- surfaces do not yet create enough soft depth
- spacing rhythm is not editorial enough
- buttons/actions feel more utilitarian than premium
- the dashboard only partially captures the intended composition

Important note:

The core logic and architecture are already in a good place.
That means we can focus on visual direction now without needing to redesign the product model.

---

## Creative Goal

FuelTrack should feel like:

- a polished personal driving journal
- a premium automotive utility
- a calm, intelligent dashboard
- something between a modern mobility app and an editorial data product

It should not feel like:

- a spreadsheet in app form
- a generic Material demo
- a fintech dashboard clone
- an over-designed concept UI that cannot be built realistically

---

## Visual Principles

### 1. Softness Over Hardness

Use:

- soft corners
- subtle tonal differences
- gentle shadows
- warm backgrounds

Avoid:

- hard borders
- strong separators
- sharp, high-contrast blocks
- overly dark or heavy containment

### 2. Tonal Layering Over Boxes

The UI should feel like stacked paper layers, not outlined containers.

Use these layers deliberately:

- page background
- grouped content surface
- raised card surface
- accent surface for key actions or highlights

### 3. Accent With Restraint

Green should feel intentional.

Use green mainly for:

- key metric emphasis
- primary actions
- active state cues
- light supportive highlights

Do not let green dominate every component.

### 4. Editorial Hierarchy

The interface should have a clear reading flow:

1. primary screen message
2. main metric or action
3. supporting stats
4. detailed history or metadata

Not every card should compete equally for attention.

### 5. Production Realism

All UI choices should still feel buildable and maintainable.

Avoid:

- decorative-only features with no clear UX value
- extreme motion
- overly complex layered effects
- fake conceptual widgets that do not match current product scope

---

## Refinement Areas

## 1. Typography

This is one of the highest-impact areas.

Current issue:

- text styling still feels too default and too “app template”

Refinement goals:

- stronger headline identity
- calmer supporting typography
- better contrast between primary metric text and metadata
- more premium overall rhythm

Questions to resolve in Figma:

- What is the exact headline font direction?
- What is the supporting/body font direction?
- How large should the main metric feel on dashboard?
- How compressed or spacious should labels and metadata feel?

Implementation note:

Typography should likely be refined before final card tuning, because typography changes alter perceived spacing and weight.

---

## 2. Color And Surface System

Current issue:

- surfaces are improved, but still not fully tuned to the reference mood

Refinement goals:

- warm neutral base background
- slightly lifted grouped surfaces
- brighter card surfaces for key content
- restrained green accent
- occasional warm highlight tones where appropriate

Questions to resolve in Figma:

- What is the exact base background tone?
- How warm should section surfaces feel?
- How bright should raised cards be?
- How much gradient is acceptable on hero areas and primary actions?

Implementation note:

We should finalize a small token set:

- background
- section surface
- card surface
- accent green
- soft secondary tint
- primary text
- secondary text

---

## 3. Shadows And Depth

Current issue:

- depth is still not soft and subtle enough

Refinement goals:

- minimal shadow usage
- more tonal lift than obvious floating cards
- only hero or priority surfaces should feel noticeably elevated

Rules:

- avoid strong black shadows
- prefer broad, very soft shadows
- use tonal contrast first, shadow second

Questions to resolve in Figma:

- which components need visible elevation at all?
- should quick stat cards float slightly or sit flatter?
- should the hero card be the only strong focal lift?

---

## 4. Card Language

Current issue:

- cards are still too similar to each other

Refinement goals:

- define 3 to 4 clear card types
- give each a distinct role

Recommended card types:

- Hero card
- Quick stat card
- Refuel detail card
- Soft message/info card

Questions to resolve in Figma:

- what radius should each type use?
- what padding should each type use?
- should some cards feel more full-bleed or more compact?
- how asymmetric should the internal layouts be?

---

## 5. Buttons And Actions

Current issue:

- some actions still feel too generic and too equal in weight

Refinement goals:

- clearer distinction between primary and secondary actions
- softer button shapes
- more embedded secondary actions
- more elegant action placement

Questions to resolve in Figma:

- should the primary trip action feel pill-like?
- should “view all” actions be text-only or softly contained?
- how bold should add/refuel actions feel?

Implementation note:

Not every action needs a full button.
Some actions should become text links or lighter pills.

---

## 6. Spacing System

Current issue:

- the app still feels slightly too compressed and component-driven

Refinement goals:

- more breathing room between major sections
- more intentional grouping inside cards
- stronger rhythm between heading, metric, metadata, and actions

Questions to resolve in Figma:

- what is the spacing between major dashboard blocks?
- how much padding does a premium card need?
- how close or far should metadata sit under main values?

Implementation note:

Spacing often matters more than adding decorative styling.
This should be treated as a first-class design decision.

---

## 7. Dashboard Composition

This is the current highest-priority screen.

The dashboard should establish the product identity for the whole app.

Elements to refine:

- header treatment
- active vehicle block
- hero consumption card
- primary trip action
- quick stats layout
- latest refuel card
- lower-priority supporting information

Questions to resolve in Figma:

- how close should we stay to the Stitch reference layout?
- should the dashboard feel more like a “cover page” or a “control center”?
- what is the one most important metric on first glance?
- what should feel secondary?

---

## 8. Core Screens To Design Next

Before major new implementation work, these screens should be designed more clearly:

1. Dashboard
2. Fuel History
3. Add Fuel Entry
4. Trip List
5. Trip Summary

Optional after that:

6. Start Trip
7. End Trip
8. Bottom Navigation

Reason:

These screens define the shared component language.
If they are designed coherently, implementation becomes much easier and less wasteful.

---

## 9. Shared Component System To Define

After the main screens are designed, define these shared patterns:

- app header
- section title
- hero metric card
- quick stat card
- refuel card
- info/message card
- primary button
- secondary button
- text action
- input field
- segmented control
- bottom navigation style

This should come after screen design, not before.

---

## 10. What Not To Change Prematurely

Do not force large product changes just to satisfy a visual mockup.

Avoid changing these unless intentionally discussed:

- fuel calculation model
- reliability rules
- trip-meter normalization model
- one-car assumption
- trip summary semantics
- data persistence shape

Those are already reasonably solid.
This phase should improve presentation, not destabilize the foundation.

---

## 11. Suggested Workflow

### Phase A: Figma Refinement

- refine dashboard first
- define visual mood and hierarchy
- refine 3 to 5 main screens
- identify reusable component patterns

### Phase B: Review Against Product Reality

For each design idea, label it as:

- visual only
- navigation change
- behavior change
- data-model change

Only visual and navigation changes should be implemented directly without deeper review.

### Phase C: Implementation

- update theme tokens
- implement shared components
- update dashboard
- update other primary screens
- then revisit smaller polish details

---

## 12. Immediate Next Questions For Design Work

When iterating in Figma, focus especially on:

1. What exact font direction feels right?
2. How soft should the surface system be?
3. Which elements deserve green emphasis?
4. What should the dashboard hero emphasize first?
5. Should the app adopt bottom navigation now?
6. Which card styles repeat across screens?

---

## 13. Definition Of Success For This Refinement Phase

This phase is successful when:

- the dashboard clearly reflects the intended FuelTrack identity
- the app feels softer and more premium
- typography no longer feels default
- surfaces feel layered and calm
- action hierarchy feels deliberate
- the main screens look like one coherent product
- implementation can proceed without guessing the visual language every time
