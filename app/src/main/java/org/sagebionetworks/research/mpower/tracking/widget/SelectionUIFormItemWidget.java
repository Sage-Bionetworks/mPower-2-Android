package org.sagebionetworks.research.mpower.tracking.widget;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.sagebionetworks.research.mpower.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectionUIFormItemWidget extends ConstraintLayout {
    @BindView(R.id.background)
    View background;
    @BindView(R.id.item_text)
    TextView text;
    @BindView(R.id.item_detail)
    TextView detail;

    public SelectionUIFormItemWidget(final Context context) {
        super(context);
        this.commonInit();
    }

    public SelectionUIFormItemWidget(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.commonInit();
    }

    public SelectionUIFormItemWidget(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.commonInit();
    }

    public void commonInit() {
        inflate(this.getContext(), R.layout.mpower2_tracking_selection_item, this);
        ButterKnife.bind(this);
    }

    public TextView getText() {
        return this.text;
    }

    public View getBackgroundView() {
        return this.background;
    }

    public TextView getDetail() {
        return this.detail;
    }
}
