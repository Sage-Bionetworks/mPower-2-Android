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

package org.sagebionetworks.research.mpower.sageresearch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.sagebionetworks.research.domain.repository.TaskRepository;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.disposables.CompositeDisposable;

public class TappingTaskActivity extends AppCompatActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(TappingTaskActivity.class);

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    TaskRepository taskRepository;

    private static final List<String> taskIdentifiers = Arrays.asList("Tapping", "Tremor", "WalkAndBalance");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    /**
     * Registers a lifecycle listener that hides the task selection buttons when any PerformTaskFragment starts,
     * and makes the button reappear when there a no PerformTaskFragments running again.
     *
     * @param textViews The list of textView's to hide when the fragment's start and to reappear when the fragments end.
     */
    private void registerFragmentLifeCycleListener(final List<TextView> textViews) {
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    int fragmentStartedCount = 0;

                    @Override
                    public void onFragmentStarted(FragmentManager fm, Fragment f) {
                        super.onFragmentStarted(fm, f);
                        if (f instanceof PerformTaskFragment) {
                            fragmentStartedCount++;
                            for (TextView textView : textViews) {
                                textView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFragmentStopped(FragmentManager fm, Fragment f) {
                        super.onFragmentStopped(fm, f);
                        if (f instanceof PerformTaskFragment) {
                            fragmentStartedCount--;
                            if (fragmentStartedCount == 0) {
                                for (TextView textView : textViews) {
                                    textView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                }, false);
    }
}