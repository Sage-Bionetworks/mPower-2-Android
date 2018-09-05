package org.sagebionetworks.research.mpower.tracking.widget;

import android.content.Context;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TriggersLoggingUIFormItemWidget extends ConstraintLayout {
    @BindView(R.id.item_title)
    TextView title;

    @BindView(R.id.record_button)
    ActionButton recordButton;

    @BindView(R.id.checkmark)
    ImageView checkmark;

    @BindView(R.id.recorded_label)
    TextView recordedLabel;

    @BindView(R.id.undo_button)
    ActionButton undoButton;

    private boolean logged;
    private int padding;

    public TriggersLoggingUIFormItemWidget(final Context context) {
        super(context);
        this.commonInit();
    }

    public TriggersLoggingUIFormItemWidget(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.commonInit();
    }

    public TriggersLoggingUIFormItemWidget(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.commonInit();
    }

    public void commonInit() {
        inflate(this.getContext(), R.layout.mpower2_triggers_logging_item, this);
        ButterKnife.bind(this);
        this.padding = this.getResources().getDimensionPixelSize(R.dimen.margin_medium);
        this.undoButton.setPaintFlags(this.undoButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.title.setPadding(this.title.getPaddingLeft(), this.padding, this.title.getPaddingRight(), this.title.getPaddingBottom());
        this.undoButton.setPadding(this.undoButton.getPaddingLeft(), this.undoButton.getPaddingTop(), this.undoButton.getPaddingRight(),
                this.padding);
        this.displayedUnloggedState();
    }

    public TextView getTitle() {
        return this.title;
    }

    public ActionButton getRecordButton() {
        return this.recordButton;
    }

    public ActionButton getUndoButton() {
        return this.undoButton;
    }

    public void setLogged(boolean logged) {
        if (this.logged != logged) {
            this.logged = logged;
            if (this.logged) {
                this.displayLoggedState();
            } else {
                this.displayedUnloggedState();
            }
        }
    }

    private void displayLoggedState() {
        this.title.setPadding(this.title.getPaddingLeft(), this.padding, this.title.getPaddingRight(), 0);
        this.recordButton.setVisibility(View.GONE);
        this.checkmark.setVisibility(View.VISIBLE);
        this.recordedLabel.setVisibility(View.VISIBLE);
        this.undoButton.setVisibility(View.VISIBLE);
    }

    private void displayedUnloggedState() {
        this.title.setPadding(this.title.getPaddingLeft(), this.title.getPaddingTop(), this.title.getPaddingRight(), this.padding);
        this.recordButton.setVisibility(View.VISIBLE);
        this.checkmark.setVisibility(View.GONE);
        this.recordedLabel.setVisibility(View.GONE);
        this.undoButton.setVisibility(View.GONE);
    }
}
