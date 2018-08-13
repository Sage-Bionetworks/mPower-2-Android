package org.sagebionetworks.research.mpower.researchstack;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    public void launchTask(@NonNull Context context, @NonNull String taskIdentifier,
            @Nullable UUID taskRunUUID) {
        Task task = mpTaskFactory.createTask(context, taskIdentifier);

        context.startActivity(ActiveTaskActivity.newIntent(context, task));
    }
}
