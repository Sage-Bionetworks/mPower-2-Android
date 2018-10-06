package org.sagebionetworks.research.mpower.tracking.recycler_view;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.widget.TriggersLoggingUIFormItemWidget;

/**
 * View Holder for the Logging Items from the Triggers Task.
 */
public class TriggersLoggingItemViewHolder extends ViewHolder {
    public interface TriggersLoggingListener {
        void recordButtonPressed(@NonNull SimpleTrackingItemConfig config, int position);

        void undoButtonPressed(@NonNull SimpleTrackingItemConfig config, int position);
    }

    private TriggersLoggingUIFormItemWidget widget;
    private SimpleTrackingItemConfig config;
    @NonNull
    private TriggersLoggingListener triggersLoggingListener;
    private int position;

    public TriggersLoggingItemViewHolder(final TriggersLoggingUIFormItemWidget itemView,
            @NonNull final TriggersLoggingListener triggersLoggingListener) {
        super(itemView);
        widget = itemView;
        this.triggersLoggingListener = triggersLoggingListener;
    }

    public void setContent(@NonNull SimpleTrackingItemConfig config, boolean recorded, int position) {
        this.config = config;
        this.position = position;
        updateRecorded(recorded);
        widget.getTitle().setText(config.getTrackingItem().getIdentifier());
        setRecordButtonListener();
        setUndoButtonListener();
    }

    private void updateRecorded(boolean recorded) {
        widget.setLogged(recorded);
    }

    private void setRecordButtonListener() {
        widget.getRecordButton().setOnClickListener(view -> triggersLoggingListener.recordButtonPressed(config, position));
    }

    private void setUndoButtonListener() {
        this.widget.getUndoButton().setOnClickListener(view -> triggersLoggingListener.undoButtonPressed(config, position));
    }
}
