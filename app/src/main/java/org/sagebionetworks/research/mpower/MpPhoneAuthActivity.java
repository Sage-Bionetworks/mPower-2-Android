package org.sagebionetworks.research.mpower;

import static org.sagebionetworks.research.mpower.research.MpIdentifier.AUTHENTICATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Strings;

import org.researchstack.backbone.ResearchStack;
import org.researchstack.backbone.factory.IntentFactory;
import org.researchstack.backbone.task.Task;
import org.sagebionetworks.bridge.android.manager.AuthenticationManager;
import org.sagebionetworks.bridge.android.manager.dao.AccountDAO;
import org.sagebionetworks.bridge.rest.model.Phone;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;
import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider;
import org.sagebionetworks.research.mpower.researchstack.framework.MpResourceManager;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;
import org.sagebionetworks.research.mpower.researchstack.framework.MpViewTaskActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import rx.subscriptions.CompositeSubscription;

public class MpPhoneAuthActivity extends DaggerAppCompatActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MpPhoneAuthActivity.class);

    protected MpTaskFactory taskFactory = new MpTaskFactory();

    @Inject
    TaskLauncher taskLauncher;

    @Inject
    ResearchStack researchStack;

    @Inject
    MpDataProvider provider;

    @Inject
    AccountDAO accountDAO;

    @Inject
    AuthenticationManager authenticationManager;

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.debug("onCreate called");

        setContentView(org.sagebionetworks.research.mpower.researchstack.R.layout.mp_activity_main);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LOGGER.debug("onNewIntent called");
        handleIntent(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    protected void startSignupTask() {
        taskLauncher.launchTask(this, AUTHENTICATE, null);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            // We came in via a link! Let's do something with it
            final String token = appLinkData.getLastPathSegment();

            String phoneRegion = accountDAO.getPhoneRegion();
            String phoneNumber = accountDAO.getPhoneNumber();

            boolean newSignIn = false;
            UserSessionInfo currentSession = authenticationManager.getUserSessionInfo();
            if (currentSession == null) {
                newSignIn = true;
            } else {
                Phone phone = currentSession.getPhone();
                newSignIn = !isSamePhone(phone, phoneRegion, phoneNumber);
            }

            // either we have no current session, or current session is for a different phone number
            if (newSignIn) {
                doPhoneSignIn(token, phoneRegion, phoneNumber);
            } else {
                compositeSubscription.add(
                        authenticationManager.getLatestUserSessionInfo()
                                .subscribe(session -> {
                                    LOGGER.debug("Session renewal succeeded.");
                                    // current session is still useable
                                    returnToEntryActivity();
                                }, t -> {
                                    LOGGER.debug("Session renewal failed, signing in.");
                                    doPhoneSignIn(token, phoneRegion, phoneNumber);
                                }));
            }
        } else {
            startSignupTask();
        }
    }

    @VisibleForTesting
    boolean isSamePhone(Phone expected, String phoneRegion, String phoneNumber) {
        return expected.getRegionCode().equals(phoneRegion)
                && expected.getNumber().endsWith(phoneNumber);
    }

    private void doPhoneSignIn(String token, String phoneRegion, String phoneNumber) {
        if (Strings.isNullOrEmpty(phoneRegion) || Strings.isNullOrEmpty(phoneNumber)) {
            LOGGER.error("Phone number and region are required and were not found in accountDAO");
            return;
        }

        compositeSubscription.add(provider.signInWithPhoneAndToken(phoneRegion, phoneNumber, token)
                .subscribe(session -> {
                    LOGGER.debug("TokenLoginSubscribe: Authenticated Login Complete");
                    returnToEntryActivity();
                }, throwable -> {
                    LOGGER.error("Sign up failed", throwable);

                    // TODO: handle errors here instead of relying on parent activity
                    returnToEntryActivity();
                }));
    }

    @VisibleForTesting
    void returnToEntryActivity() {
        Intent intent = new Intent(this, EntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
