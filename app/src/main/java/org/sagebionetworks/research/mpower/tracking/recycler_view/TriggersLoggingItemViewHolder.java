package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;

import org.sagebionetworks.research.mpower.tracking.fragment.TriggersLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.widget.TriggersLoggingUIFormItemWidget;
import org.threeten.bp.Instant;

/**
 * View Holder for the Logging Items from the Triggers Task.
 */
public class TriggersLoggingItemViewHolder extends ViewHolder {
    private TriggersLoggingUIFormItemWidget widget;
    private TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel;
    private SimpleTrackingItemConfig config;
    private TriggersLoggingFragment triggersLoggingFragment;

    public TriggersLoggingItemViewHolder(final TriggersLoggingUIFormItemWidget itemView,
            final TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel,
            final TriggersLoggingFragment triggersLoggingFragment) {
        super(itemView);
        this.widget = itemView;
        this.viewModel = viewModel;
        this.triggersLoggingFragment = triggersLoggingFragment;
    }

    public void setContent(@NonNull SimpleTrackingItemConfig config) {
        this.config = config;
        boolean logged = this.viewModel.isLogged(config.getIdentifier());
        this.widget.setLogged(logged);
        this.widget.getTitle().setText(config.getTrackingItem().getIdentifier());
        this.setRecordButtonListener();
        this.setUndoButtonListener();
        this.setLogObservers(this.config.getTrackingItem());
    }

    private void setLogObservers(@NonNull TrackingItem trackingItem) {
        final LiveData<SimpleTrackingItemLog> logLiveData = this.viewModel.getLoggedElement(trackingItem.getIdentifier());
        logLiveData.observe(this.triggersLoggingFragment, log -> this.widget.setLogged(log != null));
    }

    private void setRecordButtonListener() {
        this.widget.getRecordButton().setOnClickListener((view) -> {
            SimpleTrackingItemLog log = SimpleTrackingItemLog.builder()
                    .setIdentifier(this.config.getIdentifier())
                    .setText(this.config.getIdentifier())
                    .setTimestamp(Instant.now())
                    .build();
            this.viewModel.addLoggedElement(log);
        });
    }

    private void setUndoButtonListener() {
        this.widget.getUndoButton().setOnClickListener((view) -> {
            String identifier = this.config.getTrackingItem().getIdentifier();
            this.viewModel.removeLoggedElement(identifier);
        });
    }
}
