package org.sagebionetworks.research.mpower.researchstack.framework;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.researchstack.backbone.AppPrefs;
import org.researchstack.backbone.DataProvider;
import org.researchstack.backbone.ResourceManager;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.storage.NotificationHelper;
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.bridge.android.manager.upload.ArchiveUtil;
import org.sagebionetworks.bridge.data.JsonArchiveFile;
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider;
import org.sagebionetworks.bridge.researchstack.TaskHelper;
import org.sagebionetworks.bridge.researchstack.wrapper.StorageAccessWrapper;
import org.sagebionetworks.bridge.rest.RestUtils;
import org.sagebionetworks.bridge.rest.model.ScheduledActivity;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;

import java.util.List;
import java.util.Map;

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
    protected JsonArchiveFile createMetaDataFile(
            ScheduledActivity associatedSchedule, List<String> dataGroups) {

        UserSessionInfo sessionInfo = getUserSessionInfo();
        if (sessionInfo != null) {
            String externalId = sessionInfo.getExternalId();
            ImmutableList<String> sessionDataGroups = ImmutableList.copyOf(sessionInfo.getDataGroups());
            return createMetaDataFile(associatedSchedule, sessionDataGroups, externalId);
        } else {
            return super.createMetaDataFile(associatedSchedule, dataGroups);
        }
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

    @NonNull
    protected JsonArchiveFile createMetaDataFile(
            @NonNull ScheduledActivity scheduledActivity,
            @NonNull ImmutableList<String> dataGroups,
            @Nullable String externalId) {

        checkNotNull(scheduledActivity);
        checkNotNull(dataGroups);

        Map<String, Object> metaDataMap = ArchiveUtil.createMetaDataInfoMap(scheduledActivity, dataGroups);

        // Here we can add some of our own additional metadata to the uplaod

        if (externalId != null) {
            metaDataMap.put("externalId", externalId);
        }

        if (scheduledActivity.getActivity() != null &&
                scheduledActivity.getActivity().getSurvey() != null &&
                scheduledActivity.getActivity().getSurvey().getIdentifier() != null) {
            // Add survey identifier as taskIdentifier even though it's a survey (base implementation doesn't do this)
            metaDataMap.put("taskIdentifier", scheduledActivity.getActivity().getSurvey().getIdentifier());
        }

        String deviceTypeInfoStr =
                Build.PRODUCT + " " + Build.MODEL + " OS v" + android.os.Build.VERSION.SDK_INT;
        metaDataMap.put("deviceTypeIdentifier", deviceTypeInfoStr);

        // Grab the end date
        DateTime endDate = DateTime.now();
        if (scheduledActivity.getFinishedOn() != null) {
            endDate = scheduledActivity.getFinishedOn();
        }

        String metaDataJson = RestUtils.GSON.toJson(metaDataMap);
        return new JsonArchiveFile("metadata.json", endDate, metaDataJson);
    }
}
