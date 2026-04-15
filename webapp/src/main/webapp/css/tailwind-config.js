/*
 * Centralized Tailwind CSS configuration — "ResQFood" design system.
 *
 * Palette derived from DESIGN.md using Material Design 3 token naming.
 * Loaded AFTER the Tailwind CDN script in every page/layout.
 *
 * Color philosophy (DESIGN.md §2):
 *   Surfaces  → soft off-whites with green undertone
 *   Primary   → vibrant emerald (#059669)
 *   Secondary → muted sage (#527A61)
 *   Tertiary  → warm amber/soil (#6b5000)
 *   Error     → standard red (#ba1a1a)
 */
tailwind.config = {
  darkMode: "class",
  theme: {
    extend: {

      /* ── Colour tokens ─────────────────────────────────────── */
      colors: {
        /* Surfaces (DESIGN.md §2 — green-tinted off-whites, "no-line" rule) */
        "surface":                    "#F8FAF8",
        "surface-dim":                "#D3D6D3",
        "surface-bright":             "#FDFEFD",
        "surface-container-lowest":   "#FFFFFF",
        "surface-container-low":      "#F3F5F3",
        "surface-container":          "#EAECEA",
        "surface-container-high":     "#E4E7E4",
        "surface-container-highest":  "#DFE2DF",
        "surface-variant":            "#DCE5DC",
        "surface-tint":               "#059669",
        "background":                 "#F8FAF8",

        /* On-surface / text (DESIGN.md §3) */
        "on-surface":                 "#191C1A",
        "on-surface-variant":         "#404943",
        "on-background":              "#191C1A",

        /* Primary — vibrant emerald (DESIGN.md §2 & §5 buttons) */
        "primary":                    "#059669",
        "primary-container":          "#D1FAE5",
        "on-primary":                 "#FFFFFF",
        "on-primary-container":       "#002114",
        "primary-fixed":              "#A7F3D0",
        "primary-fixed-dim":          "#6EE7B7",
        "on-primary-fixed":           "#022C22",
        "on-primary-fixed-variant":   "#022C22",

        /* Secondary — muted sage (DESIGN.md §3 label-md) */
        "secondary":                  "#527A61",
        "secondary-container":        "#C7EBCE",
        "on-secondary":               "#FFFFFF",
        "on-secondary-container":     "#3E6350",
        "secondary-fixed":            "#C7EBCE",
        "secondary-fixed-dim":        "#ABCFB2",
        "on-secondary-fixed":         "#0E2919",
        "on-secondary-fixed-variant": "#3E6350",

        /* Tertiary — warm amber / "rich soil" (DESIGN.md §1) */
        "tertiary":                   "#6B5000",
        "tertiary-container":         "#F5DFA0",
        "on-tertiary":                "#FFFFFF",
        "on-tertiary-container":      "#221B00",
        "tertiary-fixed":             "#FFEDB5",
        "tertiary-fixed-dim":         "#E8C84E",
        "on-tertiary-fixed":          "#221B00",
        "on-tertiary-fixed-variant":  "#524000",

        /* Error */
        "error":                      "#BA1A1A",
        "error-container":            "#FFDAD6",
        "on-error":                   "#FFFFFF",
        "on-error-container":         "#93000A",

        /* Auction accent (DESIGN.md §2 — orange for auction differentiation) */
        "auction":                    "#E65100",
        "auction-container":          "#FFF3E0",
        "auction-fixed":              "#FFAB40",
        "on-auction":                 "#FFFFFF",
        "on-auction-container":       "#BF360C",

        /* Outline (DESIGN.md §4 ghost borders) */
        "outline":                    "#707973",
        "outline-variant":            "#C2C9C2",

        /* Inverse */
        "inverse-surface":            "#2E312E",
        "inverse-on-surface":         "#EFF1EE",
        "inverse-primary":            "#6EE7B7",

        /* ── Backward-compat aliases (reservationLayout.tag pages) ── */
        "appBg":          "#F8FAF8",
        "appText":        "#191C1A",
        "appPrimary":     "#059669",
        "appPrimarySoft": "#D1FAE5",
        "appCard":        "#FFFFFF",
        "appMuted":       "#404943",
        "appBorder":      "#C2C9C2",
        "appSurface":     "#F3F5F3",
        "appSuccess":     "#059669",
        "appSuccessSoft": "#D1FAE5",
      },

      /* ── Typography (DESIGN.md §3) ────────────────────────── */
      fontFamily: {
        "headline": ['"Plus Jakarta Sans"', "sans-serif"],
        "body":     ['"Be Vietnam Pro"', "sans-serif"],
        "label":    ['"Plus Jakarta Sans"', "sans-serif"],
      },

      /* ── Border radius (DESIGN.md §5 — cards rounded-xl) ── */
      borderRadius: {
        DEFAULT: "0.25rem",
        "md":    "0.75rem",
        "lg":    "0.5rem",
        "xl":    "0.75rem",
        "2xl":   "1.5rem",
        "full":  "9999px",
      },

      /* ── Elevation (DESIGN.md §4 — tonal layering, no heavy shadows) ── */
      boxShadow: {
        "soft":    "0 12px 32px rgba(25, 28, 26, 0.06)",
        "lifted":  "0 24px 48px -12px rgba(25, 28, 26, 0.12)",
      },
    },
  },
};
