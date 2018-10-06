package org.sagebionetworks.research.mpower.tracking.widget;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A Widget which contains all of the UI components in a one of the items of the MedicationAddDetails screen.
 */
public class MedicationAddDetailsWidget extends ConstraintLayout {
    @BindView(R.id.item_title)
    TextView title;

    @BindView(R.id.chevron)
    ActionButton chevron;

    public MedicationAddDetailsWidget(final Context context) {
        super(context);
        this.commonInit();
    }

    public MedicationAddDetailsWidget(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.commonInit();
    }

    public MedicationAddDetailsWidget(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.commonInit();
    }

    private void commonInit() {
        inflate(getContext(), R.layout.mpower2_medication_add_details, this);
        ButterKnife.bind(this);
    }

    public TextView getTitle() {
        return title;
    }

    public ActionButton getChevron() {
        return chevron;
    }
}
