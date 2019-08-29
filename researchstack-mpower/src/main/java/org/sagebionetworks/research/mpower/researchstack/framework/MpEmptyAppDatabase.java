package org.sagebionetworks.research.mpower.researchstack.framework;

import org.sagebionetworks.researchstack.backbone.result.StepResult;
import org.sagebionetworks.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.researchstack.backbone.storage.database.AppDatabase;

import java.util.List;

/**
 * The BpEmptyAppDatabase is a no-op implementation of the AppDatabase, since MPower does not use a sql database
 */
public class MpEmptyAppDatabase implements AppDatabase {

    public MpEmptyAppDatabase() {
        super();
    }

    @Override
    public void saveTaskResult(TaskResult result) {
        // no-op
    }

    @Override
    public TaskResult loadLatestTaskResult(String taskIdentifier) {
        // no-op
        return null;
    }

    @Override
    public List<TaskResult> loadTaskResults(String taskIdentifier) {
        // no-op
        return null;
    }

    @Override
    public List<StepResult> loadStepResults(String stepIdentifier) {
        // no-op
        return null;
    }

    @Override
    public void setEncryptionKey(String key) {
        // no-op
    }

}
