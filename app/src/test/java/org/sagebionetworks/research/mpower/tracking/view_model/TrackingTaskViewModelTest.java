package org.sagebionetworks.research.mpower.tracking.view_model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSubstepInfo;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TrackingTaskViewModelTest {

    // Basic Implementation of a ViewModel.
    private class TestViewModel extends TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> {
        private TestViewModel(
                @NonNull final TrackingStepView stepView,
                @Nullable final LoggingCollection<SimpleTrackingItemLog> previousLoggingCollection) {
            super(stepView, previousLoggingCollection);
        }

        @Override
        public LoggingCollection<SimpleTrackingItemLog> instantiateLoggingCollection() {
            return LoggingCollection.<SimpleTrackingItemLog>builder()
                    .setIdentifier(TrackingTaskViewModel.LOGGING_COLLECTION_IDENTIFIER)
                    .build();
        }

        @Override
        protected SimpleTrackingItemLog instantiateLogForUnloggedItem(
                @NonNull final SimpleTrackingItemConfig config) {
            return SimpleTrackingItemLog.builder()
                    .setIdentifier(config.getIdentifier())
                    .setText(config.getIdentifier())
                    .build();
        }

        @Override
        protected SimpleTrackingItemConfig instantiateConfigFromSelection(@NonNull final TrackingItem item) {
            return SimpleTrackingItemConfig.builder()
                    .setIdentifier(item.getIdentifier())
                    .setTrackingItem(item)
                    .build();
        }

        void setTaskStartDate(Instant startDate) {
            this.startDate = startDate;
        }

        void setTaskEndDate(Instant endDate) {
            this.endDate = endDate;
        }
    }

    private static Set<TrackingItem> TRACKING_ITEMS;

    private static Instant START_DATE = Instant.ofEpochMilli(1000);

    private static Instant END_DATE = Instant.ofEpochMilli(20000);

    static {
        TRACKING_ITEMS = new HashSet<>();
        TrackingItem item1 = TrackingItem.builder().setIdentifier("item1.1").setSectionIdentifier("section1").build();
        TRACKING_ITEMS.add(item1);
        TrackingItem item2 = TrackingItem.builder().setIdentifier("item1.2").setSectionIdentifier("section1").build();
        TRACKING_ITEMS.add(item2);
        TrackingItem item3 = TrackingItem.builder().setIdentifier("item1.3").setSectionIdentifier("section1").build();
        TRACKING_ITEMS.add(item3);
        TrackingItem item4 = TrackingItem.builder().setIdentifier("item2.1").setSectionIdentifier("section2").build();
        TRACKING_ITEMS.add(item4);
        TrackingItem item5 = TrackingItem.builder().setIdentifier("item3.1").setSectionIdentifier("section3").build();
        TRACKING_ITEMS.add(item5);
        TrackingItem item6 = TrackingItem.builder().setIdentifier("item3.2").setSectionIdentifier("section3").build();
        TRACKING_ITEMS.add(item6);
    }

    private static TrackingStep TRACKING_STEP = TrackingStep.builder()
            .setIdentifier("identifier")
            .setItems(TRACKING_ITEMS)
            .setSelectionInfo(TrackingSubstepInfo.builder().build())
            .setLoggingInfo(TrackingSubstepInfo.builder().build())
            .build();

    private static TrackingStepView TRACKING_STEP_VIEW = TrackingStepView.fromTrackingStep(TRACKING_STEP, null);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private TestViewModel viewModel;

    private Application getApplication() {
        return new Application();
    }

    @Before
    public void initializeViewModel() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        this.viewModel = new TestViewModel(TRACKING_STEP_VIEW, null);
        this.viewModel.setTaskStartDate(START_DATE);
    }

    private void setupSelections() {
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        for (int i = 0; i < 3; i++) {
            this.viewModel.itemSelected(iterator.next());
        }
    }

    private void allAvailableElementsPresent() {
        Map<TrackingSection, Set<TrackingItem>> availableElements = this.viewModel.getAvailableElements().getValue();
        assertNotNull("Available elements was unexpectedly null", availableElements);

        Set<TrackingItem> itemsInSection1 = availableElements
                .get(TrackingSection.builder().setIdentifier("section1").build());
        assertNotNull("section1 had an unexpectedly null set of contents", itemsInSection1);
        TrackingItem section1Item1 = TrackingItem.builder().setIdentifier("item1.1").setSectionIdentifier("section1")
                .build();
        assertTrue("Section 1 didn't contain item1.1", itemsInSection1.contains(section1Item1));
        TrackingItem section1Item2 = TrackingItem.builder().setIdentifier("item1.2").setSectionIdentifier("section1")
                .build();
        assertTrue("Section 1 didn't contain item1.2", itemsInSection1.contains(section1Item2));
        TrackingItem section1Item3 = TrackingItem.builder().setIdentifier("item1.3").setSectionIdentifier("section1")
                .build();
        assertTrue("Section 1 didn't contain item1.3", itemsInSection1.contains(section1Item3));
        assertTrue("Section 1 had extra elements in it", itemsInSection1.size() == 3);

        Set<TrackingItem> itemsInSection2 = availableElements
                .get(TrackingSection.builder().setIdentifier("section2").build());
        assertNotNull("section2 had an unexpectedly null set of contents", itemsInSection2);
        TrackingItem section2Item1 = TrackingItem.builder().setIdentifier("item2.1").setSectionIdentifier("section2")
                .build();
        assertTrue("Section 2 didn't contain item2.1", itemsInSection2.contains(section2Item1));
        assertTrue("Section 2 had extra elements in it", itemsInSection2.size() == 1);

        Set<TrackingItem> itemsInSection3 = availableElements
                .get(TrackingSection.builder().setIdentifier("section3").build());
        assertNotNull("Section3 had an unexpectedly null set of contents", itemsInSection3);
        TrackingItem section3Item1 = TrackingItem.builder().setIdentifier("item3.1").setSectionIdentifier("section3")
                .build();
        assertTrue("Section 3 didn't contain item3.1", itemsInSection3.contains(section3Item1));
        TrackingItem section3Item2 = TrackingItem.builder().setIdentifier("item3.2").setSectionIdentifier("section3")
                .build();
        assertTrue("Section 3 didn't contain item3.2", itemsInSection3.contains(section3Item2));
        assertTrue("Section 3 had extra elements in it", itemsInSection3.size() == 2);
    }

    private void activeElementsIsEmpty() {
        Map<String, SimpleTrackingItemConfig> activeElements = this.viewModel.getActiveElementsById().getValue();
        assertNotNull("Active elements was unexpectedly null", activeElements);
        assertTrue("Active elements was unexpectedly not empty", activeElements.isEmpty());
    }

    private void loggedElementsIsEmpty() {
        Map<String, SimpleTrackingItemLog> loggedElements = this.viewModel.getLoggedElementsById().getValue();
        assertNotNull("Logged elements was unexpectedly null", loggedElements);
        assertTrue("Logged elements was unexpectedly not empty", loggedElements.isEmpty());
    }

    // region Initialization
    @Test
    public void test_initialState_NoPreviousLoggingCollection() {
        this.allAvailableElementsPresent();
        this.activeElementsIsEmpty();
        this.loggedElementsIsEmpty();
    }

    @Test
    public void test_initialState_PreviousLoggingCollection() {
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        TrackingItem item2 = iterator.next();
        // log1 has previously been logged by the user.
        SimpleTrackingItemLog log1 = SimpleTrackingItemLog.builder()
                .setIdentifier(item1.getIdentifier())
                .setText(item1.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        // log2 has previously not been logged by the user.
        SimpleTrackingItemLog log2 = SimpleTrackingItemLog.builder()
                .setIdentifier(item2.getIdentifier())
                .setText(item2.getIdentifier())
                .build();
        LoggingCollection<SimpleTrackingItemLog> previousCollection
                = LoggingCollection.<SimpleTrackingItemLog>builder()
                .setIdentifier("trackedItems")
                .setItems(Arrays.asList(log1, log2))
                .setStartDate(START_DATE)
                .setEndDate(END_DATE)
                .build();

        // re-create the view model with the previous collection.
        viewModel = new TestViewModel(TRACKING_STEP_VIEW, previousCollection);
        allAvailableElementsPresent();
        Map<String, SimpleTrackingItemConfig> activeElements = viewModel.getActiveElementsById().getValue();
        assertNotNull("Active elements was unexpectedly null", activeElements);
        assertTrue("First log was unexpectedly not in the active elements",
                activeElements.containsKey(log1.getIdentifier()));
        assertTrue("Second log was unexpectedly not in the active elements",
                activeElements.containsKey(log2.getIdentifier()));
        assertTrue("Active elements contained extra items", activeElements.size() == 2);
        loggedElementsIsEmpty();
    }
    // endregion

    // region Selection
    @Test
    public void test_itemSelected() {
        TrackingItem item = TRACKING_ITEMS.iterator().next();
        this.viewModel.itemSelected(item);
        // ensure the available elements haven't changed.
        this.allAvailableElementsPresent();
        // ensure the active elements now have a new config.
        Map<String, SimpleTrackingItemConfig> activeElements = this.viewModel.getActiveElementsById().getValue();
        assertNotNull("Active elements was unexpectedly null", activeElements);
        assertTrue("Active elements didn't contain selected item", activeElements.containsKey(item.getIdentifier()));
        assertTrue("Active elements contained extra items", activeElements.size() == 1);
        this.loggedElementsIsEmpty();
    }

    @Test
    public void test_2ItemsSelected() {
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        this.viewModel.itemSelected(item1);
        TrackingItem item2 = iterator.next();
        this.viewModel.itemSelected(item2);
        Map<String, SimpleTrackingItemConfig> activeElements = this.viewModel.getActiveElementsById().getValue();
        assertNotNull("Active elements was unexpectedly null", activeElements);
        assertTrue("Active elements didn't contain first selected item",
                activeElements.containsKey(item1.getIdentifier()));
        assertTrue("Active elements didn't contain second selected item",
                activeElements.containsKey(item2.getIdentifier()));
        assertTrue("Active elements contained extra items", activeElements.size() == 2);
        this.loggedElementsIsEmpty();
    }

    @Test
    public void test_itemSelectedThenDeselected() {
        TrackingItem item = TRACKING_ITEMS.iterator().next();
        this.viewModel.itemSelected(item);
        this.viewModel.itemDeselected(item);
        // After a selection then deselection the state should be back to it's initial.
        this.test_initialState_NoPreviousLoggingCollection();
    }

    @Test
    public void test_2ItemsSelectedFirstDeselected() {
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        this.viewModel.itemSelected(item1);
        TrackingItem item2 = iterator.next();
        this.viewModel.itemSelected(item2);
        this.viewModel.itemDeselected(item1);
        Map<String, SimpleTrackingItemConfig> activeElements = this.viewModel.getActiveElementsById().getValue();
        assertNotNull("Active elements was unexpectedly null", activeElements);
        assertTrue("Active elements didn't contain selected item", activeElements.containsKey(item2.getIdentifier()));
        assertTrue("Active elements contained extra items", activeElements.size() == 1);
        this.loggedElementsIsEmpty();
    }

    @Test
    public void test_2ItemsSelectedSecondDeselected() {
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        this.viewModel.itemSelected(item1);
        TrackingItem item2 = iterator.next();
        this.viewModel.itemSelected(item2);
        this.viewModel.itemDeselected(item2);
        Map<String, SimpleTrackingItemConfig> activeElements = this.viewModel.getActiveElementsById().getValue();
        assertNotNull("Active elements was unexpectedly null", activeElements);
        assertTrue("Active elements didn't contain selected item", activeElements.containsKey(item1.getIdentifier()));
        assertTrue("Active elements contained extra items", activeElements.size() == 1);
        this.loggedElementsIsEmpty();
    }
    // endregion

    // region Logging
    @Test
    public void test_itemLogged() {
        this.setupSelections();
        TrackingItem item = TRACKING_ITEMS.iterator().next();
        SimpleTrackingItemLog log = SimpleTrackingItemLog.builder()
                .setIdentifier(item.getIdentifier())
                .setText(item.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        this.viewModel.addLoggedElement(log);
        Map<String, SimpleTrackingItemLog> loggedElements = this.viewModel.getLoggedElementsById().getValue();
        assertNotNull("Logged elements was unexpectedly null", loggedElements);
        assertTrue("Logged elements didn't contain item", this.viewModel.isLogged(item.getIdentifier()));
        assertEquals("Unexpected log found for item", log,
                this.viewModel.getLoggedElement(item.getIdentifier()));
        assertEquals("Logged elements contained extra items", 1, loggedElements.size());
    }

    @Test
    public void test_2ItemsLogged() {
        this.setupSelections();
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        TrackingItem item2 = iterator.next();
        SimpleTrackingItemLog log1 = SimpleTrackingItemLog.builder()
                .setIdentifier(item1.getIdentifier())
                .setText(item1.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        SimpleTrackingItemLog log2 = SimpleTrackingItemLog.builder()
                .setIdentifier(item2.getIdentifier())
                .setText(item2.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        this.viewModel.addLoggedElement(log1);
        this.viewModel.addLoggedElement(log2);
        Map<String, SimpleTrackingItemLog> loggedElements = this.viewModel.getLoggedElementsById().getValue();
        assertNotNull("Logged elements was unexpectedly null", loggedElements);
        assertTrue("Logged elements didn't contain item", this.viewModel.isLogged(item1.getIdentifier()));
        assertEquals("Unexpected log found for item", log1,
                this.viewModel.getLoggedElement(item1.getIdentifier()));
        assertTrue("Logged elements didn't contain item", this.viewModel.isLogged(item2.getIdentifier()));
        assertEquals("Unexpected log found for item", log2,
                this.viewModel.getLoggedElement(item2.getIdentifier()));
        assertEquals("Logged elements contained extra items", 2, loggedElements.size());
    }

    @Test
    public void test_removeLoggedItem() {
        this.setupSelections();
        TrackingItem item = TRACKING_ITEMS.iterator().next();
        SimpleTrackingItemLog log = SimpleTrackingItemLog.builder()
                .setIdentifier(item.getIdentifier())
                .setText(item.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        this.viewModel.addLoggedElement(log);
        this.viewModel.removeLoggedElement(item.getIdentifier());
        this.loggedElementsIsEmpty();
    }

    @Test
    public void test_2ItemsLoggedFirstRemoved() {
        this.setupSelections();
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        TrackingItem item2 = iterator.next();
        SimpleTrackingItemLog log1 = SimpleTrackingItemLog.builder()
                .setIdentifier(item1.getIdentifier())
                .setText(item1.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        SimpleTrackingItemLog log2 = SimpleTrackingItemLog.builder()
                .setIdentifier(item2.getIdentifier())
                .setText(item2.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        this.viewModel.addLoggedElement(log1);
        this.viewModel.addLoggedElement(log2);
        this.viewModel.removeLoggedElement(item1.getIdentifier());
        Map<String, SimpleTrackingItemLog> loggedElements = this.viewModel.getLoggedElementsById().getValue();
        assertNotNull("Logged elements was unexpectedly null", loggedElements);
        assertFalse("Logged elements still contained removed item", this.viewModel.isLogged(item1.getIdentifier()));
        assertTrue("Logged elements didn't contain item", this.viewModel.isLogged(item2.getIdentifier()));
        assertEquals("Unexpected log found for item", log2,
                this.viewModel.getLoggedElement(item2.getIdentifier()));
        assertTrue("Logged elements contained extra items", loggedElements.size() == 1);
    }

    @Test
    public void test_2ItemsLoggedSecondRemoved() {
        this.setupSelections();
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        TrackingItem item2 = iterator.next();
        SimpleTrackingItemLog log1 = SimpleTrackingItemLog.builder()
                .setIdentifier(item1.getIdentifier())
                .setText(item1.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        SimpleTrackingItemLog log2 = SimpleTrackingItemLog.builder()
                .setIdentifier(item2.getIdentifier())
                .setText(item2.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        this.viewModel.addLoggedElement(log1);
        this.viewModel.addLoggedElement(log2);
        this.viewModel.removeLoggedElement(item2.getIdentifier());
        Map<String, SimpleTrackingItemLog> loggedElements = this.viewModel.getLoggedElementsById().getValue();
        assertNotNull("Logged elements was unexpectedly null", loggedElements);
        assertFalse("Logged elements still contained removed item", this.viewModel.isLogged(item2.getIdentifier()));
        assertTrue("Logged elements didn't contain item", this.viewModel.isLogged(item1.getIdentifier()));
        assertEquals("Unexpected log found for item", log1,
                this.viewModel.getLoggedElement(item1.getIdentifier()));
        assertEquals("Logged elements contained extra items", 1, loggedElements.size());
    }
    // endregion

    // region LoggingCollection
    @Test
    public void test_getLoggingCollection_AllLogged() {
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        TrackingItem item2 = iterator.next();
        SimpleTrackingItemLog log1 = SimpleTrackingItemLog.builder()
                .setIdentifier(item1.getIdentifier())
                .setText(item1.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        SimpleTrackingItemLog log2 = SimpleTrackingItemLog.builder()
                .setIdentifier(item2.getIdentifier())
                .setText(item2.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        this.viewModel.itemSelected(item1);
        this.viewModel.itemSelected(item2);
        this.viewModel.addLoggedElement(log1);
        this.viewModel.addLoggedElement(log2);
        LoggingCollection<SimpleTrackingItemLog> loggingCollection = this.viewModel.getLoggingCollection();
        assertNotNull("Failed to get logging collection", loggingCollection);
        assertEquals("Logging collection had unexpected identifier",
                TrackingTaskViewModel.LOGGING_COLLECTION_IDENTIFIER,
                loggingCollection.getIdentifier());
        assertEquals("Logging collection had unexpected startDate", START_DATE, loggingCollection.getStartDate());
        assertEquals("Logging collection had unexpected type", LoggingCollection.DEFAULT_TYPE,
                loggingCollection.getType());
        assertTrue("Logging collection was missing first log", loggingCollection.getItems().contains(log1));
        assertTrue("Logging collection was missing second log", loggingCollection.getItems().contains(log2));
        assertTrue("Logging collection contained extra items", loggingCollection.getItems().size() == 2);
    }

    @Test
    public void test_getLoggingCollection_UnloggedElements() {
        Iterator<TrackingItem> iterator = TRACKING_ITEMS.iterator();
        TrackingItem item1 = iterator.next();
        TrackingItem item2 = iterator.next();
        SimpleTrackingItemLog log1 = SimpleTrackingItemLog.builder()
                .setIdentifier(item1.getIdentifier())
                .setText(item1.getIdentifier())
                .setLoggedDate(Instant.now())
                .build();
        this.viewModel.itemSelected(item1);
        this.viewModel.itemSelected(item2);
        this.viewModel.addLoggedElement(log1);
        LoggingCollection<SimpleTrackingItemLog> loggingCollection = this.viewModel.getLoggingCollection();
        assertNotNull("Failed to get logging collection", loggingCollection);
        assertEquals("Logging collection had unexpected identifier",
                TrackingTaskViewModel.LOGGING_COLLECTION_IDENTIFIER,
                loggingCollection.getIdentifier());
        assertEquals("Logging collection had unexpected startDate", START_DATE, loggingCollection.getStartDate());
        assertEquals("Logging collection had unexpected type", LoggingCollection.DEFAULT_TYPE,
                loggingCollection.getType());
        assertTrue("Logging collection was missing first log", loggingCollection.getItems().contains(log1));
        // log2 should be generated by the call to getLoggingCollection();
        SimpleTrackingItemLog log2 = SimpleTrackingItemLog.builder()
                .setIdentifier(item2.getIdentifier())
                .setText(item2.getIdentifier())
                .build();
        assertTrue("Logging collection was missing second log", loggingCollection.getItems().contains(log2));
        assertTrue("Logging collection contained extra items", loggingCollection.getItems().size() == 2);
    }
    // endregion
}

