package org.sagebionetworks.research.mpower.logging;

import static com.google.common.base.Preconditions.checkNotNull;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.sagebionetworks.bridge.android.manager.ActivityManager;
import org.sagebionetworks.bridge.rest.model.ScheduledActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.subscriptions.CompositeSubscription;

public class LoggingViewModel extends ViewModel {

    public static class ScheduledActivityView {
        public final String scheduledActivityGuid;

        public ScheduledActivityView(final String scheduledActivityGuid) {
            this.scheduledActivityGuid = scheduledActivityGuid;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingViewModel.class);

    private final ActivityManager activityManager;

    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    private final MutableLiveData<List<ScheduledActivityView>> scheduledActivitiesLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> scheduledActivitiesLoadingErrorMessageLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> scheduledActivitiesLoadingLiveData = new MutableLiveData<>();

    public LoggingViewModel(@NonNull ActivityManager activityManager) {
        this.activityManager = checkNotNull(activityManager, "activity manager cannot be null");

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

    @MainThread
    public void reload() {
        updateScheduledActivities();
        // onReloadClicked any other data sources
    }

    @Override
    protected void onCleared() {
        compositeSubscription.unsubscribe();
    }

    @MainThread
    void updateScheduledActivities() {
        scheduledActivitiesLoadingErrorMessageLiveData.setValue(null);
        scheduledActivitiesLoadingLiveData.setValue(true);

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
