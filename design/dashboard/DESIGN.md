# Design System Document: FuelTrack Editorial

## 1. Overview & Creative North Star

### Creative North Star: "The Precision Naturalist"
The digital experience for this fuel tracking application is defined by a "Precision Naturalist" aesthetic. It moves away from the sterile, high-contrast rigidity of standard utility apps, opting instead for an editorial, grounded atmosphere. We leverage a palette of organic greens and warm neutrals to evoke the feeling of high-end automotive journals and sustainability reports.

To break the "template" look, we utilize **intentional asymmetry** and **tonal layering**. Data is not just displayed; it is curated. Large-scale typography is paired with expansive whitespace to ensure that while the app remains highly functional for scanning fuel data, it feels like a premium, bespoke tool rather than a generic spreadsheet.

---

## 2. Colors

This system utilizes a sophisticated Material 3 foundation, but we elevate it through specific execution rules that prioritize depth and sophistication over flat containment.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning or containment. Boundaries must be defined solely through background color shifts or subtle tonal transitions. For example, a statistics section should be defined by moving from `surface` (#fcf9f4) to `surface-container-low` (#f6f3ee), not by drawing a box.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers—like stacked sheets of fine, heavy-stock paper. Use the container tiers to create depth:
- **Base Layer:** `surface` (#fcf9f4) for the global background.
- **Structural Sections:** `surface-container-low` (#f6f3ee) for large, grouped content areas.
- **Actionable Cards:** `surface-container-lowest` (#ffffff) to provide a soft, "bright" lift for primary interaction points.

### The "Glass & Signature" Texture
- **CTAs & Heroes:** Move beyond flat color by using subtle linear gradients. A primary button should transition from `primary` (#006c4d) to `primary_container` (#58b38d) at a 135-degree angle to provide a "tactile" glow.
- **Overlays:** For bottom sheets or floating modals, use a backdrop-blur (12px–20px) combined with a semi-transparent `surface_variant` (#e5e2dd at 80% opacity) to create a "frosted glass" effect.

---

## 3. Typography

The typography strategy balances the authoritative weights of **Manrope** for brand expression and the high-utility clarity of **Inter** for data consumption.

- **Display & Headlines (Manrope):** Use `display-lg` and `headline-md` to anchor screens. The high x-height of Manrope provides an "editorial" feel that makes simple fuel numbers look like headline news.
- **Body & Labels (Inter):** Use `body-md` for general information and `label-md` for technical data points (liters, currency, dates). Inter’s neutral, geometric construction ensures maximum legibility during quick scans at a gas station.
- **Visual Weight:** Always pair a `headline-sm` in `on_surface` (#1c1c19) with `label-sm` in `on_surface_variant` (#3e4943) to create a clear "Title/Detail" hierarchy without needing dividers.

---

## 4. Elevation & Depth

We reject traditional, heavy drop shadows in favor of **Tonal Layering** and **Ambient Light**.

### The Layering Principle
Depth is achieved by "stacking." A card using `surface_container_lowest` (#ffffff) placed on a background of `surface_container` (#f0ede9) creates a natural, soft lift. This is our primary method of elevation.

### Ambient Shadows
When a component must float (e.g., a Floating Action Button or a critical summary card):
- **Blur:** 24px to 40px.
- **Opacity:** 4% to 8%.
- **Tint:** Shadow color must be a tinted version of `on_surface` (dark olive/grey) rather than pure black, ensuring the shadow feels like it belongs to the environment.

### The "Ghost Border" Fallback
If a border is required for extreme accessibility needs, use a **Ghost Border**: `outline_variant` (#bec9c1) at **15% opacity**. 100% opaque borders are strictly forbidden.

---

## 5. Components

### Buttons
- **Primary:** High-pill shape (`rounded-full`). Gradient from `primary` to `primary_container`. Text: `label-md` in `on_primary` (#ffffff), all-caps with 0.05rem letter spacing.
- **Secondary:** `surface_container_high` (#ebe8e3) background with `on_surface` text. No border.

### Fuel Entry Cards
- **Structure:** No dividers. Use a 2-column asymmetric grid.
- **Visuals:** Use `surface_container_lowest` for the card body. Use `primary` (#006c4d) for the primary "Liters" data and `on_surface_variant` for the "Price per Liter" metadata.
- **Spacing:** Use `spacing-4` (1rem) for internal padding to give data "room to breathe."

### Input Fields
- **Styling:** Soft-filled style using `surface_container_highest` (#e5e2dd). 
- **Corners:** `rounded-lg` (0.5rem).
- **Active State:** Change background to `surface_bright` and add a 2px "Ghost Border" of `primary` at 40% opacity.

### Navigation Bar
- **Style:** Glassmorphic. Background `surface` at 85% opacity with a 16px backdrop blur.
- **Active State:** Use a `primary_fixed` (#99f5cb) soft pill behind the active icon to indicate selection, mirroring the Material 3 "Active Indicator" but with softer transparency.

---

## 6. Do's and Don'ts

### Do
- **Do** use `spacing-8` or `spacing-10` between major content blocks to emphasize the premium, editorial feel.
- **Do** use `primary` (#006c4d) sparingly as an "accent of intent"—only for primary actions or key trend indicators.
- **Do** align data points to a strict vertical rhythm to ensure the "scanning" requirement is met.

### Don't
- **Don't** use 1px dividers to separate list items. Use `spacing-2` of vertical whitespace or a subtle shift to `surface_container_low`.
- **Don't** use pure black (#000000) for text. Use `on_surface` (#1c1c19) to maintain the warm, organic tone of the palette.
- **Don't** use "hard" corners. Even the smallest components should use at least `rounded-sm` (0.125rem) to maintain the system's "soft-modern" approachable nature.