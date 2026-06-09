package com.laatu.fueltank.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.laatu.fueltank.MainActivity
import com.laatu.fueltank.core.ui.UiTestTags
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FuelTankAppFlowTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun resetAppState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("fuel_tank.db")
        context.getSharedPreferences("fueltrack_settings", 0)
            .edit()
            .clear()
            .putString("app_language", "en")
            .apply()
    }

    @After
    fun closeActivity() {
        scenario?.close()
    }

    @Test
    fun coreAppFlowCoversFuelCrudTripBreaksAndLocalization() {
        scenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.createCarThroughOnboarding("Fuel CRUD Car")

        composeRule.onNodeWithTag(UiTestTags.DashboardAddRefill)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.AddFuelEntrySheet)
        composeRule.onNodeWithTag(UiTestTags.AddFuelEntrySheet)
            .assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.FuelEntryTripMeter)
            .performTextInput("100")
        composeRule.onNodeWithTag(UiTestTags.FuelEntryQuantity)
            .performTextInput("42.5")
        composeRule.onNodeWithTag(UiTestTags.FuelEntryPricePerUnit)
            .performTextInput("1.85")
        composeRule.onNodeWithTag(UiTestTags.FuelEntryStation)
            .performTextInput("Shell UI Test")
        composeRule.onNodeWithTag(UiTestTags.FuelEntrySave)
            .performScrollTo()
            .performClick()

        composeRule.waitUntilExists(UiTestTags.DashboardViewAllRefuels)
        composeRule.onNodeWithTag(UiTestTags.DashboardViewAllRefuels)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.FuelHistoryEntry)
        composeRule.onNodeWithText("Shell UI Test")
            .assertIsDisplayed()

        composeRule.onNodeWithTag(UiTestTags.FuelHistoryEntryMoreActions)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.FuelHistoryEntryEdit)
        composeRule.onNodeWithTag(UiTestTags.FuelHistoryEntryEdit)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.FuelEntryStation)
        composeRule.onNodeWithTag(UiTestTags.FuelEntryStation)
            .performTextClearance()
        composeRule.onNodeWithTag(UiTestTags.FuelEntryStation)
            .performTextInput("Neste Edited")
        composeRule.onNodeWithTag(UiTestTags.FuelEntrySave)
            .performScrollTo()
            .performClick()

        composeRule.waitUntilTextExists("Neste Edited")
        composeRule.onNodeWithText("Neste Edited")
            .assertIsDisplayed()

        composeRule.onNodeWithTag(UiTestTags.FuelHistoryEntryMoreActions)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.FuelHistoryEntryEdit)
        composeRule.onNodeWithTag(UiTestTags.FuelHistoryEntryEdit)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.FuelEntryDelete)
        composeRule.onNodeWithTag(UiTestTags.FuelEntryDelete)
            .performScrollTo()
            .performClick()
        composeRule.waitUntilExists(UiTestTags.DialogPrimaryAction)
        composeRule.onNodeWithTag(UiTestTags.DialogPrimaryAction)
            .performClick()

        composeRule.waitUntilTextDoesNotExist("Neste Edited")

        composeRule.onNodeWithTag(UiTestTags.FuelHistoryBack)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.DashboardStartTrip)
        composeRule.onNodeWithTag(UiTestTags.DashboardStartTrip)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.StartTripTitle)
        composeRule.onNodeWithTag(UiTestTags.StartTripTitle)
            .performTextInput("UI Road Trip")
        composeRule.onNodeWithTag(UiTestTags.StartTripLocation)
            .performTextInput("Helsinki UI Start")
        composeRule.onNodeWithTag(UiTestTags.StartTripSave)
            .performScrollTo()
            .performClick()

        composeRule.waitUntilExists(UiTestTags.TripSummaryScreen)
        composeRule.onNodeWithText("UI Road Trip")
            .assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.TripTimeStartBreak)
            .performScrollTo()
            .performClick()
        composeRule.waitUntilExists(UiTestTags.TripTimeEndBreak)
        composeRule.onNodeWithTag(UiTestTags.TripTimeEndBreak)
            .performScrollTo()
            .performClick()
        composeRule.waitUntilExists(UiTestTags.TripTimelineBreak)
        composeRule.onNodeWithTag(UiTestTags.TripTimelineBreak)
            .performScrollTo()
            .performClick()
        composeRule.waitUntilExists(UiTestTags.TripBreakNote)
        composeRule.onNodeWithTag(UiTestTags.TripBreakNote)
            .performTextInput("Coffee break UI")
        composeRule.onNodeWithTag(UiTestTags.TripBreakSave)
            .performClick()
        composeRule.waitUntilDoesNotExist(UiTestTags.TripBreakNote)
        composeRule.onNodeWithTag(UiTestTags.TripTimelineBreak)
            .performScrollTo()
            .performClick()
        composeRule.waitUntilExists(UiTestTags.TripBreakDelete)
        composeRule.onNodeWithTag(UiTestTags.TripBreakDelete)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.DialogPrimaryAction)
        composeRule.onNodeWithTag(UiTestTags.DialogPrimaryAction)
            .performClick()
        composeRule.waitUntilDoesNotExist(UiTestTags.TripTimelineBreak)

        composeRule.onNodeWithTag(UiTestTags.TripSummaryEndTrip)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.DialogPrimaryAction)
        composeRule.onNodeWithTag(UiTestTags.DialogPrimaryAction)
            .performClick()

        composeRule.waitUntilExists(UiTestTags.EndTripSheet)
        composeRule.onNodeWithTag(UiTestTags.EndTripTripMeter)
            .performTextInput("240")
        composeRule.onNodeWithTag(UiTestTags.EndTripLocation)
            .performTextInput("Tampere UI End")
        composeRule.onNodeWithTag(UiTestTags.EndTripSave)
            .performScrollTo()
            .performClick()

        composeRule.waitUntilTextExists("Tampere UI End")
        composeRule.waitUntilExists(UiTestTags.TripSummaryScreen)

        composeRule.onNodeWithTag(UiTestTags.TripSummaryBack)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.DashboardMenu)
        composeRule.onNodeWithTag(UiTestTags.DashboardMenu)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.DashboardMenuLanguage)
        composeRule.onNodeWithTag(UiTestTags.DashboardMenuLanguage)
            .performClick()
        composeRule.waitUntilExists(UiTestTags.DashboardLanguageFinnish)
        composeRule.onNodeWithTag(UiTestTags.DashboardLanguageFinnish)
            .performClick()
        composeRule.waitUntilTextExists("Keskikulutus")
        composeRule.onNodeWithText("Keskikulutus")
            .assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.createCarThroughOnboarding(
        carName: String,
    ) {
        waitUntilExists(UiTestTags.CarSetupName)

        onNodeWithTag(UiTestTags.CarSetupName)
            .performTextInput(carName)
        onNodeWithTag(UiTestTags.CarSetupTankSize)
            .performTextInput("55")
        onNodeWithTag(UiTestTags.CarSetupInitialOdometer)
            .performTextInput("123456")
        onNodeWithTag(UiTestTags.CarSetupSave)
            .performClick()

        waitUntilExists(UiTestTags.DashboardAddRefill)
        onNodeWithText(carName)
            .assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilExists(
        tag: String,
        timeoutMillis: Long = 5_000,
    ) {
        waitUntil(timeoutMillis) {
            onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilDoesNotExist(
        tag: String,
        timeoutMillis: Long = 5_000,
    ) {
        waitUntil(timeoutMillis) {
            onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilTextExists(
        text: String,
        timeoutMillis: Long = 5_000,
    ) {
        waitUntil(timeoutMillis) {
            onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilTextDoesNotExist(
        text: String,
        timeoutMillis: Long = 5_000,
    ) {
        waitUntil(timeoutMillis) {
            onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()
        }
    }
}
