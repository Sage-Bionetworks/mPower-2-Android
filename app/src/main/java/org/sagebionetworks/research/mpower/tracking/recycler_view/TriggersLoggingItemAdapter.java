package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.widget.TriggersLoggingUIFormItemWidget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter which produces Logging Items for the Triggers task.
 */
public class TriggersLoggingItemAdapter extends Adapter<TriggersLoggingItemViewHolder> {
    private List<SimpleTrackingItemConfig> items;
    @NonNull
    private TriggersLoggingItemViewHolder.TriggersLoggingListener triggersLoggingListener;
    private Set<Integer> selectedIndices;

    public TriggersLoggingItemAdapter(List<SimpleTrackingItemConfig> items,
            @NonNull TriggersLoggingItemViewHolder.TriggersLoggingListener triggersLoggingListener, Set<Integer> selectedIndices) {
        this.items = new ArrayList<>(items);
        this.triggersLoggingListener = triggersLoggingListener;
        this.selectedIndices = new HashSet<>(selectedIndices);
    }

    @NonNull
    @Override
    public TriggersLoggingItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        TriggersLoggingUIFormItemWidget triggersLoggingUIFormItemWidget = (TriggersLoggingUIFormItemWidget)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.mpower2_triggers_logging_view_holder, parent, false);
        return new TriggersLoggingItemViewHolder(triggersLoggingUIFormItemWidget, triggersLoggingListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final TriggersLoggingItemViewHolder holder, final int position) {
        SimpleTrackingItemConfig config = items.get(position);
        boolean recorded = selectedIndices.contains(position);
        holder.setContent(config, recorded, position);
    }

    public void setRecorded(final int position, final boolean recorded) {
       if (recorded) {
           selectedIndices.add(position);
       } else {
           selectedIndices.remove(position);
       }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
