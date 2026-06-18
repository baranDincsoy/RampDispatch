package com.barandincsoy.rampdispatch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark-only theme for the MVP. Ramp work happens outdoors at night;
 * a dark UI reduces glare. Dynamic color is intentionally disabled:
 * status colors carry meaning and must never shift with the wallpaper.
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimary,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceMutedDark
)

@Composable
fun RampDispatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,   // Type.kt'deki hazır tanım
        content = content
    )
}