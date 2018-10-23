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

package org.sagebionetworks.research.sageresearch.reminders

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import org.sagebionetworks.research.mpower.MainActivity
import org.sagebionetworks.research.mpower.R

class MpReminderAlarmReceiver: ReminderAlarmReceiver() {

    /**
     * @property notificationChannelId set on Android OS >= 26, uniquely identified your notifications
     */
    override val notificationChannelId: String
        get() {
            return "mPower Channel"
        }

    /**
     * @property notificationChannelId set on Android OS >= 26, explains your notifications
     */
    override val notificationChannelTitle: String
        get() {
            return "mPower Reminders"
        }

    /**
     * @property notificationChannelId set on Android OS >= 26, explains your notifications in more detail
     */
    override val notificationChannelDesc: String
        get() {
            return "mPower reminders help you remember to log your data."
        }

    // TODO: mdephillips 10/23/18 get Woody to design a status bar notification icon
    override fun notificationIcon(code: Int, action: String): Int? {
        return R.drawable.ic_reminder
    }

    /**
     * Overriding this allows for us to provide our own custom activity to launch when the notification is tapped
     */
    override fun pendingIntent(context: Context, code: Int, action: String): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.action = action
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        return stackBuilder.getPendingIntent(
                code, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}