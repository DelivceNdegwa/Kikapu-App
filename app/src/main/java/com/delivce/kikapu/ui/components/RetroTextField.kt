package com.delivce.kikapu.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroShadow

@Composable
fun RetroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "", // Made optional to help placeholder visibility
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Column(modifier = modifier) {
        // If a label is provided, we show it above the field to allow the placeholder to be seen inside
//        label?.let {
//            Text(
//                text = it,
//                style = MaterialTheme.typography.labelMedium,
//                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
//                color = if (isError) MaterialTheme.colorScheme.error else RetroTheme.TextColor
//            )
//        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.retroShadow(
                shadowColor = RetroTheme.ShadowColor,
                shape = shape,
                offset = 2.dp
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = RetroTheme.TextColor.copy(alpha = 0.5f)
                )
            },
            label = {
                Text(
                text = label,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
                    color = if (isError) MaterialTheme.colorScheme.error else RetroTheme.TextColor
                )
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            readOnly = readOnly,
            enabled = enabled,
            isError = isError,
            shape = shape,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                // This removes the line at the bottom
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,

                // Container and text colors
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = RetroTheme.BorderColor,
                focusedTextColor = RetroTheme.TextColor,
                unfocusedTextColor = RetroTheme.TextColor
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RetroTextFieldPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        var email by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }

        RetroTextField(
            label = "Email",
            value = email,
            placeholder = "myemail@example.com",
            modifier = Modifier.padding(bottom = 16.dp),
            onValueChange = { email = it }
        )
        RetroTextField(
            label = "Preferred Name",
            value = name,
            placeholder = "Bruce Wayne",
            onValueChange = { name = it }
        )
    }
}