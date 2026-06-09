package com.laatu.fueltank.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    destructiveConfirm: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            DialogActionButtons(
                primaryLabel = confirmLabel,
                cancelLabel = dismissLabel,
                primaryDestructive = destructiveConfirm,
                onPrimaryClick = onConfirm,
                onCancelClick = onDismiss,
            )
        },
        dismissButton = {},
    )
}
