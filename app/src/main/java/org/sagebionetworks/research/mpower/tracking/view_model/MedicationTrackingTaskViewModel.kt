package org.sagebionetworks.research.mpower.tracking.view_model

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.slf4j.LoggerFactory

class MedicationTrackingTaskViewModel(stepView: TrackingStepView,
        previousLoggingCollection: LoggingCollection<MedicationLog>?)
    : TrackingTaskViewModel<MedicationConfig, MedicationLog>(stepView, previousLoggingCollection) {

    private val LOGGER = LoggerFactory.getLogger(MedicationTrackingTaskViewModel::class.java)

    override fun instantiateLoggingCollection(): LoggingCollection<MedicationLog> {
        return LoggingCollection.builder<MedicationLog>()
                .setIdentifier("trackedItems")
                .build()
    }

    override fun instantiateLogForUnloggedItem(config: MedicationConfig): MedicationLog {
        return MedicationLog.builder()
                .setIdentifier(config.identifier)
                .build()
    }

    override fun instantiateConfigFromSelection(item: TrackingItem): MedicationConfig {
        return MedicationConfig.builder()
                .setIdentifier(item.identifier)
                .setTrackingItem(item)
                .build()
    }

}

