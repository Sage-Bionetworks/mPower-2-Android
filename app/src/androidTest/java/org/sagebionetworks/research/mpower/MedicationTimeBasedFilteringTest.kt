package org.sagebionetworks.research.mpower

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.collect.ImmutableSet
import com.google.common.collect.RangeSet
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView
import org.sagebionetworks.research.mpower.tracking.model.TrackingSubstepInfo
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingSchedule
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingTitle
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.logs.DosageItem
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationTimestamp
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import java.util.*

@RunWith(AndroidJUnit4::class)
class MedicationTimeBasedFilteringTest {

    /**
     * The Medications in this test have the following schedules: All dosages are "100mg" Med1 - Anytime Med2 -
     * Monday, Tuesday, Friday at 6:00AM Med3 - Everyday at 1:00PM - Saturday, Sunday at 6:00PM Med4 - Everyday at
     * 1:00AM Med5 - Thursday at 5:30PM - Thursday at 9:00PM - Friday at 2:30AM
     */
    companion object {

        val DOSAGE = "100mg"
        val ITEM_1 = TrackingItem.builder()
                .setIdentifier("Med1")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_1: MedicationLog

        init {
            CONFIG_1 = MedicationLog.builder()
                    .setIdentifier("Med1")
                    .setDosageItems(mutableListOf(DosageItem(DOSAGE, DosageItem.dailySet.toMutableSet(), mutableSetOf())))
                    .build()
        }

        val ITEM_2 = TrackingItem.builder()
                .setIdentifier("Med2")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_2: MedicationLog

        init {
            val timeStamp = MedicationTimestamp.builder().setTimeOfDay("06:00")!!.build()
            val days = setOf(2, 3, 6)

            CONFIG_2 = MedicationLog.builder()
                    .setIdentifier("Med2")
                    .setDosageItems(mutableListOf(DosageItem(DOSAGE, days.toMutableSet(), mutableSetOf(timeStamp))))
                    .build();
        }

        val ITEM_3 = TrackingItem.builder()
                .setIdentifier("Med3")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_3: MedicationLog

        init {
            val timeStamp1 = MedicationTimestamp.builder().setTimeOfDay("13:00")!!.build()
            val timeStamp2 = MedicationTimestamp.builder().setTimeOfDay("18:00")!!.build()
            val days = setOf(7, 1)

            CONFIG_3 = MedicationLog.builder()
                    .setIdentifier("Med3")
                    .setDosageItems(mutableListOf(DosageItem(DOSAGE, days.toMutableSet(), mutableSetOf(timeStamp1, timeStamp2))))
                    .build()
        }

        val ITEM_4 = TrackingItem.builder()
                .setIdentifier("Med4")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_4: MedicationLog

        init {
            val timeStamp = MedicationTimestamp.builder().setTimeOfDay("01:00")!!.build()

            CONFIG_4 = MedicationLog.builder()
                    .setIdentifier("Med4")
                    .setDosageItems(mutableListOf(DosageItem(DOSAGE, DosageItem.dailySet.toMutableSet(), mutableSetOf(timeStamp))))
                    .build()
        }

        val ITEM_5 = TrackingItem.builder()
                .setIdentifier("Med5")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_5: MedicationLog

        init {
            val timeStamp1 = MedicationTimestamp.builder().setTimeOfDay("17:30")!!.build()
            val timeStamp2 = MedicationTimestamp.builder().setTimeOfDay("21:00")!!.build()
            val timeStamp3 = MedicationTimestamp.builder().setTimeOfDay("02:30")!!.build()
            val days = setOf(4)
//            val schedule1 = Schedule(LocalTime.NOON.plusHours(5).plusMinutes(30), setOf(4))
//            val schedule2 = Schedule(LocalTime.NOON.plusHours(9), setOf(4))
//            val schedule3 = Schedule(LocalTime.MIDNIGHT.plusHours(2).plusMinutes(30), setOf(5))

            CONFIG_5 = MedicationLog.builder()
                    .setIdentifier("Med5")
                    .setDosageItems(mutableListOf(DosageItem(DOSAGE, days.toMutableSet(), mutableSetOf(timeStamp1, timeStamp2, timeStamp3))))
                    .build()
        }

        val STEP: TrackingStep
        val STEP_VIEW: TrackingStepView

        init {
            val remindersInfo = TrackingSubstepInfo.builder()
                    .setTitle("title")
                    .setText("text")
                    .build()
            val selectionInfo = TrackingSubstepInfo.builder()
                    .setTitle("title")
                    .setText("text")
                    .build()
            STEP = TrackingStep.builder()
                    .setIdentifier("TestStep")
                    .setItems(ImmutableSet.of(ITEM_1, ITEM_2, ITEM_3, ITEM_4, ITEM_5))
                    .setSelectionInfo(selectionInfo)
                    .setRemindersInfo(remindersInfo)
                    .build()
            STEP_VIEW = TrackingStepView.fromTrackingStep(STEP, null)
        }
    }

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MedicationTrackingTaskViewModel

    @Before
    fun setupViewModel() {
        viewModel = MedicationTrackingTaskViewModel(InstrumentationRegistry.getInstrumentation().targetContext, STEP_VIEW, null)
        viewModel.addConfig(CONFIG_1)
        viewModel.addConfig(CONFIG_2)
        viewModel.addConfig(CONFIG_3)
        viewModel.addConfig(CONFIG_4)
        viewModel.addConfig(CONFIG_5)
    }

    private fun test_timeBlock(time: LocalTime, identifier: String) {
        val timeBlock: Pair<String, RangeSet<LocalTime>> = viewModel.getTimeBlock(time)
        assertEquals("Unexpected time block returned", identifier, timeBlock.first)
    }

    @Test
    fun test_timeBlock_1AM() {
        val time: LocalTime = LocalTime.MIDNIGHT.plusHours(1)
        test_timeBlock(time, "Night")
    }

    @Test
    fun test_timeBlock_6AM() {
        val time: LocalTime = LocalTime.MIDNIGHT.plusHours(6)
        test_timeBlock(time, "Morning")
    }

    @Test
    fun test_timeBlock_1PM() {
        val time: LocalTime = LocalTime.NOON.plusHours(1)
        test_timeBlock(time, "Afternoon")
    }

    @Test
    fun test_timeBlock_6PM() {
        val time: LocalTime = LocalTime.NOON.plusHours(6)
        test_timeBlock(time, "Evening")
    }

    @Test
    fun test_timeBlock_11PM() {
        val time: LocalTime = LocalTime.NOON.plusHours(11)
        test_timeBlock(time, "Night")
    }

    @Test
    fun test_CurrentConfigs_6AM_Tuesday() {
        // September 25th, 2018 is a Tuesday.
        val time: LocalDateTime = LocalDateTime.of(2018, Month.SEPTEMBER, 25, 6, 0)
        val timeBlock: Pair<String, RangeSet<LocalTime>> = viewModel.getTimeBlock(time.toLocalTime())
        val currentConfigs: List<MedicationLoggingItem> = viewModel.getCurrentTimeBlockMedications(timeBlock,
                time.toLocalDate())
        val expected: List<MedicationLoggingItem> = Arrays.asList(
                MedicationLoggingTitle("Med1 100mg"),
                MedicationLoggingSchedule(CONFIG_1, CONFIG_1.dosageItems[0], null),
                MedicationLoggingTitle("Med2 100mg"),
                MedicationLoggingSchedule(CONFIG_2, CONFIG_2.dosageItems[0], null)
        )

        assertEquals(currentConfigs.size, expected.size)

        assertTrue(currentConfigs[0] is MedicationLoggingTitle)
        assertEquals((currentConfigs[0] as MedicationLoggingTitle).title, "Med1 100mg")

        assertTrue(currentConfigs[1] is MedicationLoggingSchedule)
        assertEquals((currentConfigs[1] as MedicationLoggingSchedule).config, CONFIG_1)
        assertEquals((currentConfigs[1] as MedicationLoggingSchedule).dosageItem, CONFIG_1.dosageItems[0])
        assertFalse((currentConfigs[1] as MedicationLoggingSchedule).isLogged)

        assertTrue(currentConfigs[2] is MedicationLoggingTitle)
        assertEquals((currentConfigs[2] as MedicationLoggingTitle).title, "Med2 100mg")

        assertTrue(currentConfigs[3] is MedicationLoggingSchedule)
        assertEquals((currentConfigs[3] as MedicationLoggingSchedule).config, CONFIG_2)
        assertEquals((currentConfigs[3] as MedicationLoggingSchedule).dosageItem, CONFIG_2.dosageItems[0])
        assertFalse((currentConfigs[3] as MedicationLoggingSchedule).isLogged)
    }
}
