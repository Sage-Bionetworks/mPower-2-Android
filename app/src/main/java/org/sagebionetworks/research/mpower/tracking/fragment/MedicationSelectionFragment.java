package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

/**
 * Subtype of SelectionFragment specific to the flow and data types of the Medication task.
 */
public class MedicationSelectionFragment extends SelectionFragment<MedicationConfig, MedicationLog, MedicationTrackingTaskViewModel> {
    @NonNull
    public static MedicationSelectionFragment newInstance(@NonNull StepView step) {
        MedicationSelectionFragment fragment = new MedicationSelectionFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        return MedicationAddDetailsFragment.newInstance(stepView);
    }
}
