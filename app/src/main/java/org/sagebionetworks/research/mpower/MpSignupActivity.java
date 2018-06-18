package org.sagebionetworks.research.mpower;

import android.os.Bundle;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.ActiveTaskActivity;

public class MpSignupActivity extends ActiveTaskActivity {
    private static final String LOG_TAG = MpSignupActivity.class.getCanonicalName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onDataReady(); // TODO: Do this right
    }

    @Override
    protected void requestStorageAccess() {
        // TODO: remove this
    }

    @Override
    public void showStep(Step step, boolean alwaysReplaceView) {
        super.showStep(step, alwaysReplaceView);
    }
}
