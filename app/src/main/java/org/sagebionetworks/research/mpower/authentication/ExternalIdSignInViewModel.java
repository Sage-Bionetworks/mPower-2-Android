package org.sagebionetworks.research.mpower.authentication;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.google.common.base.Strings;

import org.sagebionetworks.bridge.android.manager.AuthenticationManager;
import org.sagebionetworks.bridge.rest.model.SignUp;
import org.sagebionetworks.research.mpower.research.DataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

public class ExternalIdSignInViewModel extends ViewModel {
    public static class Factory {
        private final AuthenticationManager authenticationManager;

        @Inject
        public Factory(final AuthenticationManager authenticationManager) {
            this.authenticationManager = authenticationManager;
        }

        public ViewModelProvider.Factory create() {
            return new ViewModelProvider.Factory() {
                @NonNull
                @Override
                @SuppressWarnings(value = "unchecked")
                public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                    if (modelClass.isAssignableFrom(ExternalIdSignInViewModel.class)) {
                        return (T) new ExternalIdSignInViewModel(authenticationManager);
                    }
                    throw new IllegalArgumentException("Unknown ViewModel class");
                }
            };
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalIdSignInViewModel.class);

    private final AuthenticationManager authenticationManager;

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    private final MutableLiveData<String> errorMessageMutableLiveData;

    private String externalId = "";

    private String firstName = "";

    private final MutableLiveData<Boolean> isExternalIdValidLiveData;

    private final MutableLiveData<Boolean> isLoadingMutableLiveData;

    private final MutableLiveData<Boolean> isSignedInLiveData;

    private boolean skipConsent = false;

    @MainThread
    public ExternalIdSignInViewModel(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;

        errorMessageMutableLiveData = new MutableLiveData<>();

        isLoadingMutableLiveData = new MutableLiveData<>();
        isLoadingMutableLiveData.setValue(false);

        isSignedInLiveData = new MutableLiveData<>();
        isSignedInLiveData.setValue(false);

        isExternalIdValidLiveData = new MutableLiveData<>();
        isExternalIdValidLiveData.setValue(false);
    }

    public void doSignIn() {
        LOGGER.debug("doSignIn");

        SignUp signUp = new SignUp();
        signUp.firstName(firstName);
        signUp.externalId(externalId);
        signUp.password(externalId);
        signUp.addDataGroupsItem("test_user");
        if (skipConsent) {
            signUp.addDataGroupsItem("test_no_consent");
        }
        // Add the random engagement groups
        // TODO: mdephillips 10/15/18 DataSourceManager should be synced with bridge config
        // as of now there is just hard-coded set of random engagement groups
        for (String dataGroupStr : DataSourceManager.randomDefaultEngagementGroups()) {
            signUp.addDataGroupsItem(dataGroupStr);
        }

        compositeSubscription.add(
                authenticationManager.signUp(signUp)
                        .andThen(authenticationManager.signInWithExternalId(externalId, externalId))
                        .doOnSubscribe(() -> isLoadingMutableLiveData.postValue(true))
                        .doAfterTerminate(() -> isLoadingMutableLiveData.postValue(false))
                        .subscribe(s -> {
                            isSignedInLiveData.postValue(true);
                        }, t -> {
                            isSignedInLiveData.postValue(false);
                            errorMessageMutableLiveData.postValue(t.getMessage());
                        }));
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageMutableLiveData;
    }

    public LiveData<Boolean> getIsExternalIdValid() {
        return isExternalIdValidLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingMutableLiveData;
    }

    public LiveData<Boolean> getIsSignedInLiveData() {
        return isSignedInLiveData;
    }

    public void setExternalId(String externalId) {
        LOGGER.debug("setExternalId: {}", externalId);
        this.externalId = externalId;
        isExternalIdValidLiveData.postValue(!Strings.isNullOrEmpty(externalId));
    }

    public void setFirstName(String firstName) {
        LOGGER.debug("setFirstName: {}", firstName);
        this.firstName = firstName;
    }

    public void setSkipConsent(boolean skipConsent) {
        LOGGER.debug("setSkipConsent: {}", skipConsent);
        this.skipConsent = skipConsent;
    }

    @Override
    protected void onCleared() {
        compositeSubscription.unsubscribe();
    }

    void onErrorMessageConsumed() {
        errorMessageMutableLiveData.postValue(null);
    }
}