package org.sagebionetworks.research.mpower.tracking.fragment;

import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.widget.TimePicker;

import org.sagebionetworks.research.mpower.tracking.recycler_view.SymptomsLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

/**
 * A subclass of LoggingFragment specific to the Symptoms task.
 */
public class SymptomLoggingFragment extends
        LoggingFragment<SimpleTrackingItemConfig, SymptomLog, SymptomTrackingTaskViewModel> {
    public static final String SYMPTOM_LOGGING_FRAGMENT_TAG = "symptomLoggingFragment";

    @NonNull
    public static SymptomLoggingFragment newInstance(@NonNull StepView stepView) {
        SymptomLoggingFragment fragment = new SymptomLoggingFragment();
        Bundle args = TrackingFragment.createArguments(stepView);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        return SymptomSelectionFragment.newInstance(this.stepView);
    }

    @NonNull
    @Override
    public Adapter<?> initializeAdapter() {
        return new SymptomsLoggingItemAdapter(this.viewModel, this);
    }
}
