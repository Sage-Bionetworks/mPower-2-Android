package org.sagebionetworks.research.mpower.tracking.recycler_view;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.RadioButton;

import com.google.common.base.Objects;

import org.sagebionetworks.research.mpower.MPowerRadioButton;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.widget.SymptomsLoggingUIFormItemWidget;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.zone.ZoneRulesException;

import java.util.List;

/**
 * View Holder for the Logging Items in the Symptoms task.
 */
public class SymptomsLoggingItemViewHolder extends RecyclerView.ViewHolder {
    public interface SymptomsLoggingListener {
        void onSeverityChanged(@NonNull TrackingItem trackingItem, int position, int severity);

        void onDurationButtonPressed(@NonNull TrackingItem trackingItem, int position);

        void onTimeButtonPressed(@NonNull TrackingItem trackingItem, int position);

        void onMedicationTimingChanged(@NonNull TrackingItem trackingItem, int position, @NonNull String medicationTiming);

        void onNoteButtonPressed(@NonNull TrackingItem trackingItem, int position);
    }

    private static final float FULL_ALPHA = 1.0f;

    private static final float FADED_ALPHA = .35f;

    private SymptomsLoggingUIFormItemWidget widget;

    private RadioButton previousSelectedSeverityButton;

    private SymptomLog log;

    @NonNull
    private SymptomsLoggingListener symptomLoggingListener;

    public SymptomsLoggingItemViewHolder(final SymptomsLoggingUIFormItemWidget itemView,
            @NonNull final SymptomsLoggingListener symptomLoggingListener) {
        super(itemView);
        widget = itemView;
        this.symptomLoggingListener = symptomLoggingListener;
    }

    public void setContent(@NonNull SimpleTrackingItemConfig config, @Nullable SymptomLog log, int position) {
        TrackingItem trackingItem = config.getTrackingItem();
        updateLog(log);
        // Setup the title and detail labels
        widget.getTitle().setText(trackingItem.getIdentifier());
        String detail = trackingItem.getDetail();
        if (detail != null) {
            widget.getDetail().setVisibility(View.VISIBLE);
            widget.getDetail().setText(detail);
        } else {
            widget.getDetail().setVisibility(View.GONE);
        }

        // Setup the button listeners.
        setSeverityButtonListeners(trackingItem, position);
        setTimeButtonListener(trackingItem, position);
        setDurationButtonListener(trackingItem, position);
        setPreMedsButtonListener(trackingItem, position);
        setPostMedsButtonListener(trackingItem, position);
        setAddNoteButtonListener(trackingItem, position);
    }

    public void updateLog(@Nullable SymptomLog newLog) {
        Integer severity;
        String medicationTiming;
        String duration;
        Instant time;
        if (newLog == null) {
            severity = null;
            medicationTiming = null;
            duration = null;
            time = null;
        } else {
            severity = newLog.getSeverity();
            medicationTiming = newLog.getMedicationTiming();
            duration = newLog.getDuration();
            time = newLog.getTimestamp();
        }

        // update the UI if the values have changed.
        if (log == null || !Objects.equal(log.getSeverity(), severity)) {
            updateSeverityUI(severity);
        }

        if (log == null || !Objects.equal(log.getMedicationTiming(), medicationTiming)) {
            updateMedicationTimingUI(medicationTiming);
        }

        if (log == null || !Objects.equal(log.getDuration(), duration)) {
            updateDurationUI(duration);
        }

        if (log == null || !Objects.equal(log.getTimestamp(), time)) {
            updateTimestampUI(time);
        }

        log = newLog;


    }

    private void setSeverityButtonListeners(@NonNull TrackingItem trackingItem, int position) {
        List<RadioButton> severityButtons = widget.getSeverityButtons();
        for (int i = 0; i < severityButtons.size(); i++) {
            RadioButton severityButton = severityButtons.get(i);
            final int copy = i;
            severityButton.setOnClickListener(view -> symptomLoggingListener.onSeverityChanged(trackingItem, position, copy));
        }
    }

    private void setTimeButtonListener(@NonNull TrackingItem trackingItem, int position) {
        widget.getTimeButton().setOnClickListener(view -> symptomLoggingListener.onTimeButtonPressed(trackingItem, position));
    }

    private void setDurationButtonListener(@NonNull TrackingItem trackingItem, int position) {
        widget.getDurationButton().setOnClickListener(view -> symptomLoggingListener.onDurationButtonPressed(trackingItem, position));
    }

    private void setPreMedsButtonListener(@NonNull TrackingItem trackingItem, int position) {
        MPowerRadioButton preMedsButton = widget.getPreMedsButton();
        preMedsButton.setOnClickListener(view -> {
            String medicationTiming = preMedsButton.getTitle();
            symptomLoggingListener.onMedicationTimingChanged(trackingItem, position, medicationTiming);
        });
    }

    private void setPostMedsButtonListener(@NonNull TrackingItem trackingItem, int position) {
        MPowerRadioButton postMedsButton = widget.getPostMedsButton();
        postMedsButton.setOnClickListener(view -> {
            String medicationTiming = postMedsButton.getTitle();
            symptomLoggingListener.onMedicationTimingChanged(trackingItem, position, medicationTiming);
        });
    }

    private void setAddNoteButtonListener(@NonNull TrackingItem trackingItem, int position) {
        this.widget.getAddNoteButton().setOnClickListener(view -> symptomLoggingListener.onNoteButtonPressed(trackingItem, position));
    }

    private void updateMedicationTimingUI(String medicationTiming) {
        MPowerRadioButton preMedsButton = widget.getPreMedsButton();
        MPowerRadioButton postMedsButton = widget.getPostMedsButton();
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
        widget.getDurationButton().setText(durationStringRes);
    }

    private void updateTimestampUI(Instant newTimestamp) {
        if (newTimestamp == null) {
            widget.getTimeButton().setText(R.string.symptoms_logging_time_button_default);
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
            widget.getTimeButton().setText(formattedTimestamp);
        }
    }

    private void updateSeverityUI(Integer severity) {
        List<RadioButton> severityButtons = widget.getSeverityButtons();
        if (severity == null) {
            for (RadioButton severityButton : severityButtons) {
                severityButton.setChecked(false);
                severityButton.setAlpha(FULL_ALPHA);
            }

            previousSelectedSeverityButton = null;
        } else {
            if (previousSelectedSeverityButton != null) {
                previousSelectedSeverityButton.setChecked(false);
                previousSelectedSeverityButton.setAlpha(FADED_ALPHA);
            }
            if (previousSelectedSeverityButton == null) {
                // When the first selection is made the buttons all need to be faded.
                for (RadioButton severityButton : severityButtons) {
                    severityButton.setAlpha(FADED_ALPHA);
                }
            }

            RadioButton newlySelectedSeverityButton = severityButtons.get(severity);
            newlySelectedSeverityButton.setAlpha(FULL_ALPHA);
            newlySelectedSeverityButton.setChecked(true);
            previousSelectedSeverityButton = newlySelectedSeverityButton;
        }
    }
}
