package com.laatu.fueltank.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    isError: Boolean = false,
    filledContainerColor: Color? = null,
    filledTextColor: Color? = null,
    filledLabelColor: Color? = null,
    filledPlaceholderColor: Color? = null,
    filledSupportingColor: Color? = null,
    filledCursorColor: Color? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    supportingText: String? = null,
) {
    val labelContent: @Composable () -> Unit = { Text(text = label) }
    val placeholderContent = placeholder?.let { text -> @Composable { Text(text = text) } }
    val supportingContent = supportingText?.let { text -> @Composable { Text(text = text) } }

    if (filled) {
        val containerColor = filledContainerColor ?: MaterialTheme.colorScheme.surfaceContainerLow
        val textColor = filledTextColor ?: MaterialTheme.colorScheme.onSurface
        val labelColor = filledLabelColor ?: MaterialTheme.colorScheme.onSurfaceVariant
        val placeholderColor = filledPlaceholderColor ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        val supportingColor = filledSupportingColor ?: MaterialTheme.colorScheme.onSurfaceVariant
        val cursorColor = filledCursorColor ?: MaterialTheme.colorScheme.primary
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            label = labelContent,
            placeholder = placeholderContent,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            supportingText = supportingContent,
            isError = isError,
            shape = MaterialTheme.shapes.large,
            colors = TextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                disabledTextColor = textColor.copy(alpha = 0.55f),
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledContainerColor = containerColor,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                focusedLabelColor = labelColor,
                unfocusedLabelColor = labelColor,
                disabledLabelColor = labelColor.copy(alpha = 0.55f),
                focusedPlaceholderColor = placeholderColor,
                unfocusedPlaceholderColor = placeholderColor,
                disabledPlaceholderColor = placeholderColor.copy(alpha = 0.55f),
                focusedSupportingTextColor = supportingColor,
                unfocusedSupportingTextColor = supportingColor,
                disabledSupportingTextColor = supportingColor.copy(alpha = 0.55f),
                cursorColor = cursorColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            label = labelContent,
            placeholder = placeholderContent,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            supportingText = supportingContent,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            ),
        )
    }
}
