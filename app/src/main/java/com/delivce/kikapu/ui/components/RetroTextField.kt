package com.delivce.kikapu.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroShadow

@Composable
fun RetroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    shape: Shape = RoundedCornerShape(RetroDefaults.CornerRadius)
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val borderColor = RetroTheme.BorderColor
    val shadowColor = RetroTheme.ShadowColor
    // Most call sites only ever set `placeholder` and mean it as a label ("Item name",
    // "Budget (KES)"); a few (login/signup) pass a real label plus an example value as the
    // placeholder ("Email" / "youremail@example.com"). Falling back keeps both working without
    // forcing every call site to pass both params.
    val titleText = label.ifBlank { placeholder }
    val innerPlaceholder = if (label.isNotBlank()) placeholder else ""

    Column(modifier = modifier) {
        if (titleText.isNotBlank()) {
            Text(
                text = titleText.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .retroShadow(
                    shadowColor = shadowColor,
                    shape = shape,
                    offset = 3.dp
                )
                .border(
                    width = RetroDefaults.BorderWidth,
                    color = borderColor,
                    shape = shape
                ),
            placeholder = if (innerPlaceholder.isNotBlank()) {
                {
                    Text(
                        text = innerPlaceholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = RetroTheme.TextColor.copy(alpha = 0.4f)
                    )
                }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                { Icon(imageVector = leadingIcon, contentDescription = null, tint = RetroTheme.TextColor.copy(alpha = 0.5f)) }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                }
            } else null,
            keyboardOptions = keyboardOptions,
            readOnly = readOnly,
            enabled = enabled,
            isError = isError,
            shape = shape,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                unfocusedContainerColor = RetroTheme.BackgroundColor,
                focusedContainerColor = RetroTheme.BackgroundColor,
                errorContainerColor = RetroTheme.BackgroundColor,
                cursorColor = RetroTheme.BorderColor,
                focusedTextColor = RetroTheme.TextColor,
                unfocusedTextColor = RetroTheme.TextColor
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}
