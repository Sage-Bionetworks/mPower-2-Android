package org.sagebionetworks.research.mpower.data;

import static org.sagebionetworks.research.mpower.Tasks.MEDICATION;
import static org.sagebionetworks.research.mpower.Tasks.SYMPTOMS;
import static org.sagebionetworks.research.mpower.Tasks.TRIGGERS;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import org.sagebionetworks.research.data.ResourceTaskRepository;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.domain.task.navigation.TaskBase;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep;

import java.util.Collections;

import io.reactivex.Single;

public class AppResourceTaskRepository extends ResourceTaskRepository {
    private static final ImmutableSet<String> TRACKING_IDENTIFIERS = ImmutableSet.of(TRIGGERS, MEDICATION, SYMPTOMS);

    public AppResourceTaskRepository(final Context context, final Gson gson) {
        super(context, gson);
    }

    @NonNull
    @Override
    public Single<Task> getTask(final String taskIdentifier) {
        if (!TRACKING_IDENTIFIERS.contains(taskIdentifier)) {
            return super.getTask(taskIdentifier);
        }

        return Single.fromCallable(() -> {
            TrackingStep trackingStep = gson.fromJson(this.getJsonTaskAsset(taskIdentifier), TrackingStep.class);
            Task task = TaskBase.builder()
                    .setIdentifier(trackingStep.getIdentifier())
                    .setSteps(Collections.singletonList(trackingStep)).build();
            return task;
        });
    }
}
