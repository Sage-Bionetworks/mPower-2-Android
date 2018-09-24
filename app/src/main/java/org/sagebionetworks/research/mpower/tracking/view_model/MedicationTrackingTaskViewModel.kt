package org.sagebionetworks.research.mpower.tracking.view_model

import org.sagebionetworks.research.mpower.tracking.recycler_view.Schedule
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.slf4j.LoggerFactory

class MedicationTrackingTaskViewModel(stepView: TrackingStepView,
        previousLoggingCollection: LoggingCollection<SimpleTrackingItemLog>?)
    : TrackingTaskViewModel<MedicationConfig, SimpleTrackingItemLog>(stepView, previousLoggingCollection) {

    private val LOGGER = LoggerFactory.getLogger(MedicationTrackingTaskViewModel::class.java)

    override fun instantiateLoggingCollection(): LoggingCollection<SimpleTrackingItemLog> {
        return LoggingCollection.builder<SimpleTrackingItemLog>()
                .setIdentifier("trackedItems")
                .build()
    }

    override fun instantiateLogForUnloggedItem(config: MedicationConfig): SimpleTrackingItemLog {
        return SimpleTrackingItemLog.builder()
                .setIdentifier(config.identifier)
                .setText(config.identifier)
                .build()
    }

    override fun instantiateConfigFromSelection(item: TrackingItem): MedicationConfig {
        return MedicationConfig.builder()
                .setIdentifier(item.identifier)
                .setTrackingItem(item)
                .build()
    }

}

