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

package org.sagebionetworks.research.mpower.reminders

import android.app.TimePickerDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_reminder.reminder_checkbox
import kotlinx.android.synthetic.main.activity_reminder.reminder_done_button
import kotlinx.android.synthetic.main.activity_reminder.reminder_time_button
import org.sagebionetworks.researchstack.backbone.ui.ViewTaskActivity.EXTRA_TASK_RESULT
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstReminderViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * The StudyBurstReminderActivity shows UI to the user to toggle on/off and set a reminder time for study burst reminders
 */
class StudyBurstReminderActivity: AppCompatActivity() {

    private val logger = LoggerFactory.getLogger(StudyBurstReminderActivity::class.java)

    private val timePickerDialog: TimePickerDialog by lazy {
        val dialog = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    viewModel.localHour = hour
                    viewModel.localMinute = minute
                    reminder_time_button.text = viewModel.toString(hour, minute)
                }, viewModel.localHour, viewModel.localMinute, false)
        dialog
    }

    /**
     * @property viewModel encapsulates all read/write data operations
     */
    private val viewModel: StudyBurstReminderViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(StudyBurstReminderViewModel::class.java)
    }

    /**
     * @property viewModelFactory can create a StudyBurstReminderViewModel instance injected through Dagger
     */
    @Inject
    lateinit var viewModelFactory: StudyBurstReminderViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        reminder_done_button.setOnClickListener { onDoneButtonClicked() }
        reminder_time_button.setOnClickListener { timePickerDialog.show() }
        reminder_checkbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.localDoNotRemindMe = isChecked
        }

        viewModel.reminderLiveData().observe(this, Observer {
            // no-op but keep the observation to obtain the correct schedules and reports
            logger.info("Reminder State = $it")
        })

        reminder_checkbox.isChecked = viewModel.localDoNotRemindMe
        timePickerDialog.updateTime(viewModel.localHour, viewModel.localMinute)
        reminder_time_button.text = viewModel.toString(viewModel.localHour, viewModel.localMinute)
    }

    /**
     * This function is called when the bottom done button is clicked
     */
    protected fun onDoneButtonClicked() {
        val taskResult = viewModel.saveReminder(this,
                viewModel.localDoNotRemindMe, viewModel.localHour, viewModel.localMinute)
        taskResult?.let {
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_TASK_RESULT, it)
            setResult(RESULT_OK, resultIntent)
        }
        finish()
    }
}