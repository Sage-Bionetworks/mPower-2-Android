package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;

import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.widget.TriggersLoggingUIFormItemWidget;
import org.threeten.bp.Instant;

public class TriggersLoggingItemViewHolder extends ViewHolder {
    private TriggersLoggingUIFormItemWidget widget;
    private TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel;
    private SimpleTrackingItemConfig config;

    public TriggersLoggingItemViewHolder(final TriggersLoggingUIFormItemWidget itemView,
            final TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel) {
        super(itemView);
        this.widget = itemView;
        this.viewModel = viewModel;
        this.widget.getRecordButton().setOnClickListener((view) -> {
            SimpleTrackingItemLog log = SimpleTrackingItemLog.builder().setTrackingItem(this.config.getTrackingItem())
                    .setTimestamp(Instant.now()).build();
            this.viewModel.addLoggedElement(log);
            this.widget.setLogged(true);
        });

        this.widget.getUndoButton().setOnClickListener((view) -> {
            String identifier = this.config.getTrackingItem().getIdentifier();
            this.viewModel.removeLoggedElement(identifier);
            this.widget.setLogged(false);
        });
    }

    public void setContent(@NonNull SimpleTrackingItemConfig config) {
        this.config = config;
        boolean logged = this.viewModel.isLogged(config);
        this.widget.setLogged(logged);
        this.widget.getTitle().setText(config.getTrackingItem().getIdentifier());
    }
}
