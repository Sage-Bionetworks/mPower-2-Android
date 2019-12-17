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

package org.sagebionetworks.research.mpower.inject

import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.Button
import org.sagebionetworks.research.mobile_ui.widget.ActionButton
import org.sagebionetworks.research.modules.common.step.overview.ShowOverviewStepFragment
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.reminders.MpReminderManager
import org.sagebionetworks.research.mpower.reminders.REMINDER_ACTION_RUN_TASK
import org.sagebionetworks.research.mpower.reminders.REMINDER_CODE_RUN_TASK
import org.sagebionetworks.research.presentation.model.action.ActionType
import org.sagebionetworks.research.presentation.model.interfaces.StepView
import org.sagebionetworks.research.sageresearch.reminders.Reminder
import org.sagebionetworks.research.sageresearch.reminders.ReminderScheduleRules
import org.threeten.bp.LocalDateTime

class MpShowOverviewStepFragment: ShowOverviewStepFragment() {

    companion object {
        /**
         * @return a new instance of the MpShowOverviewStepFragment
         */
        fun newInstance(stepView: StepView): MpShowOverviewStepFragment {
            val fragment = MpShowOverviewStepFragment()
            val arguments = ShowOverviewStepFragment.createArguments(stepView)
            fragment.arguments = arguments
            return fragment
        }
    }

    /**
     * This function is called by the subclass when any action button is clicked
     */
    override fun handleActionButtonClick(actionButton: ActionButton) {
        getActionTypeFromActionButton(actionButton)?.let {
            when(it) {
                ActionType.SKIP -> onReminderMeLaterClicked()
                else -> super.handleActionButtonClick(actionButton)
            }
        } ?: run {
            super.handleActionButtonClick(actionButton)
        }
    }

    /**
     * This function is called when the user taps the
     * "Remind me later" skip button action at the bottom of the screen
     */
    protected fun onReminderMeLaterClicked() {
        val act = activity ?: return
        val dialog = BottomSheetDialog(act)
        val sheetView = act.layoutInflater.inflate(R.layout.dialog_reminder_me_later, null)

        sheetView.findViewById<Button>(R.id.remind_me_in_2_hours_button)?.setOnClickListener {
            setReminder(LocalDateTime.now().plusHours(2))
            dialog.dismiss()
            performTaskFragment.cancelTask(false)
        }
        sheetView.findViewById<Button>(R.id.remind_me_in_1_hour_button)?.setOnClickListener {
            setReminder(LocalDateTime.now().plusHours(1))
            dialog.dismiss()
            performTaskFragment.cancelTask(false)
        }
        sheetView.findViewById<Button>(R.id.remind_me_in_15_minutes_button)?.setOnClickListener {
            setReminder(LocalDateTime.now().plusMinutes(15))
            dialog.dismiss()
            performTaskFragment.cancelTask(false)
        }
        sheetView.findViewById<Button>(R.id.do_not_remind_me_button)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    /**
     * @param reminderTime the time to schedule the reminder at
     */
    protected fun setReminder(reminderTime: LocalDateTime) {
        val taskId = performTaskViewModel.taskView.identifier
        context?.let {
            val reminderManager = MpReminderManager(it)
            val reminderScheduleRules = ReminderScheduleRules(reminderTime)
            val reminder = Reminder(
                    taskId, REMINDER_ACTION_RUN_TASK,
                    REMINDER_CODE_RUN_TASK, reminderScheduleRules,
                    title = it.getString(R.string.reminder_title_run_task).format(taskId))
            reminderManager.scheduleReminder(it, reminder)
        }
    }
}