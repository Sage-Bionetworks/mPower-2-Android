package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

/**
 * Subtype of SelectionFragment specific to the flow and data types of the Medication task.
 */
// TODO use the correct generics once the types for medication are more certain.
public class MedicationSelectionFragment extends SelectionFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog, SimpleTrackingTaskViewModel> {
    @NonNull
    public static MedicationSelectionFragment newInstance(@NonNull StepView step) {
        MedicationSelectionFragment fragment = new MedicationSelectionFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        // TODO return the correct fragment here.
        return null;
    }
}
