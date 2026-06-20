package com.delivce.kikapu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Amber,
    onPrimary = AppColors.Earth,

    secondary = AppColors.Earth,
    onSecondary = AppColors.White,

    tertiary = AppColors.Coral,
    onTertiary = AppColors.White,

    background = AppColors.Cream,
    onBackground = AppColors.Earth,

    surface = AppColors.White,
    onSurface = AppColors.Earth,

    outline = AppColors.Border
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Amber,
    onPrimary = AppColors.Earth,

    secondary = AppColors.Earth,
    onSecondary = AppColors.White,

    tertiary = AppColors.Coral,
    onTertiary = AppColors.White,

    background = AppColors.Earth,
    onBackground = AppColors.Cream,

    surface = AppColors.DarkSurface,
    onSurface = AppColors.Cream,

    outline = AppColors.Cream
)

@Composable
fun KikapuTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
