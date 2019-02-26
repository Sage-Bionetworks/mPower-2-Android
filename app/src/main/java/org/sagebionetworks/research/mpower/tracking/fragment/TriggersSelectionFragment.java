package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.view_model.TriggersTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

/**
 * A subclass of SelectionFragment specific to the Triggers Task.
 */
public class TriggersSelectionFragment extends SelectionFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog, TriggersTrackingTaskViewModel> {
    @NonNull
    public static SelectionFragment newInstance(@NonNull StepView step) {
        TriggersSelectionFragment fragment = new TriggersSelectionFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        return TriggersLoggingFragment.newInstance(this.stepView);
    }
}
