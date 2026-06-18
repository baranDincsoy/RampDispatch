package com.barandincsoy.rampdispatch.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Central spacing scale. Using a fixed scale (4-8-12-16-24) instead of
 * arbitrary values keeps every screen visually consistent and makes
 * spacing decisions trivial: pick the nearest step, never invent a number.
 */
object Dimens {
    val SpacingXs = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 12.dp
    val SpacingL = 16.dp
    val SpacingXl = 24.dp

    val CardCornerRadius = 12.dp
    val ChipCornerRadius = 8.dp
    val StatusStripeWidth = 4.dp   // colored edge strip on order cards
    val MinTouchTarget = 48.dp     // accessibility: minimum tappable size
}