package com.delivce.kikapu.ui.components

import RetroCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color
) {

    RetroCard(
        backgroundColor = backgroundColor
    ) {

        Icon(icon, null)

        Text(title)

        Text(
            value,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(subtitle)
    }
}