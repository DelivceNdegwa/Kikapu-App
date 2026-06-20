package com.delivce.kikapu.ui.foundation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object RetroTheme {
    val BorderColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.outline

    val SurfaceColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.surface

    val BackgroundColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.background

    val TextColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.onSurface

    val ShadowColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.outline
}