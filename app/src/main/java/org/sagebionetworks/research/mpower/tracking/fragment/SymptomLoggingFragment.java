package org.sagebionetworks.research.mpower.tracking.fragment;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.SortUtil;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SymptomsLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SymptomsLoggingItemViewHolder;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.zone.ZoneRulesException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A subclass of LoggingFragment specific to the Symptoms task.
 */
public class SymptomLoggingFragment extends
        LoggingFragment<SimpleTrackingItemConfig, SymptomLog, SymptomTrackingTaskViewModel, SymptomsLoggingItemAdapter> {
    public static final String SYMPTOM_LOGGING_FRAGMENT_TAG = "symptomLoggingFragment";
    private static final Logger LOGGER = LoggerFactory.getLogger(SymptomLoggingFragment.class);

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
    public SymptomsLoggingItemAdapter initializeAdapter() {
        SymptomsLoggingItemViewHolder.SymptomsLoggingListener symptomsLoggingListener
                = new SymptomsLoggingItemViewHolder.SymptomsLoggingListener() {
            @Override
            public void onSeverityChanged(@NonNull final TrackingItem trackingItem, final int position,
                    final int severity) {
                SymptomLog log = getPreviousLogOrInstantiate(trackingItem);
                log = log.toBuilder()
                        .setSeverity(severity)
                        .build();
                adapter.updateLog(position, log);
                adapter.notifyItemChanged(position);
                viewModel.addLoggedElement(log);
            }

            @Override
            public void onDurationButtonPressed(@NonNull final TrackingItem trackingItem, final int position) {
                String title = getResources().getString(R.string.duration_fragment_title);
                String detail = getResources().getString(R.string.duration_fragment_detail);
                final SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
                String previousSelection = previousLog != null ? previousLog.getDuration() : null;
                DurationFragment durationFragment = DurationFragment.newInstance(title, detail, previousSelection);
                durationFragment.setOnDurationChangeListener(duration -> {
                    SymptomLog log = previousLog.toBuilder()
                            .setDuration(duration)
                            .build();
                    adapter.updateLog(position, log);
                    adapter.notifyItemChanged(position);
                    viewModel.addLoggedElement(log);
                });

                addChildFragmentOnTop(durationFragment, SymptomLoggingFragment.SYMPTOM_LOGGING_FRAGMENT_TAG);
            }

            @Override
            public void onTimeButtonPressed(@NonNull final TrackingItem trackingItem, final int position) {
                final Calendar calendar = Calendar.getInstance();
                OnTimeSetListener onTimeSetListener = (timePicker, hour, minute) -> {
                    ZoneId zoneId;
                    try {
                        zoneId = ZoneId.systemDefault();
                    } catch (ZoneRulesException e) {
                        // UTC time.
                        LOGGER.warn("No system default timezone set, using UTC time.");
                        zoneId = ZoneId.of("Z");
                    }

                    Instant startOfDay = ZonedDateTime.ofInstant(Instant.now(), zoneId).toLocalDate()
                            .atStartOfDay(zoneId)
                            .toInstant();
                    Instant selectedInstant = startOfDay.plus(hour, ChronoUnit.HOURS)
                            .plus(minute, ChronoUnit.MINUTES);

                    SymptomLog log = getPreviousLogOrInstantiate(trackingItem);
                    log = log.toBuilder()
                            .setLoggedDate(selectedInstant)
                            .build();
                    adapter.updateLog(position, log);
                    adapter.notifyItemChanged(position);
                    viewModel.addLoggedElement(log);
                };

                new TimePickerDialog(getContext(), onTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), false).show();
            }

            @Override
            public void onMedicationTimingChanged(@NonNull final TrackingItem trackingItem, final int position,
                    @NonNull final String medicationTiming) {
                SymptomLog log = getPreviousLogOrInstantiate(trackingItem);
                log = log.toBuilder()
                        .setMedicationTiming(medicationTiming)
                        .build();
                adapter.updateLog(position, log);
                adapter.notifyItemChanged(position);
                viewModel.addLoggedElement(log);
            }

            @Override
            public void onNoteButtonPressed(@NonNull final TrackingItem trackingItem, final int position) {
                String title = getResources().getString(R.string.add_note_fragment_title);
                String text = "";
                final SymptomLog previousLog = getPreviousLogOrInstantiate(trackingItem);
                String previousNote = previousLog != null ? previousLog.getNote() : null;
                AddNoteFragment addNoteFragment = AddNoteFragment.newInstance(title, text, previousNote);
                addNoteFragment.setOnNoteChangeListener(note -> {
                    SymptomLog log = previousLog.toBuilder()
                            .setNote(note)
                            .build();
                    adapter.updateLog(position, log);
                    adapter.notifyItemChanged(position);
                    viewModel.addLoggedElement(log);
                });

                addChildFragmentOnTop(addNoteFragment,
                        SymptomLoggingFragment.SYMPTOM_LOGGING_FRAGMENT_TAG);
            }

            private SymptomLog getPreviousLogOrInstantiate(@NonNull TrackingItem trackingItem) {
                SymptomLog previousLog = viewModel.getLoggedElementsById().getValue()
                        .get(trackingItem.getIdentifier());
                if (previousLog != null) {
                    return previousLog;
                }

                return SymptomLog.builder()
                        .setIdentifier(trackingItem.getIdentifier())
                        .setText(trackingItem.getIdentifier())
                        .setLoggedDate(Instant.now())
                        .build();

            }
        };

        List<SimpleTrackingItemConfig> activeElements = SortUtil.getActiveElementsSorted(
                viewModel.getActiveElementsById().getValue());
        Map<Integer, SymptomLog> logsByPosition = getLogsByPosition(activeElements);
        return new SymptomsLoggingItemAdapter(activeElements, symptomsLoggingListener, logsByPosition);
    }

    private Map<Integer, SymptomLog> getLogsByPosition(List<SimpleTrackingItemConfig> activeElements) {
        Map<Integer, SymptomLog> result = new HashMap<>();
        for (int i = 0; i < activeElements.size(); i++) {
            SimpleTrackingItemConfig config = activeElements.get(i);
            if (viewModel.getLoggedElementsById().getValue().containsKey(config.getIdentifier())) {
                SymptomLog log = viewModel.getLoggedElementsById().getValue().get(config.getIdentifier());
                result.put(i, log);
            }
        }

        return result;
    }
}
