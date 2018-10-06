package org.sagebionetworks.research.mpower.tracking;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.sagebionetworks.bridge.android.manager.ActivityManager;
import org.sagebionetworks.research.mpower.TaskLauncher;

import javax.inject.Inject;

/**
 * A ViewModelFactory allows us to instantiate ViewModels using creation parameters and/or constructor arguments.
 * TrackingViewModel's create method does not take parameters, but it does take a constructor argument.
 */
public class TrackingViewModelFactory {
    private final ActivityManager activityManager;

    private final TaskLauncher taskLauncher;

    /**
     * This constructor is annotated with @Inject, which is picked up by our dependency injection framework.
     *
     * @param activityManager
     *         injected activity manager
     */
    @Inject
    public TrackingViewModelFactory(ActivityManager activityManager, TaskLauncher taskLauncher) {
        this.activityManager = activityManager;
        this.taskLauncher = taskLauncher;
    }

    /**
     * TrackingViewModel uses construction injection to get an instance of the ActivityManager.
     *
     * @return a ViewModel instance
     */
    public ViewModelProvider.Factory create() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings(value = "unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(TrackingViewModel.class)) {
                    // noinspection unchecked
                    return (T) new TrackingViewModel(activityManager, taskLauncher);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }
}
