package org.sagebionetworks.research.mpower;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import static org.sagebionetworks.research.mpower.Tasks.MEDICATION;
import static org.sagebionetworks.research.mpower.Tasks.SYMPTOMS;
import static org.sagebionetworks.research.mpower.Tasks.TAPPING;
import static org.sagebionetworks.research.mpower.Tasks.TREMOR;
import static org.sagebionetworks.research.mpower.Tasks.TRIGGERS;
import static org.sagebionetworks.research.mpower.Tasks.WALK_AND_BALANCE;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.sagebionetworks.research.mpower.TaskLauncher.TaskLaunchState.Type;
import org.sagebionetworks.research.mpower.researchstack.ResearchStackTaskLauncher;
import org.sagebionetworks.research.mpower.sageresearch.SageResearchTaskLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

import javax.inject.Inject;

/**
 * This launches tasks. We use this task to launch both RS and SR tasks. We may need to add more methods or change
 * this later.
 */
public class TaskLauncher {
    public static class TaskLaunchState {
        @Retention(RetentionPolicy.SOURCE)
        @StringDef({Type.RUNNING, Type.LAUNCH_ERROR, Type.CANCELED, Type.COMPLETED})
        public @interface Type {
            // TODO: determine other states @liujoshua 2018/08/06
            String RUNNING = "running";
            String LAUNCH_ERROR = "launch_error";
            String COMPLETED = "completed";
            String CANCELED = "canceled";
        }

        private final String state;

        public TaskLaunchState(final String state) {
            this.state = state;
        }

        @Type
        public String getState() {
            return state;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLauncher.class);

    private static final ImmutableSet<String> RS_TASKS = ImmutableSet.of();

    private static final ImmutableSet<String> SR_TASKS = ImmutableSet
            .of(TAPPING, WALK_AND_BALANCE, TRIGGERS, TREMOR, SYMPTOMS, MEDICATION);

    private final ResearchStackTaskLauncher researchStackTaskLauncher;

    private final SageResearchTaskLauncher sageResearchTaskLauncher;

    @Inject
    public TaskLauncher(@NonNull SageResearchTaskLauncher sageResearchTaskLauncher,
            @NonNull ResearchStackTaskLauncher researchStackTaskLauncher) {
        this.sageResearchTaskLauncher = checkNotNull(sageResearchTaskLauncher);
        this.researchStackTaskLauncher = checkNotNull(researchStackTaskLauncher);
    }

    /**
     * @param taskIdentifier
     *         identifer of task to launch
     * @param taskRunUUID
     *         optional uuid of previous task run to continue from
     * @return state of the task launch
     */
    @NonNull
    public LiveData<TaskLaunchState> launchTask(@NonNull Context context, @NonNull String taskIdentifier,
            @Nullable UUID taskRunUUID) {
        checkNotNull(context);
        checkArgument(!Strings.isNullOrEmpty(taskIdentifier), "taskIdentifier cannot be null or empty");
        checkState(taskRunUUID == null, "taskRunUUID not supported yet");

        // TODO: figure out what type of return values are appropriate @liujoshua 2018/08/06
        MutableLiveData<TaskLaunchState> tls;

        if (SR_TASKS.contains(taskIdentifier)) {
            LOGGER.debug("Launching SageResearch task: {}", taskIdentifier);

            sageResearchTaskLauncher.launchTask(context, taskIdentifier, taskRunUUID);
            tls = new MutableLiveData<>();
        } else if (RS_TASKS.contains(taskIdentifier)) {
            LOGGER.debug("Launching ResearchStack task: {}", taskIdentifier);

            tls = launchResearchStackTask(context, taskIdentifier, taskRunUUID);
        } else {
            LOGGER.warn("Unknown type of task: {}", taskIdentifier);

            tls = new MutableLiveData<>();
            tls.postValue(new TaskLaunchState(Type.LAUNCH_ERROR));
        }
        return tls;
    }

    @VisibleForTesting
    MutableLiveData<TaskLaunchState> launchResearchStackTask(@NonNull Context context, @NonNull String taskIdentifier,
            @Nullable UUID taskRunUUID) {

        MutableLiveData<TaskLaunchState> tls = new MutableLiveData<>();
        try {
            researchStackTaskLauncher.launchTask(context, taskIdentifier, taskRunUUID);
        } catch (Throwable t) {
            LOGGER.warn("Exception launching ResearchStack task: ()", taskIdentifier, t);
            tls.postValue(new TaskLaunchState(Type.LAUNCH_ERROR));
        }

        return tls;
    }
}
