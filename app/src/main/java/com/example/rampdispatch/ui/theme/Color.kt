package com.example.rampdispatch.ui.theme

import androidx.compose.ui.graphics.Color

// ---------- Material core palette (dark-first design) ----------
// A ramp app is used outdoors, often at night; dark UI reduces glare
// and battery drain on OLED screens. So we design dark as the default.

val SurfaceDark = Color(0xFF12161C)        // app background
val SurfaceContainerDark = Color(0xFF1C222B) // cards sit slightly above background
val OnSurfaceDark = Color(0xFFE4E8EE)      // primary text
val OnSurfaceMutedDark = Color(0xFF93A0B0) // secondary text (labels, captions)

val PrimaryBlue = Color(0xFF4F8EF7)        // brand/action color: buttons, highlights
val OnPrimary = Color(0xFFFFFFFF)

// ---------- Domain colors: order status ----------
// These are NOT Material roles; they carry domain meaning.
// Chosen to be distinguishable at a glance in sunlight and for
// common color-vision deficiencies (blue/amber/green/red split).

val StatusPending = Color(0xFF8B97A5)      // neutral gray: nothing happened yet
val StatusAssigned = Color(0xFF4F8EF7)     // blue: planned, someone owns it
val StatusInProgress = Color(0xFFF5A623)   // amber: active work, attention here
val StatusCompleted = Color(0xFF34C77B)    // green: done
val StatusOverdue = Color(0xFFE5484D)      // red: derived alert state

// Soft "container" versions for chip/badge backgrounds —
// full-strength colors are for text/icons, soft ones for fills.
val StatusPendingContainer = Color(0x338B97A5)
val StatusAssignedContainer = Color(0x334F8EF7)
val StatusInProgressContainer = Color(0x33F5A623)
val StatusCompletedContainer = Color(0x3334C77B)
val StatusOverdueContainer = Color(0x33E5484D)