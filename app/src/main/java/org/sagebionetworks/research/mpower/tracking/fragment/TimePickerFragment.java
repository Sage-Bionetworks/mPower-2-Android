package org.sagebionetworks.research.mpower.tracking.fragment;

import static com.google.common.base.Preconditions.checkState;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.joda.time.Hours;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModelFactory;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.TemporalUnit;
import org.threeten.bp.zone.ZoneRulesException;

import java.util.Calendar;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class TimePickerFragment extends DialogFragment {
    public static final String ARGUMENT_STEP_VIEW = "stepView";
    public static final String ARGUMENT_TRACKING_ITEM = "trackingItem";

    @Inject
    protected TrackingTaskViewModelFactory trackingActiveTaskViewModelFactory;
    private SymptomTrackingTaskViewModel viewModel;
    private TrackingItem trackingItem;
    private OnTimeSetListener listener = (view, hour, minute) -> {
        SymptomLog log = this.viewModel.getLog(this.trackingItem);
        ZoneId zoneId;
        try {
            zoneId = ZoneId.systemDefault();
        } catch (ZoneRulesException e) {
            // UTC time.
            zoneId = ZoneId.of("Z");
        }

        ZonedDateTime startOfToday = ZonedDateTime.ofInstant(Instant.now(), zoneId).toLocalDate().atStartOfDay(zoneId);
        Instant timestamp = startOfToday.toInstant().plus(hour, ChronoUnit.HOURS).plus(minute, ChronoUnit.MINUTES);
        log = log.toBuilder()
                .setTimestamp(timestamp)
                .build();
        this.viewModel.addLoggedElement(log);
    };

    public static TimePickerFragment newInstance(@NonNull TrackingStepView trackingStepView, @NonNull TrackingItem trackingItem) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_STEP_VIEW, trackingStepView);
        args.putParcelable(ARGUMENT_TRACKING_ITEM, trackingItem);
        timePickerFragment.setArguments(args);
        return timePickerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TrackingStepView stepView = null;
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                // noinspection unchecked
                stepView = (TrackingStepView)this.getArguments().getSerializable(ARGUMENT_STEP_VIEW);
                this.trackingItem = this.getArguments().getParcelable(ARGUMENT_TRACKING_ITEM);
            }
        } else {
            // noinspection unchecked
            stepView = (TrackingStepView)savedInstanceState.getSerializable(ARGUMENT_STEP_VIEW);
            this.trackingItem = savedInstanceState.getParcelable(ARGUMENT_TRACKING_ITEM);
        }

        checkState(stepView != null, "stepView cannot be null");
        // noinspection unchecked
        this.viewModel =
                (SymptomTrackingTaskViewModel) ViewModelProviders.of(this.getParentFragment(), this.trackingActiveTaskViewModelFactory.create(stepView))
                        .get(TrackingTaskViewModel.class);
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this.listener, hour, minute, false);
    }
}
