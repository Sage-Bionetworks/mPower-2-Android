package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;

import org.sagebionetworks.researchstack.backbone.TaskProvider;
import org.sagebionetworks.researchstack.backbone.task.Task;

import java.util.HashMap;

public class MpTaskProvider extends TaskProvider {

    private HashMap<String, Task> map = new HashMap<>();

    public MpTaskProvider(Context context) {
    }

    @Override
    public Task get(String taskId) {
        return map.get(taskId);
    }

    @Override
    public void put(String id, Task task) {
        map.put(id, task);
    }
}
