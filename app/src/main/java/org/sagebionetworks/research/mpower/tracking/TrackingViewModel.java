package org.sagebionetworks.research.mpower.tracking;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

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

    private final MutableLiveData<List<ScheduledActivityView>> scheduledActivitiesLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> scheduledActivitiesLoadingErrorMessageLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> scheduledActivitiesLoadingLiveData = new MutableLiveData<>();

    private final TaskLauncher taskLauncher;

    /**
     * @param activityManager
     *         injected activity manager dependency
     */
    public TrackingViewModel(@NonNull ActivityManager activityManager, @NonNull TaskLauncher taskLauncher) {
        this.activityManager = checkNotNull(activityManager, "activity manager cannot be null");
        this.taskLauncher = checkNotNull(taskLauncher, "task launcher cannot be null");

        updateScheduledActivities();
    }

    public LiveData<List<ScheduledActivityView>> getScheduledActivitiesLiveData() {
        return scheduledActivitiesLiveData;
    }

    public LiveData<String> getScheduledActivitiesLoadingErrorMessageLiveData() {
        return scheduledActivitiesLoadingErrorMessageLiveData;
    }

    public LiveData<Boolean> getScheduledActivitiesLoadingLiveData() {
        return scheduledActivitiesLoadingLiveData;
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

    /**
     * Receives action from the View (TrackingFragment)
     */
    @MainThread
    public void reload() {
        updateScheduledActivities();
        // onReloadClicked any other data sources
    }

    @Override
    protected void onCleared() {
        compositeSubscription.unsubscribe();
    }

    @VisibleForTesting
    @AnyThread
    void updateScheduledActivities() {
        scheduledActivitiesLoadingErrorMessageLiveData.postValue(null);
        scheduledActivitiesLoadingLiveData.postValue(true);

        compositeSubscription.add(activityManager.getActivities(DateTime.now().minusDays(14), DateTime.now())
                .delay(1, TimeUnit.SECONDS)
                .doAfterTerminate(() -> scheduledActivitiesLoadingLiveData.postValue(false))
                .subscribe(scheduledActivityListV4 -> {
                    List<ScheduledActivityView> scheduledActivityViews = new ArrayList<>();
                    for (ScheduledActivity sa : scheduledActivityListV4.getItems()) {
                        scheduledActivityViews.add(new ScheduledActivityView(sa.getGuid()));
                    }
                    scheduledActivitiesLiveData.postValue(scheduledActivityViews);
                }, throwable -> {
                    LOGGER.warn("error retrieving scheduled activities", throwable);
                    scheduledActivitiesLoadingErrorMessageLiveData.postValue(throwable.getMessage());
                }));
    }
}
