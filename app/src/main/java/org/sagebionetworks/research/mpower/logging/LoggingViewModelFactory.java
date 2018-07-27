package org.sagebionetworks.research.mpower.logging;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.sagebionetworks.bridge.android.manager.ActivityManager;

import javax.inject.Inject;

public class LoggingViewModelFactory {
    private ActivityManager activityManager;

    @Inject
    public LoggingViewModelFactory(ActivityManager activityManager) {
        this.activityManager = activityManager;
    }

    public ViewModelProvider.Factory create() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings(value = "unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(LoggingViewModel.class)) {
                    // noinspection unchecked
                    return (T) new LoggingViewModel(activityManager);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }
}
