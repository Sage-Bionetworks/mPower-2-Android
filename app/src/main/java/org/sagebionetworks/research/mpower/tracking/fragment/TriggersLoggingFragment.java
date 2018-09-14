package org.sagebionetworks.research.mpower.tracking.fragment;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.SimpleItemAnimator;

import org.sagebionetworks.research.mpower.tracking.SortUtil;
import org.sagebionetworks.research.mpower.tracking.recycler_view.TriggersLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.recycler_view.TriggersLoggingItemViewHolder.TriggersLoggingListener;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A subclass of LoggingFragment specific to the Triggers Task.
 */
public class TriggersLoggingFragment extends LoggingFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog,
        SimpleTrackingTaskViewModel, TriggersLoggingItemAdapter> {
    @NonNull
    public static TriggersLoggingFragment newInstance(@NonNull StepView step) {
        TriggersLoggingFragment fragment = new TriggersLoggingFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Disable animations since the way views change size makes them look odd.
        ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    @NonNull
    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        return TriggersSelectionFragment.newInstance(stepView);
    }

    @NonNull
    @Override
    public TriggersLoggingItemAdapter initializeAdapter() {
        List<SimpleTrackingItemConfig> activeElements = SortUtil.getActiveElementsSorted(viewModel.getActiveElementsById().getValue());
        TriggersLoggingListener triggersLoggingListener = new TriggersLoggingListener() {
            @Override
            public void recordButtonPressed(@NonNull final SimpleTrackingItemConfig config, final int position) {
                adapter.setRecorded(position, true);
                adapter.notifyItemChanged(position);
                viewModel.addLoggedElement(SimpleTrackingItemLog.builder()
                        .setIdentifier(config.getIdentifier())
                        .setText(config.getIdentifier())
                        .setTimestamp(Instant.now())
                        .build());
            }

            @Override
            public void undoButtonPressed(@NonNull final SimpleTrackingItemConfig config, final int position) {
                adapter.setRecorded(position, false);
                adapter.notifyItemChanged(position);
                viewModel.removeLoggedElement(config.getIdentifier());
            }
        };

        Set<Integer> recordedIndices = getRecordedIndices(activeElements);
        return new TriggersLoggingItemAdapter(activeElements, triggersLoggingListener, recordedIndices);
    }

    private Set<Integer> getRecordedIndices(List<SimpleTrackingItemConfig> activeElements) {
        Set<Integer> recordedIndices = new HashSet<>();
        Set<String> recordedIdentifiers = viewModel.getLoggedElementsById().getValue().keySet();
        for (int i = 0; i < activeElements.size(); i++) {
            if (recordedIdentifiers.contains(activeElements.get(i).getIdentifier())) {
                recordedIndices.add(i);
            }
        }

        return recordedIndices;
    }
}
