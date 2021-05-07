package org.sagebionetworks.research.mpower.researchstack.framework;

import static com.google.common.base.Preconditions.checkNotNull;

import static org.sagebionetworks.bridge.researchstack.ApiUtils.SUCCESS_DATA_RESPONSE;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import org.sagebionetworks.bridge.android.manager.AuthenticationManager;
import org.sagebionetworks.bridge.researchstack.ApiUtils;
import org.sagebionetworks.bridge.researchstack.ResearchStackDAO;
import org.sagebionetworks.bridge.researchstack.TaskHelper;
import org.sagebionetworks.bridge.researchstack.wrapper.StorageAccessWrapper;
import org.sagebionetworks.bridge.rest.model.Phone;
import org.sagebionetworks.bridge.rest.model.SignUp;
import org.sagebionetworks.researchstack.backbone.AppPrefs;
import org.sagebionetworks.researchstack.backbone.DataProvider;
import org.sagebionetworks.researchstack.backbone.DataResponse;
import org.sagebionetworks.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider;
import org.sagebionetworks.bridge.rest.model.StudyParticipant;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;
import org.sagebionetworks.researchstack.backbone.storage.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MpDataProvider extends BridgeDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(MpDataProvider.class);

    public static final String SHOW_HEART_SNAPSHOT_DATA_GROUP = "show_heartsnapshot";
    public static final String TEST_USER_DATA_GROUP = "test_user";

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

    public Observable<DataResponse> signUpPhone(SignUp signUp) {
        Preconditions.checkNotNull(signUp);
        logger.debug("Called signUp using phone");
        return this.bridgeManagerProvider.getAuthenticationManager().signUp(signUp)
                .andThen(ApiUtils.SUCCESS_DATA_RESPONSE);
    }

    /**
     * @return a custom task helper for mPower
     */
    @Override
    public TaskHelper createTaskHelper(NotificationHelper notif,
            StorageAccessWrapper wrapper, BridgeManagerProvider provider) {

        return new MpTaskHelper(wrapper, MpResourceManager.getInstance(),
                AppPrefs.getInstance(), notif, provider);
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

    public boolean hasHeartSnapshotDataGroup() {
        return getUserDataGroups().contains(SHOW_HEART_SNAPSHOT_DATA_GROUP);
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
