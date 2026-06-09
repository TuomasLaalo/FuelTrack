package com.laatu.fueltank.feature.carsetup

import com.google.common.truth.Truth.assertThat
import com.laatu.fueltank.MainDispatcherRule
import com.laatu.fueltank.TestStringResourceProvider
import com.laatu.fueltank.domain.model.Car
import com.laatu.fueltank.domain.repository.CarRepository
import com.laatu.fueltank.domain.usecase.CreateCarUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CarSetupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submitWithInvalidFields_setsLocalizedErrorsAndDoesNotSave() {
        val carRepository = FakeCarRepository()
        val viewModel = viewModel(carRepository)

        viewModel.submit()

        val state = viewModel.uiState.value
        assertThat(state.nameError).isEqualTo("Enter car name")
        assertThat(state.tankSizeError).isEqualTo("Enter tank size")
        assertThat(state.odometerError).isEqualTo("Enter odometer")
        assertThat(state.isSaving).isFalse()
        assertThat(carRepository.savedCars).isEmpty()
    }

    @Test
    fun submitWithValidFields_savesTrimmedCarAndEmitsSavedEvent() = runTest {
        val carRepository = FakeCarRepository()
        val viewModel = viewModel(carRepository)
        val savedEvent = async { viewModel.eventFlow.first() }

        viewModel.onNameChange("  Corolla  ")
        viewModel.onTankSizeChange("55.5")
        viewModel.onInitialOdometerChange("123456")
        viewModel.submit()
        advanceUntilIdle()

        val savedCar = carRepository.savedCars.single()
        assertThat(savedCar.name).isEqualTo("Corolla")
        assertThat(savedCar.tankSizeMilliliters).isEqualTo(55_500)
        assertThat(savedCar.initialOdometerKm).isEqualTo(123_456)
        assertThat(viewModel.uiState.value.isSaving).isFalse()
        assertThat(savedEvent.await()).isEqualTo(CarSetupEvent.CarSaved)
    }

    private fun viewModel(carRepository: FakeCarRepository): CarSetupViewModel =
        CarSetupViewModel(
            createCarUseCase = CreateCarUseCase(carRepository),
            stringResourceProvider = TestStringResourceProvider(),
        )
}

private class FakeCarRepository : CarRepository {
    private val carState = MutableStateFlow<Car?>(null)
    val savedCars = mutableListOf<Car>()

    override fun observePrimaryCar(): Flow<Car?> = carState.asStateFlow()

    override suspend fun getPrimaryCar(): Car? = carState.value

    override suspend fun upsertCar(car: Car): Long {
        savedCars += car
        carState.value = car
        return car.id
    }
}
