package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;
import android.support.annotation.NonNull;

import org.researchstack.backbone.DataProvider;
import org.researchstack.backbone.result.Result;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider;
import org.sagebionetworks.bridge.researchstack.TaskHelper;
import org.sagebionetworks.bridge.rest.model.StudyParticipant;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MpDataProvider extends BridgeDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(MpDataProvider.class);

    public static String RESULT_IDENTIFIER_MPOWER_1_EMAIL = "mPower1Email";
    protected MpTaskFactory taskFactory;
    protected CompositeSubscription compositeSubscription = new CompositeSubscription();

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

    /**
     * This is a work-around for storing the result of a survey's question in the user data attributes
     * Instead of uploading it to synapse or including it in a report
     * @param taskResult from running the background survey
     */
    public void addUserDataAttributesFromBackgroundSurvey(TaskResult taskResult) {
        Map<String, String> attributes = new HashMap<>();
        List<Result> flattenedResultList = TaskHelper.flattenResults(taskResult);
        for (Result result : flattenedResultList) {
            // Add the mPower1Email as a string to the user attributes map
            if (RESULT_IDENTIFIER_MPOWER_1_EMAIL.equals(result.getIdentifier()) &&
                    result instanceof StepResult &&
                    ((StepResult)result).getResult() instanceof String) {
                attributes.put(result.getIdentifier(), (String)((StepResult)result).getResult());
            }
        }
        if (!attributes.isEmpty()) {
            compositeSubscription.add(
                    updateStudyParticipant(buildParticipantUserDataAttributes(attributes))
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(userSessionInfo -> {
                        logger.info("Successfully updated user data attributes for background survey");
                    }, throwable -> {
                        logger.warn("Error updating user attributes for background survey " +
                                throwable.getLocalizedMessage());
                    }));
        }
    }

    private StudyParticipant buildParticipantUserDataAttributes(Map<String, String> userDataAttributes) {
        StudyParticipant participant = new StudyParticipant();
        participant.setDataGroups(null);
        participant.setNotifyByEmail(null);
        participant.setLanguages(null);
        participant.setRoles(null);
        participant.setAttributes(userDataAttributes);
        return participant;
    }
}
