/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.mpower;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.CompositeDisposable;
import org.sagebionetworks.research.domain.repository.TaskRepository;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.presentation.model.TaskView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    TaskRepository taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rs2_perform_task_activity_layout);

        PerformTaskFragment performTaskFragment = (PerformTaskFragment) getSupportFragmentManager()
                .findFragmentById(R.id.rs2_task_content_frame);

        // TODO: show list of available tasks
        if (performTaskFragment == null) {
            compositeDisposable.add(
                    taskRepository.getTaskInfo("Tremor")
                            .map(taskInfo -> TaskView.builder()
                                    .setIdentifier(taskInfo.getIdentifier())
                                    .build())
                            .subscribe(taskView -> {
                                PerformTaskFragment newPerformTaskFragment = PerformTaskFragment
                                        .newInstance(taskView, UUID.randomUUID());

                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.rs2_task_content_frame, newPerformTaskFragment)
                                        .commit();
                            }, throwable ->
                                    LOGGER.error("Failed to retrieve task", throwable)));
        } else {
            LOGGER.debug("Found existing PerformTaskFragment");
        }
    }
}