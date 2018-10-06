package org.sagebionetworks.research.mpower.researchstack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.researchstack.backbone.ResearchStack;
import org.researchstack.backbone.factory.IntentFactory;
import org.researchstack.backbone.task.Task;
import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider;
import org.sagebionetworks.research.mpower.researchstack.framework.MpResourceManager;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import rx.Subscription;

public class MpMainActivity extends AppCompatActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MpMainActivity.class);

    public static final int SIGN_UP_TASK_CODE = 1600;

    protected MpTaskFactory taskFactory = new MpTaskFactory();

    @Inject
    ResearchStack researchStack;

    String testPhoneNumber = "2063066209";

    String token = "252-345";

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mp_main);

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    protected void startConsentActivity() {
        // For now just launching to a link
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mpower.sagebridge.org/"));
        startActivity(browserIntent);
    }

    protected void startOverviewActivity() {
        // TODO define this
    }

    protected void startSignupTask() {
        Task signupTask = taskFactory.createTask(this, MpResourceManager.SIGNUP_TASK_RESOURCE);
        Intent intent = IntentFactory.INSTANCE.newTaskIntent(
                this, MpSignupActivity.class, signupTask);
        startActivityForResult(intent, SIGN_UP_TASK_CODE);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            // We came in via a link! Let's do something with it
            String token = appLinkData.getLastPathSegment();
            MpDataProvider provider = MpDataProvider.getInstance();
            Subscription subscription = provider.signInWithPhoneAndToken("US", testPhoneNumber, token)
                    .subscribe(dataResponse -> {
                        if (dataResponse.isAuthenticated()) {
                            LOGGER.debug("TokenLoginSubscribe: Authenticated Login Complete");
                            // We were able to log in. Let's go somewhere!
                            // Check if they are consented
                            if (dataResponse.isAuthenticated()) {
                                startOverviewActivity();
                            } else {
                                startConsentActivity();
                            }
                        } else {
                            LOGGER.error("TokenLoginSubscribe: Authenticated Login Failure");
                            // Unable to log in right now. Let's boot to consent process.
                        }
                    }, throwable -> LOGGER.error("Sign up failed", throwable));
        } else {
            // Normal app opening, let's go to the right spot
            // TODO: Check if already signed in
            // for now, just launch the signup task
            startSignupTask();
        }
    }
}
