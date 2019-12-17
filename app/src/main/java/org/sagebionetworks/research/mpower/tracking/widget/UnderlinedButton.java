package org.sagebionetworks.research.mpower.tracking.widget;

import android.content.Context;
import android.graphics.Paint;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class UnderlinedButton extends AppCompatTextView {
    public UnderlinedButton(final Context context) {
        super(context);
        commonInit();
    }

    public UnderlinedButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        commonInit();
    }

    public UnderlinedButton(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        commonInit();
    }

    private void commonInit() {
        setPaintFlags(getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }
}
