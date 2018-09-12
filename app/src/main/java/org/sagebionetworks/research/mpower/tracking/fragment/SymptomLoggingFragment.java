package org.sagebionetworks.research.mpower.tracking.fragment;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SymptomsLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SymptomsLoggingItemViewHolder;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.zone.ZoneRulesException;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A subclass of LoggingFragment specific to the Symptoms task.
 */
public class SymptomLoggingFragment extends
        LoggingFragment<SimpleTrackingItemConfig, SymptomLog, SymptomTrackingTaskViewModel> {
    public static final String SYMPTOM_LOGGING_FRAGMENT_TAG = "symptomLoggingFragment";

    @NonNull
    public static SymptomLoggingFragment newInstance(@NonNull StepView stepView) {
        SymptomLoggingFragment fragment = new SymptomLoggingFragment();
        Bundle args = TrackingFragment.createArguments(stepView);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        return SymptomSelectionFragment.newInstance(this.stepView);
    }

    @NonNull
    @Override
    public Adapter<?> initializeAdapter() {
        SymptomsLoggingItemViewHolder.SymptomsLoggingListener symptomsLoggingListener
                = new SymptomsLoggingItemViewHolder.SymptomsLoggingListener() {
            @Override
            public void onSeverityChanged(@NonNull final TrackingItem trackingItem, final int severity) {
                SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
                viewModel.addLoggedElement(previousLog.toBuilder()
                        .setSeverity(severity)
                        .build());
            }

            @Override
            public void onDurationButtonPressed(@NonNull final TrackingItem trackingItem) {
                String title = getResources().getString(R.string.duration_fragment_title);
                String detail = getResources().getString(R.string.duration_fragment_detail);
                final SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
                String previousSelection = previousLog != null ? previousLog.getDuration() : null;
                DurationFragment durationFragment = DurationFragment.newInstance(title, detail, previousSelection);
                durationFragment.setOnDurationChangeListener(duration -> {
                    viewModel.addLoggedElement(previousLog.toBuilder()
                            .setDuration(duration)
                            .build());
                });

                addChildFragmentOnTop(durationFragment, SymptomLoggingFragment.SYMPTOM_LOGGING_FRAGMENT_TAG);
            }

            @Override
            public void onTimeButtonPressed(@NonNull final TrackingItem trackingItem) {
                final Calendar calendar = Calendar.getInstance();
                OnTimeSetListener onTimeSetListener = (timePicker, hour, minute) -> {
                    ZoneId zoneId;
                    try {
                        zoneId = ZoneId.systemDefault();
                    } catch (ZoneRulesException e) {
                        // UTC time.
                        zoneId = ZoneId.of("Z");
                    }

                    Instant startOfDay = ZonedDateTime.ofInstant(Instant.now(), zoneId).toLocalDate()
                            .atStartOfDay(zoneId)
                            .toInstant();
                    Instant selectedInstant = startOfDay.plus(hour, ChronoUnit.HOURS)
                            .plus(minute, ChronoUnit.MINUTES);

                    SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
                    viewModel.addLoggedElement(previousLog.toBuilder()
                            .setTimestamp(selectedInstant)
                            .build());
                };

                new TimePickerDialog(getContext(), onTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), false).show();
            }

            @Override
            public void onMedicationTimingChanged(@NonNull final TrackingItem trackingItem,
                    @NonNull final String medicationTiming) {
                SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
                viewModel.addLoggedElement(previousLog.toBuilder()
                        .setMedicationTiming(medicationTiming)
                        .build());
            }

            @Override
            public void onNoteButtonPressed(@NonNull final TrackingItem trackingItem) {
                String title = getResources().getString(R.string.add_note_fragment_title);
                String text = "";
                final SymptomLog previousLog = getSymptomLogData(trackingItem).getValue();
                String previousNote = previousLog != null ? previousLog.getNote() : null;
                AddNoteFragment addNoteFragment = AddNoteFragment.newInstance(title, text, previousNote);
                addNoteFragment.setOnNoteChangeListener(note -> previousLog.toBuilder()
                        .setNote(note)
                        .build());

                addChildFragmentOnTop(addNoteFragment,
                        SymptomLoggingFragment.SYMPTOM_LOGGING_FRAGMENT_TAG);
            }

            @Override
            public LiveData<SymptomLog> getSymptomLogData(@NonNull final TrackingItem trackingItem) {
                return viewModel.getLoggedElement(trackingItem.getIdentifier());
            }

            private SymptomLog getPreviousLogOrInstantiate(@NonNull TrackingItem trackingItem) {
                SymptomLog previousLog = viewModel.getLoggedElementsById().getValue().get(trackingItem.getIdentifier());
                if (previousLog != null) {
                    return previousLog;
                }

                return SymptomLog.builder()
                        .setIdentifier(trackingItem.getIdentifier())
                        .setText(trackingItem.getIdentifier())
                        .setTimestamp(Instant.now())
                        .build();

            }
        };

        return new SymptomsLoggingItemAdapter(symptomsLoggingListener, getLifecycle(),
                new ArrayList<>(viewModel.getActiveElementsById().getValue().values()));
    }
}
