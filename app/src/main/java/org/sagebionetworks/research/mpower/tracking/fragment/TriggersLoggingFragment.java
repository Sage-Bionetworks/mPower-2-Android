package org.sagebionetworks.research.mpower.tracking.fragment;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;

import org.sagebionetworks.research.mpower.tracking.recycler_view.TriggersLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.recycler_view.TriggersLoggingItemViewHolder.TriggersLoggingListener;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * A subclass of LoggingFragment specific to the Triggers Task.
 */
public class TriggersLoggingFragment extends LoggingFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog,
        SimpleTrackingTaskViewModel> {
    @NonNull
    public static TriggersLoggingFragment newInstance(@NonNull StepView step) {
        TriggersLoggingFragment fragment = new TriggersLoggingFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        return TriggersSelectionFragment.newInstance(stepView);
    }

    @NonNull
    @Override
    public Adapter<?> initializeAdapter() {
        List<SimpleTrackingItemConfig> activeElements = new ArrayList<>(
                viewModel.getActiveElementsSorted().getValue());
        return new TriggersLoggingItemAdapter(activeElements, getLifecycle(), new TriggersLoggingListener() {
            @Override
            public void recordButtonPressed(@NonNull final SimpleTrackingItemConfig config) {
                viewModel.addLoggedElement(SimpleTrackingItemLog.builder()
                        .setIdentifier(config.getIdentifier())
                        .setText(config.getIdentifier())
                        .setTimestamp(Instant.now())
                        .build());
            }

            @Override
            public void undoButtonPressed(@NonNull final SimpleTrackingItemConfig config) {
                viewModel.removeLoggedElement(config.getIdentifier());
            }

            @Override
            public LiveData<SimpleTrackingItemLog> getLog(@NonNull final SimpleTrackingItemConfig config) {
                return viewModel.getLoggedElement(config.getIdentifier());
            }
        });
    }

}
