package org.sagebionetworks.research.mpower

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.common.collect.ImmutableSet
import com.google.common.collect.RangeSet
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.*
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView
import org.sagebionetworks.research.mpower.tracking.model.TrackingSubstepInfo
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingSchedule
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingTitle
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.configs.Schedule
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import java.util.Arrays
import java.util.Collections

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
        val CONFIG_1: MedicationConfig

        init {
            val schedule = Schedule("0")
            schedule.timeOfDay = null

            CONFIG_1 = MedicationConfig.builder()
                    .setIdentifier("Med1")
                    .setDosage(DOSAGE)
                    .setSchedules(Collections.singletonList(schedule))
                    .build()
        }

        val ITEM_2 = TrackingItem.builder()
                .setIdentifier("Med2")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_2: MedicationConfig

        init {
            val schedule = Schedule(
                    LocalTime.MIDNIGHT.plusHours(6),
                    setOf(2, 3, 6))

            CONFIG_2 = MedicationConfig.builder()
                    .setIdentifier("Med2")
                    .setDosage(DOSAGE)
                    .setSchedules(Collections.singletonList(schedule))
                    .build();
        }

        val ITEM_3 = TrackingItem.builder()
                .setIdentifier("Med3")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_3: MedicationConfig

        init {
            val schedule1 = Schedule(LocalTime.NOON.plusHours(1))

            val schedule2 = Schedule(LocalTime.NOON.plusHours(6),
                    setOf(7, 1))

            CONFIG_3 = MedicationConfig.builder()
                    .setIdentifier("Med3")
                    .setDosage(DOSAGE)
                    .setSchedules(Arrays.asList(schedule1, schedule2))
                    .build()
        }

        val ITEM_4 = TrackingItem.builder()
                .setIdentifier("Med4")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_4: MedicationConfig

        init {
            val schedule = Schedule(LocalTime.MIDNIGHT.plusHours(1))

            CONFIG_4 = MedicationConfig.builder()
                    .setIdentifier("Med4")
                    .setDosage(DOSAGE)
                    .setSchedules(Collections.singletonList(schedule))
                    .build()
        }

        val ITEM_5 = TrackingItem.builder()
                .setIdentifier("Med5")
                .setSectionIdentifier("section")
                .build()
        val CONFIG_5: MedicationConfig

        init {
            val schedule1 = Schedule(LocalTime.NOON.plusHours(5).plusMinutes(30), setOf(4))
            val schedule2 = Schedule(LocalTime.NOON.plusHours(9), setOf(4))
            val schedule3 = Schedule(LocalTime.MIDNIGHT.plusHours(2).plusMinutes(30), setOf(5))

            CONFIG_5 = MedicationConfig.builder()
                    .setIdentifier("Med5")
                    .setDosage(DOSAGE)
                    .setSchedules(Arrays.asList(schedule1, schedule2, schedule3))
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
        viewModel = MedicationTrackingTaskViewModel(InstrumentationRegistry.getTargetContext(), STEP_VIEW, null)
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
                MedicationLoggingSchedule(CONFIG_1, CONFIG_1.schedules[0], null),
                MedicationLoggingTitle("Med2 100mg"),
                MedicationLoggingSchedule(CONFIG_2, CONFIG_2.schedules[0], null)
        )

        assertEquals(currentConfigs.size, expected.size)

        assertTrue(currentConfigs[0] is MedicationLoggingTitle)
        assertEquals((currentConfigs[0] as MedicationLoggingTitle).title, "Med1 100mg")

        assertTrue(currentConfigs[1] is MedicationLoggingSchedule)
        assertEquals((currentConfigs[1] as MedicationLoggingSchedule).config, CONFIG_1)
        assertEquals((currentConfigs[1] as MedicationLoggingSchedule).schedule, CONFIG_1.schedules[0])
        assertNull((currentConfigs[1] as MedicationLoggingSchedule).loggedDate)

        assertTrue(currentConfigs[2] is MedicationLoggingTitle)
        assertEquals((currentConfigs[2] as MedicationLoggingTitle).title, "Med2 100mg")

        assertTrue(currentConfigs[3] is MedicationLoggingSchedule)
        assertEquals((currentConfigs[3] as MedicationLoggingSchedule).config, CONFIG_2)
        assertEquals((currentConfigs[3] as MedicationLoggingSchedule).schedule, CONFIG_2.schedules[0])
        assertNull((currentConfigs[3] as MedicationLoggingSchedule).loggedDate)
    }
}