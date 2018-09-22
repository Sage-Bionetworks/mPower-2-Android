package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.Map;

/**
 * Adapter which produces Logging Items for the Symptoms Task.
 */
public class SymptomsLoggingItemAdapter extends Adapter<SymptomsLoggingItemViewHolder> {
    private List<SimpleTrackingItemConfig> configs;
    @NonNull
    private SymptomsLoggingItemViewHolder.SymptomsLoggingListener symptomsLoggingListener;
    private Map<Integer, SymptomLog> logsByPosition;

    public SymptomsLoggingItemAdapter(
            List<SimpleTrackingItemConfig> configs,
            @NonNull SymptomsLoggingItemViewHolder.SymptomsLoggingListener symptomsLoggingListener,
            Map<Integer, SymptomLog> logsByPosition) {
        this.symptomsLoggingListener = symptomsLoggingListener;
        this.configs = configs;
        this.logsByPosition = logsByPosition;
    }

    public void updateLog(int position, @Nullable SymptomLog log) {
        if (log == null) {
            this.logsByPosition.remove(position);
        } else {
            this.logsByPosition.put(position, log);
        }
    }

    @NonNull
    @Override
    public SymptomsLoggingItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        SymptomsLoggingUIFormItemWidget widget = (SymptomsLoggingUIFormItemWidget)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.mpower2_symptoms_logging_view_holder, parent,false);
        return new SymptomsLoggingItemViewHolder(widget, symptomsLoggingListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final SymptomsLoggingItemViewHolder holder, final int position) {
        SimpleTrackingItemConfig config = configs.get(position);
        SymptomLog log = logsByPosition.get(position);
        holder.setContent(config, log, position);
    }

    @Override
    public int getItemCount() {
        return configs.size();
    }
}
