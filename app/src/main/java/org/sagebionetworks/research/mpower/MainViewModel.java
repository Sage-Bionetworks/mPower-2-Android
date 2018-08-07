package org.sagebionetworks.research.mpower;

import static com.google.common.base.Preconditions.checkNotNull;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import org.sagebionetworks.bridge.android.manager.AuthenticationManager;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;

public class MainViewModel extends ViewModel {

    private final AuthenticationManager authenticationManager;


    public MainViewModel(@NonNull AuthenticationManager authenticationManager) {
        this.authenticationManager = checkNotNull(authenticationManager);
    }

    public boolean isAuthenticated() {
        UserSessionInfo userSessionInfo = authenticationManager.getUserSessionInfo();
        return userSessionInfo != null && userSessionInfo.isAuthenticated();
    }

    public boolean isConsented() {
        return authenticationManager.isConsented();
    }
}
