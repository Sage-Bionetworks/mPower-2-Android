package org.sagebionetworks.research.mpower.researchstack;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.ActiveTaskActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;

import java.util.UUID;

import javax.inject.Inject;

public class ResearchStackTaskLauncher {
    private final MpTaskFactory mpTaskFactory;

    @Inject
    public ResearchStackTaskLauncher(@NonNull MpTaskFactory mpTaskFactory) {
        this.mpTaskFactory = checkNotNull(mpTaskFactory);
    }

    /**
     * @param activity
     * @param taskIdentifier
     * @param taskRunUUID
     * @param requestCode If >= 0, this code will be returned in
     *                    onActivityResult() when the activity exits.
     */
    public void launchTask(@NonNull Activity activity, @NonNull String taskIdentifier,
            @Nullable UUID taskRunUUID, int requestCode) {
        Task task = mpTaskFactory.createTask(activity, taskIdentifier);

        activity.startActivityForResult(ActiveTaskActivity.newIntent(activity, task), requestCode);
    }
}
