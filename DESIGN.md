# Design System Document

## 1. Overview & Creative North Star: "ResQFood"
The "ResQFood" design philosophy rejects the clinical, sterile nature of traditional logistics platforms in favor of a warm, editorial, and organic experience. This design system bridges the gap between high-end culinary magazines and sustainable technology.

**The Creative North Star:** We are not just moving surplus food; we are honoring it. The UI must feel like a curated gallery—breathable, intentional, and deeply tactile. We move away from the "app-in-a-box" look by utilizing **intentional asymmetry**, where imagery breaks container boundaries, and **tonal layering**, which replaces rigid borders with soft, atmospheric transitions. The goal is to create a sense of "abundance without waste."

---

## 2. Color & Surface Philosophy
This system uses a palette that mimics a fresh, thriving ecosystem: the vibrant emeralds of fresh produce, the soft off-whites of organic textures, and deep forest greens for grounded contrast.

### The "No-Line" Rule
To achieve a premium, modern feel, **1px solid borders are strictly prohibited for sectioning.** Boundaries must be defined through:
- **Tonal Shifts:** Placing a `surface-container-low` (#F3F5F3) element against a `surface` (#F8FAF8) background.
- **Negative Space:** Utilizing the larger increments of our Spacing Scale (8, 10, or 12) to create "invisible" gutters.

### Surface Hierarchy & Nesting
Treat the UI as a series of stacked, semi-translucent sheets. 
- **Base Layer:** `surface` (#F8FAF8).
- **Secondary Content:** `surface-container` (#EAECEA).
- **Interactive Highlighting:** `surface-container-highest` (#DFE2DF).

### The "Glass & Gradient" Rule
To avoid a flat "template" look, use **Glassmorphism** for floating headers or navigation bars. Apply `surface` at 80% opacity with a `backdrop-filter: blur(12px)`. For Primary CTAs, use a subtle linear gradient transitioning from `primary` (#059669) to `primary_container` (#D1FAE5) at a 135-degree angle. This adds "soul" and depth that static hex codes cannot provide.

### Auction Accent Palette
Auctions are a distinct feature within ResQFood and receive a **warm orange accent** to differentiate them from standard Packs — while the overall page still feels unmistakably "ResQFood" (green surfaces, same typography, same card shapes).

- **`auction`** (#E65100) — Deep orange. Used **only** for: auction CTA buttons, auction section-header icons, and tiny inline badges/chips.
- **`auction-container`** (#FFF3E0) — Soft peach. Background for auction badges and small highlighted areas (never full sections).
- **`auction-fixed`** (#FFAB40) — Amber. Hover states and secondary highlights within an auction context.
- **`on-auction`** (#FFFFFF) — Text on top of `auction` backgrounds.
- **`on-auction-container`** (#BF360C) — Dark terracotta. Text on `auction-container` backgrounds.

**Usage Rules:**
- **Do** apply `auction` to submit buttons and small iconography on auction-specific forms.
- **Do** keep form focus states, breadcrumb links, and navigation in `primary` (emerald).
- **Don't** replace surface/background colours with orange; the page must remain within the green ResQFood ecosystem.
- **Don't** use `auction` tokens outside of explicitly auction-related components (e.g. auction creation forms, auction cards, auction status badges).

---

## 3. Typography: The Editorial Voice
Our typography pair balances the authority of a sustainable leader with the friendliness of a local community.

* **Display & Headlines (Plus Jakarta Sans):** Chosen for its modern, slightly geometric yet friendly curves. Use `display-lg` for hero statements with tight letter-spacing (-0.02em) to create a bold, "magazine-cover" impact.
* **Body & Titles (Be Vietnam Pro):** A highly legible sans-serif that feels approachable and warm.
* **Hierarchy as Identity:** Use `headline-sm` (#191C1A) for card titles, paired with `label-md` in `secondary` (#527A61) for category tags. The high contrast between the primary and the secondary emphasizes the "freshness" and "urgency" of food rescue.

---

## 4. Elevation & Depth: Tonal Layering
Traditional drop shadows are often too "heavy" for a minimalist sustainability platform. We use light to create hierarchy.

- **The Layering Principle:** Instead of shadows, stack surfaces. A card using `surface-container-lowest` (#FFFFFF) sitting on a `surface-container` (#EAECEA) background creates a natural, crisp lift.
- **Ambient Shadows:** For "Floating Action Buttons" or critical modals, use a diffused shadow: `box-shadow: 0 12px 32px rgba(25, 28, 26, 0.06)`. The shadow is a tint of our `on-surface` color, not pure black, ensuring it feels like a natural shadow in a sunlit room.
- **Ghost Borders:** If a boundary is required for accessibility (e.g., in a high-density data list), use `outline-variant` (#C2C9C2) at **15% opacity**.

---

## 5. Components & UI Patterns

### Cards & Lists
* **The Content Card:** Use `rounded-xl` (1.5rem) corners. Forbid divider lines. Instead, use a `3` (1rem) spacing increment between internal elements and a background shift for the footer of the card.
* **Photography:** Images should never be fully contained. Allow food photography to "bleed" to the top and sides of the card, using a `surface-dim` placeholder while loading.

### Buttons
* **Primary:** Gradient of `primary` to `primary_container`. Pill-shaped (`rounded-full`).
* **Secondary:** `surface-container-highest` background with `on-surface` text. No border.
* **Tertiary:** `surface` background, `primary` text, with a subtle underline of `primary_fixed` at 2px height for an editorial feel.

### Input Fields
* **Style:** Minimalist. Use `surface-container-low` as the background with a `rounded-md` (0.75rem) corner.
* **States:** On focus, transition the background to `surface-container-lowest` and add a `2px` "Ghost Border" using `primary`.

### Specialized Components: "The Impact Tracker"
* **Sustainability Chips:** Use `primary_fixed` (#A7F3D0) backgrounds with `on_primary_fixed_variant` (#022C22) text to denote CO2 savings or meals rescued. These should use `rounded-sm` for a slightly more technical, precise feel compared to the rounder UI.

---

## 6. Do's and Don'ts

### Do:
* **Do** use asymmetrical layouts. Let an image take up 60% of a row while text takes up 40%.
* **Do** use "white space" as a functional element. If a screen feels crowded, increase the spacing to the next scale increment (e.g., move from `6` to `8`).
* **Do** ensure all vibrant food photography is high-resolution and features natural lighting.

### Don't:
* **Don't** use 1px solid black or grey borders. Use background color shifts.
* **Don't** use "default" system shadows. They feel "cheap" and break the "ResQFood" aesthetic.
* **Don't** use more than three levels of surface nesting. If a fourth level is needed, reconsider the information architecture.
* **Don't** center-align long passages of body text. Keep it left-aligned to maintain the editorial, readable grid.