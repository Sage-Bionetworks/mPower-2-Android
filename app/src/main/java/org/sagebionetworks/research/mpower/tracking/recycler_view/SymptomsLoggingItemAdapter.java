package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.widget.SymptomsLoggingUIFormItemWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter which produces Logging Items for the Symptoms Task.
 */
public class SymptomsLoggingItemAdapter extends Adapter<SymptomsLoggingItemViewHolder> {
    private List<SimpleTrackingItemConfig> configs;
    private SymptomsLoggingItemViewHolder.SymptomsLoggingListener symptomsLoggingListener;
    private Lifecycle lifecycle;

    public SymptomsLoggingItemAdapter(
            SymptomsLoggingItemViewHolder.SymptomsLoggingListener symptomsLoggingListener,
            Lifecycle lifecycle, List<SimpleTrackingItemConfig> configs) {
        this.lifecycle = lifecycle;
        this.symptomsLoggingListener = symptomsLoggingListener;
        this.configs = configs;
    }

    @NonNull
    @Override
    public SymptomsLoggingItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        SymptomsLoggingUIFormItemWidget widget = (SymptomsLoggingUIFormItemWidget)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.mpower2_symptoms_logging_view_holder, parent,false);
        return new SymptomsLoggingItemViewHolder(widget, lifecycle, symptomsLoggingListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final SymptomsLoggingItemViewHolder holder, final int position) {
        SimpleTrackingItemConfig config = this.configs.get(position);
        holder.setContent(config);
    }

    @Override
    public int getItemCount() {
        return this.configs.size();
    }
}
