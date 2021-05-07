package org.sagebionetworks.research.mpower;

import android.annotation.SuppressLint;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.VisibleForTesting;
import android.widget.TextView;

import org.sagebionetworks.bridge.android.access.Resource;
import org.sagebionetworks.bridge.android.access.Resource.Status;
import org.sagebionetworks.bridge.android.viewmodel.PhoneAuthViewModel;
import org.sagebionetworks.research.mpower.authentication.IntroductionActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import rx.subscriptions.CompositeSubscription;

public class MpPhoneAuthActivity extends DaggerAppCompatActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MpPhoneAuthActivity.class);

    @Inject
    TaskLauncher taskLauncher;

    @Inject
    PhoneAuthViewModel.Factory phoneAuthViewModelFactory;

    private PhoneAuthViewModel phoneAuthViewModel;

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.debug("onCreate called");

        setContentView(R.layout.mp_activity_phone_auth);

        phoneAuthViewModel = ViewModelProviders.of(this, phoneAuthViewModelFactory)
                .get(PhoneAuthViewModel.class);

        phoneAuthViewModel.getSignInStateLiveData().observe(this, this::onSignInStateReceived);
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

    @VisibleForTesting
    void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            // We came in via a link! Let's do something with it
            final String token = appLinkData.getLastPathSegment();

            phoneAuthViewModel.signInWithToken(token);
        } else {
            startSignupTask();
        }
    }

    @VisibleForTesting
    void onSignInStateReceived(Resource<Object> signInState) {
        if (signInState.status == Status.SUCCESS) {
            returnToEntryActivity();
        } else if (signInState.status == Status.ERROR &&
                signInState.message != null) {

            if (signInState.message.contains("Consent required.")) {
                Fragment fragment = WebConsentFragment.Companion.newInstance();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container, fragment, "WebConsentFragment");
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                ((TextView) findViewById(R.id.textView))
                        .setText(signInState.message);
            }
        }
    }

    @VisibleForTesting
    void returnToEntryActivity() {
        Intent intent = new Intent(this, EntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @VisibleForTesting
    void startSignupTask() {
        startActivity(new Intent(this, IntroductionActivity.class));
    }
}
