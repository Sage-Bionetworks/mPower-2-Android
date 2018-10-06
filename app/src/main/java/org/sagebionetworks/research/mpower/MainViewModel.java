package org.sagebionetworks.research.mpower;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import org.sagebionetworks.bridge.android.manager.AuthenticationManager;
import org.sagebionetworks.bridge.rest.model.UserSessionInfo;

public class MainViewModel extends ViewModel {
    private final AuthenticationManager authenticationManager;

    public MainViewModel(@NonNull AuthenticationManager authenticationManager) {
        this.authenticationManager = checkNotNull(authenticationManager);
    }

    // TODO: make isAuthenticated and isConsented LiveData objects @liujoshua 2018/08/09
    public boolean isAuthenticated() {
        UserSessionInfo userSessionInfo = authenticationManager.getUserSessionInfo();
        return userSessionInfo != null && userSessionInfo.isAuthenticated();
    }

    public boolean isConsented() {
        return authenticationManager.isConsented();
    }
}
