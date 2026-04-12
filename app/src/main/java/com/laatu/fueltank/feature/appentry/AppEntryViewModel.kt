package com.laatu.fueltank.feature.appentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laatu.fueltank.domain.usecase.ObservePrimaryCarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    observePrimaryCarUseCase: ObservePrimaryCarUseCase,
) : ViewModel() {
    val uiState: StateFlow<AppEntryUiState> = observePrimaryCarUseCase()
        .map { car ->
            when {
                car == null -> AppEntryUiState.NeedsCarSetup
                else -> AppEntryUiState.Ready
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppEntryUiState.Loading,
        )
}

sealed interface AppEntryUiState {
    data object Loading : AppEntryUiState
    data object NeedsCarSetup : AppEntryUiState
    data object Ready : AppEntryUiState
}
