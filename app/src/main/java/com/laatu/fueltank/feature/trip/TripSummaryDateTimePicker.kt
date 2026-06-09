package com.laatu.fueltank.feature.trip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laatu.fueltank.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun DateTimePickerButton(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    allowClear: Boolean = false,
) {
    val context = LocalContext.current
    val displayedValue = value.toLocalDateTimeOrNull()
        ?.format(dialogDisplayDateTimeFormatter)
        ?: stringResource(R.string.trip_time_not_set)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Surface(
            onClick = {
                val current = value.toLocalDateTimeOrNull() ?: LocalDateTime.now()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                onValueChange(
                                    LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                                        .format(dialogDateTimeFormatter),
                                )
                            },
                            current.hour,
                            current.minute,
                            true,
                        ).show()
                    },
                    current.year,
                    current.monthValue - 1,
                    current.dayOfMonth,
                ).show()
            },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayedValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.trip_time_pick),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (allowClear && value.isNotBlank()) {
            TextButton(
                onClick = { onValueChange("") },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.trip_time_clear))
            }
        }
    }
}

private fun String.toLocalDateTimeOrNull(): LocalDateTime? {
    return try {
        LocalDateTime.parse(trim(), dialogDateTimeFormatter)
    } catch (error: DateTimeParseException) {
        null
    }
}

private val dialogDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val dialogDisplayDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
