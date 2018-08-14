package org.sagebionetworks.research.mpower.sageresearch;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment.OnPerformTaskExitListener;
import org.sagebionetworks.research.presentation.model.TaskView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class PerformTaskActivity extends AppCompatActivity implements HasSupportFragmentInjector,
        OnPerformTaskExitListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformTaskActivity.class);

    private static final String ARGUMENT_TASK_VIEW = "TASK_VIEW";

    private static final String ARGUMENT_TASK_RUN_UUID = "TASK_RUN_UUID";

    @Inject
    DispatchingAndroidInjector<Fragment> supportFragmentInjector;


    public static Intent createIntent(@NonNull Context context, @NonNull TaskView taskView,
            @Nullable UUID taskRunUUID) {
        checkNotNull(context);
        checkNotNull(taskView);

        Intent launchIntent = new Intent(context, PerformTaskActivity.class)
                .putExtra(ARGUMENT_TASK_VIEW, taskView);
        if (taskRunUUID != null) {
            launchIntent.putExtra(ARGUMENT_TASK_RUN_UUID, new ParcelUuid(taskRunUUID));
        }
        return launchIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_task);

        Intent launchIntent = getIntent();

        ParcelUuid parcelUuid = launchIntent.getParcelableExtra(ARGUMENT_TASK_RUN_UUID);
        UUID taskRunUUID = parcelUuid == null ? null : parcelUuid.getUuid();

        PerformTaskFragment performTaskFragment = PerformTaskFragment.newInstance(
                launchIntent.getParcelableExtra(ARGUMENT_TASK_VIEW), taskRunUUID);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, performTaskFragment)
                .commit();
    }

    @Override
    public void onTaskExit(@NonNull final Status status, @NonNull final TaskResult taskResult) {
        LOGGER.info("Task exited with status: {}, taskResult: {}", status, taskResult);

        // TODO: handle task result processing
        finish();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }
}
