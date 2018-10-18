package org.sagebionetworks.research.mpower.tracking;

import static com.google.common.base.Preconditions.checkNotNull;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.joda.time.DateTime;
import org.sagebionetworks.bridge.android.manager.ActivityManager;
import org.sagebionetworks.bridge.rest.model.ScheduledActivity;
import org.sagebionetworks.research.mpower.TaskLauncher;
import org.sagebionetworks.research.mpower.TaskLauncher.TaskLaunchState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.subscriptions.CompositeSubscription;

/**
 * The ViewModel forms the presentation layer (ViewModel or Presenter). It is responsible for receiving actions from
 * the views, communicating with the data layer, applying UI logic, keeping state, and exposing relevant data to the
 * UI for rendering. It encapsulates logic and state while not depending on the Android framework, thus making it
 * suitable for JVM unit testing.
 * <p>
 * https://medium.com/upday-devs/android-architecture-patterns-part-3-model-view-viewmodel-e7eeee76b73b
 * <p>
 * https://medium.com/@rohitsingh14101992/lets-keep-activity-dumb-using-livedata-53468ed0dc1f
 */
public class TrackingViewModel extends ViewModel {
    /**
     * This is the ScheduledActivity model for the view layer. It contains data related to rendering a scheduled
     * activity.
     */
    public static class ScheduledActivityView {
        public final String scheduledActivityGuid;

        public ScheduledActivityView(final String scheduledActivityGuid) {
            this.scheduledActivityGuid = scheduledActivityGuid;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingViewModel.class);

    private final ActivityManager activityManager;

    // holds onto RxJava subscriptions for cleanup
    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    private final TaskLauncher taskLauncher;

    /**
     * @param activityManager
     *         injected activity manager dependency
     */
    public TrackingViewModel(@NonNull ActivityManager activityManager, @NonNull TaskLauncher taskLauncher) {
        this.activityManager = checkNotNull(activityManager, "activity manager cannot be null");
        this.taskLauncher = checkNotNull(taskLauncher, "task launcher cannot be null");
    }

    /**
     * Receives action from the View.
     *
     * @param context
     *         context to launch task from
     * @param taskId
     *         identifier of task to launch
     * @param taskRunUUID
     *         previous task run to continue from, if applicable
     * @return state of task launch
     */
    @MainThread
    public LiveData<TaskLaunchState> launchTask(@NonNull Context context, @NonNull String taskId,
            @Nullable UUID taskRunUUID) {
        return taskLauncher.launchTask(context, taskId, taskRunUUID);
    }

    @Override
    protected void onCleared() {
        compositeSubscription.unsubscribe();
    }
}
