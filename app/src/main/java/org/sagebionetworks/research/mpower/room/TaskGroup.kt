package org.sagebionetworks.research.mpower.room

import org.sagebionetworks.research.domain.step.ui.theme.ImageTheme
import org.sagebionetworks.research.domain.task.TaskInfo

//
//  Copyright © 2016-2018 Sage Bionetworks. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// 1.  Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// 2.  Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// 3.  Neither the name of the copyright holder(s) nor the names of any contributors
// may be used to endorse or promote products derived from this software without
// specific prior written permission. No license is granted to the trademarks of
// the copyright holders even if such marks are included in this software.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/**
 * `TaskGroup` defines a sub-grouping of tasks.  This is used in UI presentations where
 * the researchers wish to tie a group of activities and surveys together but allow the
 * user to perform them non-sequentially or with a break between the activities.
 */
interface TaskGroup {

    /**
     * @property identifier A short string that uniquely identifies the task group.
     */
    val identifier: String

    /**
     * @property The primary text to display for the task group in a localized string.
     */
    val title: String?

    /**
     * @property Additional detail text to display for the task group in a localized string.
     */
    val detail: String?

    /**
     * TODO: mdephillips 8/23/18 is this the correct equivalent of iOS' RSDImageVendor?
     * @property An icon image that can be used for displaying the choice.
     */
    val imageVendor: ImageTheme?

    /**
     * TODO: mdephillips 8/23/18 the TaskInfo interface is missing a few fields from it's iOS equivalent 'RSDTaskGroup'
     * @property An array of the task references included in this group.
     */
    val tasks: Array<TaskInfo>

    /**
     * TODO: mdephillips 8/23/18 we're not sure if we are using TaskPaths yet on Android or not, leaving out for now
     */
    //fun instantiateTaskPath(taskInfo: TaskInfo): TaskPath?
}