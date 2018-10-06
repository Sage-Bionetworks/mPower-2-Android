package org.sagebionetworks.research.mpower.tracking.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.sagebionetworks.research.mpower.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MedicationReviewWidget extends ConstraintLayout{
    @BindView(R.id.item_title)
    TextView title;

    @BindView(R.id.time_label)
    TextView timeLabel;

    @BindView(R.id.days_label)
    TextView daysLabel;

    @BindView(R.id.edit_button)
    UnderlinedButton editButton;

    public MedicationReviewWidget(final Context context) {
        super(context);
        commonInit();
    }

    public MedicationReviewWidget(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        commonInit();
    }

    public MedicationReviewWidget(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        commonInit();
    }

    private void commonInit() {
        inflate(getContext(), R.layout.mpower2_medication_review, this);
        ButterKnife.bind(this);
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getTimeLabel() {
        return timeLabel;
    }

    public TextView getDaysLabel() {
        return daysLabel;
    }

    public UnderlinedButton getEditButton() {
        return editButton;
    }
}
