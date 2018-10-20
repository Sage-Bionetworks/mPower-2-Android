package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;
import android.support.annotation.NonNull;

import org.researchstack.backbone.DataProvider;
import org.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;

import java.util.ArrayList;
import java.util.List;

public class MpDataProvider extends BridgeDataProvider {

    protected MpTaskFactory taskFactory;

    public static MpDataProvider getInstance() {
        DataProvider provider = DataProvider.getInstance();
        if (!(provider instanceof MpDataProvider)) {
            throw new IllegalStateException("This app only works with MpDataProvider");
        }
        return (MpDataProvider) DataProvider.getInstance();
    }

    public MpDataProvider() {
        super(BridgeManagerProvider.getInstance());
        taskFactory = new MpTaskFactory();
        taskHelper.setSurveyFactory(taskFactory);
    }

    @Override
    public void processInitialTaskResult(Context context, TaskResult taskResult) {
        // no op
    }

    /**
     * @return the current status of the user's data groups, empty list of user is not signed in
     */
    public @NonNull List<String> getUserDataGroups() {
        UserSessionInfo sessionInfo = bridgeManagerProvider.getAuthenticationManager().getUserSessionInfo();
        if (sessionInfo == null || sessionInfo.getDataGroups() == null) {
            return new ArrayList<>();
        }
        return sessionInfo.getDataGroups();
    }
}
