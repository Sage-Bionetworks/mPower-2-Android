package org.sagebionetworks.research.mpower;

import static com.google.common.base.Preconditions.checkArgument;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.google.common.base.Strings;

import org.sagebionetworks.research.domain.repository.TaskRepository;
import org.sagebionetworks.research.mpower.TaskLauncher.TaskLaunchState.Type;

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
        String getState() {
            return state;
        }
    }

    @Inject
    public TaskLauncher(TaskRepository taskRepository) {

    }

    /**
     * @param taskIdentifier
     *         identifer of task to launch
     * @param taskRunUUID
     *         optional uuid of previous task run to continue from
     * @return state of the task launch
     */
    @NonNull
    public LiveData<TaskLaunchState> launchTask(@NonNull String taskIdentifier, @Nullable UUID taskRunUUID) {
        checkArgument(!Strings.isNullOrEmpty(taskIdentifier), "taskIdentifier cannot be null or empty");

        // TODO: load and launch task using RS or SR as appropriate @liujoshua 2018/08/06
        MutableLiveData<TaskLaunchState> tls = new MutableLiveData<>();
        tls.postValue(new TaskLaunchState(Type.LAUNCH_ERROR));
        return tls;
    }
}
