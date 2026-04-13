package com.laatu.fueltank.feature.fuelentry

import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
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

internal fun AddFuelEntryFormState.toComputedTotalPriceLabel(
    stringResourceProvider: StringResourceProvider,
): String {
    val liters = liters.parseLocalizedDecimal()
    val pricePerLiter = pricePerLiter.parseLocalizedDecimal()
    val total = if (liters != null && pricePerLiter != null && liters >= 0.0 && pricePerLiter >= 0.0) {
        liters * pricePerLiter
    } else {
        0.0
    }
    return stringResourceProvider.get(R.string.unit_euro_symbol, moneyFormatter.format(total))
}

internal fun AddFuelEntryFormState.toComputedTotalPriceLabel(): String =
    "${moneyFormatter.format((liters.parseLocalizedDecimal() ?: 0.0) * (pricePerLiter.parseLocalizedDecimal() ?: 0.0))} €"

internal fun Double.toCents(): Long = (this * 100).roundToLong()

internal fun Double.toMicrosPerLiter(): Long = (this * 1_000_000).roundToLong()

internal fun String.parseLocalizedDecimal(): Double? = replace(',', '.').toDoubleOrNull()

private val moneyFormatter = DecimalFormat(
    "0.00",
    DecimalFormatSymbols.getInstance(Locale.getDefault()),
)
