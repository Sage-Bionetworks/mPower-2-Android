package org.sagebionetworks.research.mpower.tracking.widget;

import android.view.View;

public class PaddingUtil {
    private PaddingUtil() {}

    public static void setTopPadding(View view, int paddingDp) {
        float density = view.getContext().getResources().getDisplayMetrics().density;
        int paddingPixels = (int)(paddingDp * density);
        view.setPadding(view.getPaddingLeft(), paddingPixels, view.getPaddingRight(), view.getPaddingBottom());
    }

    public static void setBottomPadding(View view, int paddingDp) {
        float density = view.getContext().getResources().getDisplayMetrics().density;
        int paddingPixels = (int)(paddingDp * density);
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), paddingPixels);
    }
}
