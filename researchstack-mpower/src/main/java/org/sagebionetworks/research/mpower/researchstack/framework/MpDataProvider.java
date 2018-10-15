package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;

import org.researchstack.backbone.AppPrefs;
import org.researchstack.backbone.DataProvider;
import org.researchstack.backbone.ResourceManager;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.storage.NotificationHelper;
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider;
import org.sagebionetworks.bridge.researchstack.TaskHelper;
import org.sagebionetworks.bridge.researchstack.wrapper.StorageAccessWrapper;

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

    @Override
    public TaskHelper createTaskHelper(
            NotificationHelper notif,
            StorageAccessWrapper wrapper,
            BridgeManagerProvider provider) {

        // Override to provide our own custom MpTaskHelper
        return new MpTaskHelper(
                wrapper,
                ResourceManager.getInstance(),
                AppPrefs.getInstance(),
                notif,
                provider);
    }
}
