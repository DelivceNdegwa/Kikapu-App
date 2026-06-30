package com.delivce.kikapu.ui.foundation

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp

fun Modifier.retroBorder(
    borderColor: Color,
    shape: Shape = RoundedCornerShape(RetroDefaults.CornerRadius),
    borderWidth: Dp = RetroDefaults.BorderWidth
): Modifier {
    return border(
        width = borderWidth,
        color = borderColor,
        shape = shape
    )
}

fun Modifier.retroShadow(
    shadowColor: Color,
    shape: Shape = RoundedCornerShape(RetroDefaults.CornerRadius),
    offset: Dp = RetroDefaults.ShadowOffset
): Modifier = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    translate(left = offset.toPx(), top = offset.toPx()) {
        drawOutline(outline, color = shadowColor)
    }
}

fun Modifier.retroFrame(
    borderColor: Color,
    shadowColor: Color,
    shape: Shape = RoundedCornerShape(RetroDefaults.CornerRadius),
    thickness: Dp = RetroDefaults.BorderWidth
): Modifier {
    return this
        .retroShadow(shadowColor, shape, thickness)
        .retroBorder(borderColor, shape, thickness)
}
