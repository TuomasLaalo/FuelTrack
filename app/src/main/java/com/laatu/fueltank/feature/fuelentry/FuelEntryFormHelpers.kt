package com.laatu.fueltank.feature.fuelentry

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal fun MutableStateFlow<AddFuelEntryFormState>.updateField(
    transform: AddFuelEntryFormState.() -> AddFuelEntryFormState,
) {
    update { current -> current.transform() }
}

internal fun AddFuelEntryFormState.toComputedTotalPriceLabel(): String {
    val liters = liters.parseLocalizedDecimal()
    val pricePerLiter = pricePerLiter.parseLocalizedDecimal()
    val total = if (liters != null && pricePerLiter != null && liters >= 0.0 && pricePerLiter >= 0.0) {
        liters * pricePerLiter
    } else {
        0.0
    }
    return "${moneyFormatter.format(total)} EUR"
}

internal fun Double.toCents(): Long = (this * 100).roundToLong()

internal fun Double.toMicrosPerLiter(): Long = (this * 1_000_000).roundToLong()

internal fun String.parseLocalizedDecimal(): Double? = replace(',', '.').toDoubleOrNull()

private val moneyFormatter = DecimalFormat(
    "0.00",
    DecimalFormatSymbols.getInstance(Locale.getDefault()),
)
