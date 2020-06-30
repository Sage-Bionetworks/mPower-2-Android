package org.sagebionetworks.research.mpower;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import static org.sagebionetworks.research.mpower.research.MpIdentifier.AUTHENTICATE;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.MEDICATION;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.SYMPTOMS;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.TAPPING;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.TREMOR;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.TRIGGERS;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.WALK_AND_BALANCE;

import android.app.Activity;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.NotNull;
import org.sagebionetworks.research.domain.repository.TaskRepository;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
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

    public static final int TASK_REQUEST_CODE = 1492;
    public static final int WALK_AND_BALANCE_REQUEST_CODE = 1776;

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

    private static final ImmutableSet<String> RS_TASKS = ImmutableSet.of(AUTHENTICATE);

    private static final ImmutableSet<String> SR_TASKS = ImmutableSet
            .of(TAPPING, WALK_AND_BALANCE, TRIGGERS, TREMOR, SYMPTOMS, MEDICATION);
    private static final ImmutableSet<String> SR_TASKS_REQUIRING_RESULT = ImmutableSet.of(WALK_AND_BALANCE);

    private final ResearchStackTaskLauncher researchStackTaskLauncher;
    private final SageResearchTaskLauncher sageResearchTaskLauncher;
    private final TaskRepository taskRepository;

    @Inject
    public TaskLauncher(@NonNull SageResearchTaskLauncher sageResearchTaskLauncher,
            @NonNull ResearchStackTaskLauncher researchStackTaskLauncher,
            @NonNull TaskRepository taskRepository) {
        this.sageResearchTaskLauncher = checkNotNull(sageResearchTaskLauncher);
        this.researchStackTaskLauncher = checkNotNull(researchStackTaskLauncher);
        this.taskRepository = checkNotNull(taskRepository);
    }

    @VisibleForTesting
    MutableLiveData<TaskLaunchState> launchResearchStackTask(@NonNull Activity activity, @NonNull String taskIdentifier,
            @Nullable UUID taskRunUUID) {

        MutableLiveData<TaskLaunchState> tls = new MutableLiveData<>();
        try {
            researchStackTaskLauncher.launchTask(activity, taskIdentifier, taskRunUUID, TASK_REQUEST_CODE);
        } catch (Throwable t) {
            LOGGER.warn("Exception launching ResearchStack task: ()", taskIdentifier, t);
            tls.postValue(new TaskLaunchState(Type.LAUNCH_ERROR));
        }

        return tls;
    }

    /**
     * @param context used to launch the task, if this function ends up launching a ResearchTask task,
     *                context must be an instance of Activity
     * @param taskIdentifier identifier of task to launch
     * @param taskRunUUID optional uuid of previous task run to continue from
     * @return state of the task launch, some tasks, like surveys, may require an additional network call
     */
    @NonNull
    public LiveData<TaskLaunchState> launchTask(@NonNull Context context, @NonNull String taskIdentifier,
            @Nullable UUID taskRunUUID) {
        return launchTask(context, taskIdentifier, taskRunUUID, null);
    }
    public LiveData<TaskLaunchState> launchTask(@NonNull Context context, @NonNull String taskIdentifier,
            @Nullable UUID taskRunUUID, @Nullable TaskResult taskResult) {
        return launchTask(context, null, taskIdentifier, taskRunUUID, null);

    }

    /**
     * @param context used to launch the task, if this function ends up launching a ResearchTask task,
     *                context must be an instance of Activity
     * @param taskIdentifier identifier of task to launch
     * @param taskRunUUID optional uuid of previous task run to continue from
     * @param taskResult if not null, it will be used as the initial value of the TaskResult for the task.
     * @return state of the task launch, some tasks, like surveys, may require an additional network call
     */
    @NonNull
    public LiveData<TaskLaunchState> launchTask(@NonNull Context context, @NonNull Fragment fragment, @NonNull String taskIdentifier,
            @Nullable UUID taskRunUUID, @Nullable TaskResult taskResult) {
        checkNotNull(context);
        checkArgument(!Strings.isNullOrEmpty(taskIdentifier), "taskIdentifier cannot be null or empty");

        if (taskResult != null) {
            taskRepository.setTaskResult(taskResult).blockingAwait();
        }

        // TODO: figure out what type of return values are appropriate @liujoshua 2018/08/06
        MutableLiveData<TaskLaunchState> tls;

        if (SR_TASKS.contains(taskIdentifier)) {
            LOGGER.debug("Launching SageResearch task: {}", taskIdentifier);
            if (SR_TASKS_REQUIRING_RESULT.contains(taskIdentifier) && context instanceof Activity) {
                sageResearchTaskLauncher.launchTask(context, fragment, taskIdentifier, taskRunUUID, WALK_AND_BALANCE_REQUEST_CODE, null);

            } else {
                sageResearchTaskLauncher.launchTask(context, taskIdentifier, taskRunUUID);
            }
            tls = new MutableLiveData<>();
        } else if (RS_TASKS.contains(taskIdentifier)) {
            LOGGER.debug("Launching ResearchStack task: {}", taskIdentifier);

            if (!(context instanceof Activity)) {
                throw new IllegalArgumentException(
                        "To launch research tasks, context param must be an Activity,"
                                + " so that the task result can be returned in onActivityResult");
            }

            tls = launchResearchStackTask((Activity)context, taskIdentifier, taskRunUUID);
        } else {
            LOGGER.warn("Unknown type of task: {}", taskIdentifier);

            tls = new MutableLiveData<>();
            tls.postValue(new TaskLaunchState(Type.LAUNCH_ERROR));
        }
        return tls;
    }
}
