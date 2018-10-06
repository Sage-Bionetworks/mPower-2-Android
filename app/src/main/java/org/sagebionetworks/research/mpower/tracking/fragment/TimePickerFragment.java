package org.sagebionetworks.research.mpower.tracking.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

import dagger.android.support.AndroidSupportInjection;


/**
 * The time picker fragment allows the user to input a time. In order to receive a callback when the user submits a time
 * the creator of this fragment should call setOnTimeSetListener() at some time before the show() method is called.
 */
public class TimePickerFragment extends DialogFragment {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimePickerFragment.class);
    private OnTimeSetListener onTimeSetListener;

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
        if (this.onTimeSetListener == null) {
            LOGGER.warn("TimePickerFragment dialog created but OnTimeSetListener was null");
        }

        return new TimePickerDialog(getActivity(), this.onTimeSetListener, hour, minute, false);
    }

    /**
     * Sets the OnTimeSetListener to notify when the user submits a time.
     * @param onTimeSetListener the OnTimeSetListener ot notify when the user submits a time.
     */
    public void setOnTimeSetListener(OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener = onTimeSetListener;
    }
}
