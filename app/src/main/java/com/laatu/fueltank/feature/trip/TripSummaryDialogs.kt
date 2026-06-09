package com.laatu.fueltank.feature.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R
import com.laatu.fueltank.core.ui.UiTestTags
import com.laatu.fueltank.core.ui.component.DestructiveDialogActionButtons
import com.laatu.fueltank.core.ui.component.DialogActionButtons
import com.laatu.fueltank.core.ui.component.SaveDeleteCancelDialogActionButtons

@Composable
fun RenameTripDialog(
    titleValue: String,
    onTitleChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.trip_summary_rename_title)) },
        text = {
            OutlinedTextField(
                value = titleValue,
                onValueChange = onTitleChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text(stringResource(R.string.trip_summary_trip_title_label)) },
            )
        },
        confirmButton = {
            DialogActionButtons(
                primaryLabel = stringResource(R.string.common_save),
                onPrimaryClick = onSaveClick,
                onCancelClick = onDismiss,
            )
        },
        dismissButton = {},
    )
}

@Composable
fun ConfirmEndTripDialog(
    onContinueClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.trip_summary_end_title)) },
        text = { Text(text = stringResource(R.string.trip_summary_end_message)) },
        confirmButton = {
            DialogActionButtons(
                primaryLabel = stringResource(R.string.trip_summary_continue),
                primaryTestTag = UiTestTags.DialogPrimaryAction,
                onPrimaryClick = onContinueClick,
                onCancelClick = onDismiss,
            )
        },
        dismissButton = {},
    )
}

@Composable
fun EditTripTimesDialog(
    startValue: String,
    endValue: String,
    allowClearEnd: Boolean,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.trip_time_edit_trip_times)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DateTimePickerButton(
                    label = stringResource(R.string.trip_time_start_time),
                    value = startValue,
                    onValueChange = onStartChange,
                )
                DateTimePickerButton(
                    label = stringResource(R.string.trip_time_end_time),
                    value = endValue,
                    onValueChange = onEndChange,
                    allowClear = allowClearEnd,
                )
            }
        },
        confirmButton = {
            DialogActionButtons(
                primaryLabel = stringResource(R.string.common_save),
                onPrimaryClick = onSaveClick,
                onCancelClick = onDismiss,
            )
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    )
}

@Composable
fun EditBreakDialog(
    target: TripBreakUiModel?,
    startValue: String,
    endValue: String,
    noteValue: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (target == null) {
                        stringResource(R.string.trip_time_add_break)
                    } else {
                        stringResource(R.string.trip_time_edit_break)
                    },
                )
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.common_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DateTimePickerButton(
                    label = stringResource(R.string.trip_time_start_time),
                    value = startValue,
                    onValueChange = onStartChange,
                )
                DateTimePickerButton(
                    label = stringResource(R.string.trip_time_end_time),
                    value = endValue,
                    onValueChange = onEndChange,
                    allowClear = true,
                )
                OutlinedTextField(
                    value = noteValue,
                    onValueChange = onNoteChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text(stringResource(R.string.trip_time_break_note)) },
                    modifier = Modifier.testTag(UiTestTags.TripBreakNote),
                )
            }
        },
        confirmButton = {
            SaveDeleteCancelDialogActionButtons(
                showDelete = target != null,
                onSaveClick = onSaveClick,
                onDeleteClick = onDeleteClick,
                onCancelClick = onDismiss,
                saveTestTag = UiTestTags.TripBreakSave,
                deleteTestTag = UiTestTags.TripBreakDelete,
                showCancel = false,
            )
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    )
}

@Composable
fun DeleteBreakDialog(
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.trip_time_delete_break_title)) },
        text = { Text(text = stringResource(R.string.trip_time_delete_break_message)) },
        confirmButton = {
            DestructiveDialogActionButtons(
                destructiveLabel = stringResource(R.string.common_delete),
                destructiveTestTag = UiTestTags.DialogPrimaryAction,
                onDestructiveClick = onDeleteClick,
                onCancelClick = onDismiss,
            )
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    )
}
