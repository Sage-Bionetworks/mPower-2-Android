package org.sagebionetworks.research.mpower.tracking.recycler_view;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioButton;

import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.MPowerRadioButton;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomAddNoteFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.widget.SymptomsLoggingUIFormItemWidget;
import org.threeten.bp.Instant;

import java.util.List;

public class SymptomsLoggingItemViewHolder extends RecyclerView.ViewHolder {
    private static final float FULL_ALPHA = 1.0f;
    private static final float FADED_ALPHA = .35f;

    private TrackingTaskViewModel<SimpleTrackingItemConfig, SymptomLog> viewModel;
    private SymptomsLoggingUIFormItemWidget widget;
    private SymptomLoggingFragment symptomLoggingFragment;

    public SymptomsLoggingItemViewHolder(final SymptomsLoggingUIFormItemWidget itemView, final
    TrackingTaskViewModel<SimpleTrackingItemConfig, SymptomLog> viewModel, final SymptomLoggingFragment symptomLoggingFragment) {
        super(itemView);
        this.widget = itemView;
        this.viewModel = viewModel;
        this.symptomLoggingFragment = symptomLoggingFragment;
    }

    public void setContent(@NonNull SimpleTrackingItemConfig config) {
        TrackingItem trackingItem = config.getTrackingItem();
        // Setup the title and detail labels
        this.widget.getTitle().setText(trackingItem.getIdentifier());
        String detail = trackingItem.getDetail();
        if (detail != null) {
            this.widget.getDetail().setVisibility(View.VISIBLE);
            this.widget.getDetail().setText(detail);
        } else {
            this.widget.getDetail().setVisibility(View.GONE);
        }

        SymptomLog previousLog = this.viewModel.getLog(trackingItem);
        if (previousLog != null) {
            // If we have a previous log we initialize the views to show that logs information.
            this.setContentFromLog(previousLog);
        }

        // Setup the button listeners.
        this.setSeverityButtonListeners(trackingItem);
        // TODO set time button listener to bring up the time wheel.
        // TODO set duration button listener to bring up the duration selection.
        this.setPreMedsButtonListener(trackingItem);
        this.setPostMedsButtonListener(trackingItem);
        this.setAddNoteButtonListener(trackingItem);
    }

    private void setContentFromLog(@NonNull SymptomLog previousLog) {
        List<RadioButton> severityButtons = this.widget.getSeverityButtons();
        ActionButton timeButton = this.widget.getTimeButton();
        ActionButton durationButton = this.widget.getDurationButton();
        MPowerRadioButton preMedsButton = this.widget.getPreMedsButton();
        MPowerRadioButton postMedsButton = this.widget.getPostMedsButton();
        Integer previousSeverity = previousLog.getSeverity();
        // Reset all the severity buttons.
        for (RadioButton severityButton : severityButtons) {
            severityButton.setSelected(false);
            severityButton.setAlpha(FULL_ALPHA);
        }

        if (previousSeverity != null) {
            for (RadioButton severityButton : severityButtons) {
                severityButton.setAlpha(FADED_ALPHA);
            }

            RadioButton selectedButton = severityButtons.get(previousSeverity);
            selectedButton.setChecked(true);
            selectedButton.setAlpha(FULL_ALPHA);
        }
        // TODO set time button title to previously selected time.
        // TODO set duration button to previously selected duration.

        // Set the medication timing selection accordingly.
        String medicationTiming = previousLog.getMedicationTiming();
        if (medicationTiming != null) {
            if (medicationTiming.equals(preMedsButton.getTitle())) {
                preMedsButton.setSelected(true);
                postMedsButton.setSelected(false);
            } else if (medicationTiming.equals(postMedsButton.getTitle())) {
                preMedsButton.setSelected(false);
                postMedsButton.setSelected(true);
            }
        } else {
            preMedsButton.setSelected(false);
            postMedsButton.setSelected(false);
        }
    }

    private void setSeverityButtonListeners(@NonNull TrackingItem trackingItem) {
        List<RadioButton> severityButtons = this.widget.getSeverityButtons();
        for (int i = 0; i < severityButtons.size(); i++) {
            RadioButton severityButton = severityButtons.get(i);
            final int copy = i;
            severityButton.setOnClickListener(view -> {
                for (RadioButton button : severityButtons) {
                    button.setChecked(false);
                    button.setAlpha(FADED_ALPHA);
                }

                severityButton.setAlpha(FULL_ALPHA);
                severityButton.setChecked(true);
                SymptomLog log = this.getPreviousLogOrInstantiate(trackingItem).toBuilder()
                        .setSeverity(copy)
                        .setTimestamp(Instant.now())
                        .build();
                this.viewModel.addLoggedElement(log);
            });
        }
    }

    private void setPreMedsButtonListener(@NonNull TrackingItem trackingItem) {
        MPowerRadioButton preMedsButton = this.widget.getPreMedsButton();
        MPowerRadioButton postMedsButton = this.widget.getPostMedsButton();
        preMedsButton.setOnClickListener(view -> {
            String medicationTiming = preMedsButton.getTitle();
            preMedsButton.setSelected(!preMedsButton.isSelected());
            postMedsButton.setSelected(false);
            SymptomLog log = this.getPreviousLogOrInstantiate(trackingItem).toBuilder()
                    .setMedicationTiming(preMedsButton.isSelected() ? medicationTiming : null)
                    .setTimestamp(Instant.now())
                    .build();
            this.viewModel.addLoggedElement(log);
        });
    }

    private void setPostMedsButtonListener(@NonNull TrackingItem trackingItem) {
        MPowerRadioButton preMedsButton = this.widget.getPreMedsButton();
        MPowerRadioButton postMedsButton = this.widget.getPostMedsButton();
        postMedsButton.setOnClickListener(view -> {
            String medicationTiming = preMedsButton.getTitle();
            preMedsButton.setSelected(false);
            postMedsButton.setSelected(!postMedsButton.isSelected());
            SymptomLog log = this.getPreviousLogOrInstantiate(trackingItem).toBuilder()
                    .setMedicationTiming(postMedsButton.isSelected() ? medicationTiming : null)
                    .setTimestamp(Instant.now())
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

    @NonNull
    private static SymptomLog createLogIfNull(@Nullable SymptomLog symptomLog, @NonNull TrackingItem trackingItem) {
        if (symptomLog != null) {
            return symptomLog;
        }

        return SymptomLog.builder().setTrackingItem(trackingItem).setTimestamp(Instant.now()).build();
    }
}
