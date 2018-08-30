package org.sagebionetworks.research.mpower.tracking.recycler_view;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioButton;

import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.MPowerRadioButton;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.fragment.DurationFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomAddNoteFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TimePickerFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
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
    private static final float FULL_ALPHA = 1.0f;
    private static final float FADED_ALPHA = .35f;

    private TrackingTaskViewModel<SimpleTrackingItemConfig, SymptomLog> viewModel;
    private SymptomsLoggingUIFormItemWidget widget;
    private SymptomLoggingFragment symptomLoggingFragment;
    private LiveData<SymptomLog> logLiveData;
    private LiveData<Integer> severityLiveData;
    private LiveData<Instant> timestampLiveData;
    private LiveData<String> durationLiveData;
    private LiveData<String> medicationTimingLiveData;
    private RadioButton previousSelectedSeverityButton;

    public SymptomsLoggingItemViewHolder(final SymptomsLoggingUIFormItemWidget itemView, final
    TrackingTaskViewModel<SimpleTrackingItemConfig, SymptomLog> viewModel, final SymptomLoggingFragment symptomLoggingFragment) {
        super(itemView);
        this.widget = itemView;
        this.viewModel = viewModel;
        this.symptomLoggingFragment = symptomLoggingFragment;
    }

    public void setContent(@NonNull SimpleTrackingItemConfig config) {
        TrackingItem trackingItem = config.getTrackingItem();
        this.logLiveData = Transformations.map(this.viewModel.getLoggedElements(), elements -> {
            for (SymptomLog log : elements) {
                if (log.getTrackingItem().getIdentifier().equals(trackingItem.getIdentifier())) {
                    return log;
                }
            }

            return null;
        });

        this.severityLiveData = Transformations.map(this.logLiveData, log -> log != null ? log.getSeverity() : null);
        this.severityLiveData.observe(this.symptomLoggingFragment, this::updateSeverityUI);
        this.timestampLiveData = Transformations.map(this.logLiveData, log -> log != null ? log.getTimestamp() : null);
        this.timestampLiveData.observe(this.symptomLoggingFragment, this::updateTimestampUI);
        this.durationLiveData = Transformations.map(this.logLiveData, log -> log != null ? log.getDuration() : null);
        this.durationLiveData.observe(this.symptomLoggingFragment, this::updateDurationUI);
        this.medicationTimingLiveData = Transformations.map(this.logLiveData, log -> log != null ? log.getMedicationTiming() : null);
        this.medicationTimingLiveData.observe(this.symptomLoggingFragment, this::updateMedicationTimingUI);

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

    private void setSeverityButtonListeners(@NonNull TrackingItem trackingItem) {
        List<RadioButton> severityButtons = this.widget.getSeverityButtons();
        for (int i = 0; i < severityButtons.size(); i++) {
            RadioButton severityButton = severityButtons.get(i);
            final int copy = i;
            severityButton.setOnClickListener(view -> {
                SymptomLog log = this.getPreviousLogOrInstantiate(trackingItem).toBuilder()
                        .setSeverity(copy)
                        .build();
                this.viewModel.addLoggedElement(log);
            });
        }
    }

    private void setTimeButtonListener(@NonNull TrackingItem trackingItem) {
        this.widget.getTimeButton().setOnClickListener(view -> {
            // Done to ensure a log is instantiated before the add note fragment gets added.
            SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
            this.viewModel.addLoggedElement(previousLog);
            TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(this.viewModel.getStepView(), trackingItem);
            timePickerFragment.show(this.symptomLoggingFragment.getFragmentManager(), null);
        });
    }

    private void setDurationButtonListener(@NonNull TrackingItem trackingItem) {
        this.widget.getDurationButton().setOnClickListener(view -> {
            DurationFragment durationFragment = DurationFragment.newInstance(this.viewModel.getStepView(), trackingItem);
            // Done to ensure a log is instantiated before the add note fragment gets added.
            SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
            this.viewModel.addLoggedElement(previousLog);
            this.symptomLoggingFragment.addChildFragmentOnTop(durationFragment, SymptomLoggingFragment.SYMPTOM_LOGGING_FRAGMENT_TAG);
        });
    }

    private void setPreMedsButtonListener(@NonNull TrackingItem trackingItem) {
        MPowerRadioButton preMedsButton = this.widget.getPreMedsButton();
        preMedsButton.setOnClickListener(view -> {
            String medicationTiming = preMedsButton.getTitle();
            SymptomLog log = this.getPreviousLogOrInstantiate(trackingItem).toBuilder()
                    .setMedicationTiming(medicationTiming)
                    .build();
            this.viewModel.addLoggedElement(log);
        });
    }

    private void setPostMedsButtonListener(@NonNull TrackingItem trackingItem) {
        MPowerRadioButton postMedsButton = this.widget.getPostMedsButton();
        postMedsButton.setOnClickListener(view -> {
            String medicationTiming = postMedsButton.getTitle();
            SymptomLog log = this.getPreviousLogOrInstantiate(trackingItem).toBuilder()
                    .setMedicationTiming(medicationTiming)
                    .build();
            this.viewModel.addLoggedElement(log);
        });
    }

    private void setAddNoteButtonListener(@NonNull TrackingItem trackingItem) {
        this.widget.getAddNoteButton().setOnClickListener(view -> {
            SymptomAddNoteFragment addNoteFragment =
                    SymptomAddNoteFragment.newInstance(this.viewModel.getStepView(), trackingItem);
            // Done to ensure a log is instantiated before the add note fragment gets added.
            SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
            this.viewModel.addLoggedElement(previousLog);
            this.symptomLoggingFragment.addChildFragmentOnTop(addNoteFragment, SymptomLoggingFragment.SYMPTOM_LOGGING_FRAGMENT_TAG);
        });
    }

    @NonNull
    private SymptomLog getPreviousLogOrInstantiate(@NonNull TrackingItem trackingItem) {
        SymptomLog result = this.viewModel.getLog(trackingItem);
        result = createLogIfNull(result, trackingItem);
        return result;
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
            } if (this.previousSelectedSeverityButton == null) {
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


    @NonNull
    private static SymptomLog createLogIfNull(@Nullable SymptomLog symptomLog, @NonNull TrackingItem trackingItem) {
        if (symptomLog != null) {
            return symptomLog;
        }

        return SymptomLog.builder().setTrackingItem(trackingItem).setTimestamp(Instant.now()).build();
    }
}