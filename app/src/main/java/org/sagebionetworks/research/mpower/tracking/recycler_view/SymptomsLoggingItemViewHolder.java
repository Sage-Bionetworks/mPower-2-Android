package org.sagebionetworks.research.mpower.tracking.recycler_view;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioButton;

import org.sagebionetworks.research.mpower.MPowerRadioButton;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.widget.SymptomsLoggingUIFormItemWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.zone.ZoneRulesException;

import java.util.List;

/**
 * View Holder for the Logging Items in the Symptoms task.
 */
public class SymptomsLoggingItemViewHolder extends RecyclerView.ViewHolder implements LifecycleOwner {
    public interface SymptomsLoggingListener {
        void onSeverityChanged(@NonNull TrackingItem trackingItem, int severity);

        void onDurationButtonPressed(@NonNull TrackingItem trackingItem);

        void onTimeButtonPressed(@NonNull TrackingItem trackingItem);

        void onMedicationTimingChanged(@NonNull TrackingItem trackingItem, @NonNull String medicationTiming);

        void onNoteButtonPressed(@NonNull TrackingItem trackingItem);

        LiveData<SymptomLog> getSymptomLogData(@NonNull TrackingItem trackingItem);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SymptomsLoggingItemViewHolder.class);

    private static final float FULL_ALPHA = 1.0f;

    private static final float FADED_ALPHA = .35f;

    private SymptomsLoggingUIFormItemWidget widget;

    private RadioButton previousSelectedSeverityButton;

    private Lifecycle lifecycle;

    private SymptomsLoggingListener symptomLoggingListener;

    public SymptomsLoggingItemViewHolder(final SymptomsLoggingUIFormItemWidget itemView,
            final Lifecycle lifecycle, final SymptomsLoggingListener symptomLoggingListener) {
        super(itemView);
        this.widget = itemView;
        this.lifecycle = lifecycle;
        this.symptomLoggingListener = symptomLoggingListener;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setContent(@NonNull SimpleTrackingItemConfig config) {
        TrackingItem trackingItem = config.getTrackingItem();
        this.setLogObservers(trackingItem);

        // Setup the title and detail labels
        this.widget.getTitle().setText(trackingItem.getIdentifier());
        String detail = trackingItem.getDetail();
        if (detail != null) {
            this.widget.getDetail().setVisibility(View.VISIBLE);
            this.widget.getDetail().setText(detail);
        } else {
            this.widget.getDetail().setVisibility(View.GONE);
        }

        // Setup the button listeners.
        this.setSeverityButtonListeners(trackingItem);
        this.setTimeButtonListener(trackingItem);
        this.setDurationButtonListener(trackingItem);
        this.setPreMedsButtonListener(trackingItem);
        this.setPostMedsButtonListener(trackingItem);
        this.setAddNoteButtonListener(trackingItem);
    }

    private void setLogObservers(@NonNull TrackingItem trackingItem) {
        final LiveData<SymptomLog> logLiveData = symptomLoggingListener.getSymptomLogData(trackingItem);
        final LiveData<Integer> severityLiveData = Transformations
                .map(logLiveData, log -> {
                    if (log != null) {
                        return log.getSeverity();
                    }

                    return null;
                });

        severityLiveData.observe(this, this::updateSeverityUI);
        final LiveData<Instant> timestampLiveData = Transformations
                .map(logLiveData, log -> {
                    if (log != null) {
                        return log.getTimestamp();
                    }

                    return null;
                });

        timestampLiveData.observe(this, this::updateTimestampUI);
        final LiveData<String> durationLiveData = Transformations
                .map(logLiveData, log -> {
                    if (log != null) {
                        return log.getDuration();
                    }

                    return null;
                });

        durationLiveData.observe(this, this::updateDurationUI);
        final LiveData<String> medicationTimingLiveData = Transformations
                .map(logLiveData, log -> {
                    if (log != null) {
                        return log.getMedicationTiming();
                    }

                    return null;
                });

        medicationTimingLiveData.observe(this, this::updateMedicationTimingUI);
    }

    private void setSeverityButtonListeners(@NonNull TrackingItem trackingItem) {
        List<RadioButton> severityButtons = widget.getSeverityButtons();
        for (int i = 0; i < severityButtons.size(); i++) {
            RadioButton severityButton = severityButtons.get(i);
            final int copy = i;
            severityButton.setOnClickListener(view -> symptomLoggingListener.onSeverityChanged(trackingItem, copy));
        }
    }

    private void setTimeButtonListener(@NonNull TrackingItem trackingItem) {
        widget.getTimeButton().setOnClickListener(view -> symptomLoggingListener.onTimeButtonPressed(trackingItem));
    }

    private void setDurationButtonListener(@NonNull TrackingItem trackingItem) {
        widget.getDurationButton().setOnClickListener(view -> symptomLoggingListener.onDurationButtonPressed(trackingItem));
    }

    private void setPreMedsButtonListener(@NonNull TrackingItem trackingItem) {
        MPowerRadioButton preMedsButton = this.widget.getPreMedsButton();
        preMedsButton.setOnClickListener(view -> {
            String medicationTiming = preMedsButton.getTitle();
            symptomLoggingListener.onMedicationTimingChanged(trackingItem, medicationTiming);
        });
    }

    private void setPostMedsButtonListener(@NonNull TrackingItem trackingItem) {
        MPowerRadioButton postMedsButton = this.widget.getPostMedsButton();
        postMedsButton.setOnClickListener(view -> {
            String medicationTiming = postMedsButton.getTitle();
            symptomLoggingListener.onMedicationTimingChanged(trackingItem, medicationTiming);
        });
    }

    private void setAddNoteButtonListener(@NonNull TrackingItem trackingItem) {
        this.widget.getAddNoteButton().setOnClickListener(view -> symptomLoggingListener.onNoteButtonPressed(trackingItem));
    }

    private void updateMedicationTimingUI(String medicationTiming) {
        MPowerRadioButton preMedsButton = this.widget.getPreMedsButton();
        MPowerRadioButton postMedsButton = this.widget.getPostMedsButton();
        if (medicationTiming == null) {
            preMedsButton.setSelected(false);
            postMedsButton.setSelected(false);
        } else {
            boolean preMedsSelected = medicationTiming.equals(preMedsButton.getTitle());
            preMedsButton.setSelected(preMedsSelected);
            postMedsButton.setSelected(!preMedsSelected);
        }
    }

    private void updateDurationUI(String duration) {
        // TODO display user selected duration instead of boolean selected or not
        @StringRes int durationStringRes = duration != null ? R.string.symptoms_logging_duration_button_logged :
                R.string.symptoms_logging_duration_button_default;
        this.widget.getDurationButton().setText(durationStringRes);
    }

    private void updateTimestampUI(Instant newTimestamp) {
        if (newTimestamp == null) {
            this.widget.getTimeButton().setText(R.string.symptoms_logging_time_button_default);
        } else {
            ZoneId zoneId;
            try {
                zoneId = ZoneId.systemDefault();
            } catch (ZoneRulesException e) {
                // UTC time.
                zoneId = ZoneId.of("Z");
            }

            ZonedDateTime newTimestampDateTime = ZonedDateTime.ofInstant(newTimestamp, zoneId);
            String formattedTimestamp = DateTimeFormatter.ofPattern("h:mm a").format(newTimestampDateTime);
            this.widget.getTimeButton().setText(formattedTimestamp);
        }
    }

    private void updateSeverityUI(Integer severity) {
        List<RadioButton> severityButtons = this.widget.getSeverityButtons();
        if (severity == null) {
            for (RadioButton severityButton : severityButtons) {
                severityButton.setChecked(false);
                severityButton.setAlpha(FULL_ALPHA);
            }

            this.previousSelectedSeverityButton = null;
        } else {
            if (this.previousSelectedSeverityButton != null) {
                this.previousSelectedSeverityButton.setChecked(false);
                this.previousSelectedSeverityButton.setAlpha(FADED_ALPHA);
            }
            if (this.previousSelectedSeverityButton == null) {
                // When the first selection is made the buttons all need to be faded.
                for (RadioButton severityButton : severityButtons) {
                    severityButton.setAlpha(FADED_ALPHA);
                }
            }

            RadioButton newlySelectedSeverityButton = severityButtons.get(severity);
            newlySelectedSeverityButton.setAlpha(FULL_ALPHA);
            newlySelectedSeverityButton.setChecked(true);
            this.previousSelectedSeverityButton = newlySelectedSeverityButton;
        }
    }
}
