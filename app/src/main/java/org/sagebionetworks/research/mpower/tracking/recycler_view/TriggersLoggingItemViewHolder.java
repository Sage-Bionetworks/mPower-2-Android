package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
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
public class TriggersLoggingItemViewHolder extends ViewHolder implements LifecycleOwner{
    public interface TriggersLoggingListener {
        void recordButtonPressed(@NonNull SimpleTrackingItemConfig config);

        void undoButtonPressed(@NonNull SimpleTrackingItemConfig config);

        LiveData<SimpleTrackingItemLog> getLog(@NonNull SimpleTrackingItemConfig config);
    }

    private TriggersLoggingUIFormItemWidget widget;
    private SimpleTrackingItemConfig config;
    private TriggersLoggingListener triggersLoggingListener;
    private Lifecycle lifecycle;

    public TriggersLoggingItemViewHolder(final TriggersLoggingUIFormItemWidget itemView,
            final TriggersLoggingListener triggersLoggingListener, final Lifecycle lifecycle) {
        super(itemView);
        widget = itemView;
        this.lifecycle = lifecycle;
        this.triggersLoggingListener = triggersLoggingListener;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setContent(@NonNull SimpleTrackingItemConfig config) {
        this.config = config;
        boolean logged = triggersLoggingListener.getLog(config).getValue() != null;
        widget.setLogged(logged);
        widget.getTitle().setText(config.getTrackingItem().getIdentifier());
        setRecordButtonListener();
        setUndoButtonListener();
        setLogObservers(config.getTrackingItem());
    }

    private void setLogObservers(@NonNull TrackingItem trackingItem) {
        final LiveData<SimpleTrackingItemLog> logLiveData = triggersLoggingListener.getLog(config);
        logLiveData.observe(this,  log -> widget.setLogged(log != null));
    }

    private void setRecordButtonListener() {
        widget.getRecordButton().setOnClickListener(view -> triggersLoggingListener.recordButtonPressed(config));
    }

    private void setUndoButtonListener() {
        this.widget.getUndoButton().setOnClickListener(view -> triggersLoggingListener.undoButtonPressed(config));
    }
}
