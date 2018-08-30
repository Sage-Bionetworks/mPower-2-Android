package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.widget.TriggersLoggingUIFormItemWidget;

import java.util.List;

/**
 * Adapter which produces Logging Items for the Triggers task.
 */
public class TriggersLoggingItemAdapter extends Adapter<TriggersLoggingItemViewHolder> {
    private List<SimpleTrackingItemConfig> items;
    private TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel;
    private TriggersLoggingFragment triggersLoggingFragment;

    public TriggersLoggingItemAdapter(List<SimpleTrackingItemConfig> items,
            TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel,
            TriggersLoggingFragment triggersLoggingFragment) {
        this.items = items;
        this.viewModel = viewModel;
        this.triggersLoggingFragment = triggersLoggingFragment;
    }

    @NonNull
    @Override
    public TriggersLoggingItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        TriggersLoggingUIFormItemWidget triggersLoggingUIFormItemWidget = (TriggersLoggingUIFormItemWidget)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.mpower2_triggers_logging_view_holder, parent, false);
        return new TriggersLoggingItemViewHolder(triggersLoggingUIFormItemWidget, this.viewModel, this.triggersLoggingFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull final TriggersLoggingItemViewHolder holder, final int position) {
        SimpleTrackingItemConfig config = this.items.get(position);
        holder.setContent(config);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
