package com.laatu.fueltank.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R

@Composable
fun DialogActionButtons(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    cancelLabel: String? = null,
    primaryDestructive: Boolean = false,
    primaryTestTag: String? = null,
) {
    val resolvedCancelLabel = cancelLabel ?: stringResource(R.string.common_cancel)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(
            onClick = onPrimaryClick,
            modifier = primaryTestTag?.let { Modifier.testTag(it) } ?: Modifier,
        ) {
            Text(
                text = primaryLabel,
                color = if (primaryDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
        TextButton(onClick = onCancelClick) {
            Text(
                text = resolvedCancelLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun DestructiveDialogActionButtons(
    destructiveLabel: String,
    onDestructiveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    cancelLabel: String? = null,
    destructiveTestTag: String? = null,
) {
    DialogActionButtons(
        primaryLabel = destructiveLabel,
        cancelLabel = cancelLabel,
        primaryDestructive = true,
        primaryTestTag = destructiveTestTag,
        onPrimaryClick = onDestructiveClick,
        onCancelClick = onCancelClick,
        modifier = modifier,
    )
}

@Composable
fun SaveDeleteCancelDialogActionButtons(
    showDelete: Boolean,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    saveLabel: String? = null,
    deleteLabel: String? = null,
    cancelLabel: String? = null,
    saveTestTag: String? = null,
    deleteTestTag: String? = null,
    showCancel: Boolean = true,
) {
    val resolvedSaveLabel = saveLabel ?: stringResource(R.string.common_save)
    val resolvedDeleteLabel = deleteLabel ?: stringResource(R.string.common_delete)
    val resolvedCancelLabel = cancelLabel ?: stringResource(R.string.common_cancel)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(
            onClick = onSaveClick,
            modifier = saveTestTag?.let { Modifier.testTag(it) } ?: Modifier,
        ) {
            Text(text = resolvedSaveLabel)
        }
        if (showDelete) {
            TextButton(
                onClick = onDeleteClick,
                modifier = deleteTestTag?.let { Modifier.testTag(it) } ?: Modifier,
            ) {
                Text(
                    text = resolvedDeleteLabel,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (showCancel) {
            TextButton(onClick = onCancelClick) {
                Text(
                    text = resolvedCancelLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
