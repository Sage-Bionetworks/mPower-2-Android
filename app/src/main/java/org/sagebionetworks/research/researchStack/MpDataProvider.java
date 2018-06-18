package org.sagebionetworks.research.researchStack;

import android.content.Context;
import org.researchstack.backbone.DataProvider;
import org.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider;

public class MpDataProvider extends BridgeDataProvider {

    protected MpTaskFactory taskFactory;

    public static MpDataProvider getInstance() {
        DataProvider provider = DataProvider.getInstance();
        if (!(provider instanceof MpDataProvider)) {
            throw new IllegalStateException("This app only works with MpDataProvider");
        }
        return  (MpDataProvider)DataProvider.getInstance();
    }

    @Override
    public void processInitialTaskResult(Context context, TaskResult taskResult) {
        // no op
    }

    public MpDataProvider() {
        super(BridgeManagerProvider.getInstance());
        taskFactory = new MpTaskFactory();
//
//        resetManagers();
    }
}
