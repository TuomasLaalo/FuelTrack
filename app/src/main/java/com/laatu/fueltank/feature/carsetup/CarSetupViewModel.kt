package com.laatu.fueltank.feature.carsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.R
import com.laatu.fueltank.core.localization.StringResourceProvider
import com.laatu.fueltank.domain.usecase.CreateCarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CarSetupViewModel @Inject constructor(
    private val createCarUseCase: CreateCarUseCase,
    private val stringResourceProvider: StringResourceProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CarSetupUiState())
    val uiState: StateFlow<CarSetupUiState> = _uiState.asStateFlow()

    private val events = Channel<CarSetupEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onTankSizeChange(value: String) {
        _uiState.update { it.copy(tankSizeLiters = value, tankSizeError = null) }
    }

    fun onInitialOdometerChange(value: String) {
        _uiState.update { it.copy(initialOdometerKm = value, odometerError = null) }
    }

    fun submit() {
        val current = _uiState.value
        val validation = validate(current)
        if (validation != null) {
            _uiState.update { validation }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val tankSizeMilliliters = (current.tankSizeLiters.toDouble() * 1000).toLong()
            createCarUseCase(
                name = current.name.trim(),
                tankSizeMilliliters = tankSizeMilliliters,
                initialOdometerKm = current.initialOdometerKm.toLong(),
            )
            _uiState.update { it.copy(isSaving = false) }
            events.send(CarSetupEvent.CarSaved)
        }
    }

    private fun validate(state: CarSetupUiState): CarSetupUiState? {
        var hasError = false
        var nameError: String? = null
        var tankSizeError: String? = null
        var odometerError: String? = null

        if (state.name.trim().isEmpty()) {
            hasError = true
            nameError = stringResourceProvider.get(R.string.car_setup_error_car_name)
        }

        val tankSize = state.tankSizeLiters.toDoubleOrNull()
        if (tankSize == null || tankSize <= 0) {
            hasError = true
            tankSizeError = stringResourceProvider.get(R.string.car_setup_error_tank_size)
        }

        val odometer = state.initialOdometerKm.toLongOrNull()
        if (odometer == null || odometer < 0) {
            hasError = true
            odometerError = stringResourceProvider.get(R.string.car_setup_error_odometer)
        }

        return if (hasError) {
            state.copy(
                nameError = nameError,
                tankSizeError = tankSizeError,
                odometerError = odometerError,
                isSaving = false,
            )
        } else {
            null
        }
    }
}

data class CarSetupUiState(
    val name: String = "",
    val tankSizeLiters: String = "",
    val initialOdometerKm: String = "",
    val nameError: String? = null,
    val tankSizeError: String? = null,
    val odometerError: String? = null,
    val isSaving: Boolean = false,
)

sealed interface CarSetupEvent {
    data object CarSaved : CarSetupEvent
}
