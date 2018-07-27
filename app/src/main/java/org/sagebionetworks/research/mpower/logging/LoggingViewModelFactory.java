package org.sagebionetworks.research.mpower.logging;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.sagebionetworks.bridge.android.manager.ActivityManager;

import javax.inject.Inject;

/**
 * A ViewModelFactory allows us to instantiate ViewModels using creation parameters and/or constructor arguments.
 * LoggingViewModel's create method does not take parameters, but it does take a constructor argument.
 */
public class LoggingViewModelFactory {
    private ActivityManager activityManager;

    /**
     * This constructor is annotated with @Inject, which is picked up by our dependency injection framework.
     *
     * @param activityManager
     *         injected activity manager
     */
    @Inject
    public LoggingViewModelFactory(ActivityManager activityManager) {
        this.activityManager = activityManager;
    }

    /**
     * LoggingViewModel uses construction injection to get an instance of the ActivityManager.
     *
     * @return a ViewModel instance
     */
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
