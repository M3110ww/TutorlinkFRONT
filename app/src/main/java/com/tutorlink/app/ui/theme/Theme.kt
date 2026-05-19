package com.tutorlink.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MainPurple,
    secondary = LightPurple,
    tertiary = SoftGreen,
    background = DarkPurple,
    surface = DarkPurple,
    onPrimary = SurfaceWhite,
    onSecondary = DarkPurple,
    onTertiary = DarkPurple,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = MainPurple,
    secondary = LightPurple,
    tertiary = SoftGreen,
    background = BackgroundWhite,
    surface = SurfaceWhite,
    onPrimary = SurfaceWhite,
    onSecondary = DarkPurple,
    onTertiary = TextGreen,
    onBackground = DarkGrey,
    onSurface = DarkGrey,
    surfaceVariant = ExtraLightPurple
)

@Composable
fun TutorLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
