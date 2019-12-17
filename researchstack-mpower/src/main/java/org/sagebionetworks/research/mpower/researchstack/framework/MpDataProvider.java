package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;
import androidx.annotation.NonNull;

import org.sagebionetworks.researchstack.backbone.DataProvider;
import org.sagebionetworks.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider;
import org.sagebionetworks.bridge.rest.model.StudyParticipant;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MpDataProvider extends BridgeDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(MpDataProvider.class);

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
     * Adds user data attributes to the participant and uploads it to bridge
     * @param attributes to add to the user
     */
    public void addUserDataAttributesFromBackgroundSurvey(Map<String, String> attributes) {
        if (!attributes.isEmpty()) {
            StudyParticipant participant = buildParticipantUserDataAttributes(attributes);
            compositeSubscription.add(
                    updateStudyParticipant(participant)
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
