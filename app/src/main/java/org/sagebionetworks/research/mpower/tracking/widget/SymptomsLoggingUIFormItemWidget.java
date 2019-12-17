package org.sagebionetworks.research.mpower.tracking.widget;

import android.content.Context;
import android.graphics.Paint;
import androidx.annotation.ColorRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.MPowerRadioButton;
import org.sagebionetworks.research.mpower.R;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class SymptomsLoggingUIFormItemWidget extends ConstraintLayout {
    @BindView(R.id.item_title)
    TextView title;
    @BindView(R.id.item_detail)
    TextView detail;
    @BindViews({R.id.none_button, R.id.mild_button, R.id.moderate_button, R.id.severe_button})
    List<RadioButton> severityButtons;
    @BindView(R.id.time_button)
    ActionButton timeButton;
    @BindView(R.id.duration_button)
    ActionButton durationButton;
    @BindView(R.id.add_note_button)
    ActionButton addNoteButton;
    @BindView(R.id.pre_meds_button)
    MPowerRadioButton preMedsButton;
    @BindView(R.id.post_meds_button)
    MPowerRadioButton postMedsButton;

    /**
     * @return the color of the text when the severity radio button is selected
     */
    public @ColorRes int getSeveritySelectedTextColorRes() {
        return R.color.severity_selected_text;
    }

    /**
     * @return the color of the text when the severity radio button is not selected
     */
    public @ColorRes int getSeverityUnselectedTextColorRes() {
        return R.color.severity_unselected_text;
    }

    public SymptomsLoggingUIFormItemWidget(final Context context) {
        super(context);
        this.commonInit();
    }

    public SymptomsLoggingUIFormItemWidget(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.commonInit();
    }

    public SymptomsLoggingUIFormItemWidget(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.commonInit();
    }

    private void commonInit() {
        LayoutInflater.from(this.getContext()).inflate(R.layout.mpower2_symptoms_logging_item, this);
        ButterKnife.bind(this);
        this.timeButton.setPaintFlags(this.title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.durationButton.setPaintFlags(this.detail.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    public TextView getTitle() {
        return this.title;
    }

    public TextView getDetail() {
        return this.detail;
    }

    public List<RadioButton> getSeverityButtons() {
        return this.severityButtons;
    }

    public ActionButton getAddNoteButton() {
        return this.addNoteButton;
    }

    public ActionButton getTimeButton() {
        return this.timeButton;
    }

    public ActionButton getDurationButton() {
        return this.durationButton;
    }

    public MPowerRadioButton getPreMedsButton() {
        return this.preMedsButton;
    }

    public MPowerRadioButton getPostMedsButton() {
        return this.postMedsButton;
    }
}
