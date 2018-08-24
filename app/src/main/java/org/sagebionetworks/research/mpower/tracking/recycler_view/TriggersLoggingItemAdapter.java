package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingActiveTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.widget.TriggersLoggingUIFormItemWidget;

import java.util.List;

public class TriggersLoggingItemAdapter extends Adapter<TriggersLoggingItemViewHolder> {
    private List<SimpleTrackingItemConfig> items;
    private TrackingActiveTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel;

    public TriggersLoggingItemAdapter(List<SimpleTrackingItemConfig> items,
            TrackingActiveTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel) {
        this.items = items;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public TriggersLoggingItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        TriggersLoggingUIFormItemWidget triggersLoggingUIFormItemWidget = (TriggersLoggingUIFormItemWidget)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.mpower2_triggers_logging_view_holder, parent, false);
        return new TriggersLoggingItemViewHolder(triggersLoggingUIFormItemWidget, this.viewModel);
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
