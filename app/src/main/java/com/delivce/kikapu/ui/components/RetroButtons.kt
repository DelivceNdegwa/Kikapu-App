package com.delivce.kikapu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame

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
