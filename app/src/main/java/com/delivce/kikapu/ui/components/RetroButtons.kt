package com.delivce.kikapu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SocialButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor,
                shape = RoundedCornerShape(16.dp),
                thickness = 1.5.dp
            )
            .background(RetroTheme.BackgroundColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun SocialIconPlaceholder(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}


/**
 * Coral-outlined button that briefly animates to a filled coral background on tap, then eases
 * back to outlined. Replaces the old solid brown/earthy buttons in the app.
 */
@Composable
fun AnimatedFillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = AppColors.Coral,
    icon: ImageVector? = null,
    height: Dp = 44.dp,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    val coroutineScope = rememberCoroutineScope()
    var filled by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (filled) color else RetroTheme.BackgroundColor,
        animationSpec = tween(220),
        label = "fillButtonBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (filled) Color.White else color,
        animationSpec = tween(220),
        label = "fillButtonContent"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .retroFrame(
                borderColor = color,
                shadowColor = RetroTheme.ShadowColor,
                shape = shape,
                thickness = 1.5.dp
            )
            .background(backgroundColor, shape)
            .clickable {
                filled = true
                onClick()
                coroutineScope.launch {
                    delay(240)
                    filled = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun NavigationButton(isBack: Boolean, onBackClick: () -> Unit){
    val navigationSymbol = if (isBack) "‹" else "›"
    Box(
        modifier = Modifier
            .size(40.dp)
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(RetroTheme.BackgroundColor, RoundedCornerShape(12.dp))
            .clickable { onBackClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = navigationSymbol,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.TextColor
        )
    }
}
