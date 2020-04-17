/*
 * BSD 3-Clause License
 *
 * Copyright 2020  Sage Bionetworks. All rights reserved.
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

package org.sagebionetworks.research.mpower

import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.sagebionetworks.research.mpower.profile.MPowerProfileSettingsFragment
import org.sagebionetworks.research.sageresearch.profile.ProfileSettingsFragment.Companion.ARG_IS_MAIN_VIEW
import org.sagebionetworks.research.sageresearch.profile.ProfileSettingsFragment.Companion.ARG_PROFILE_KEY
import org.sagebionetworks.research.sageresearch.profile.ProfileSettingsRecyclerViewAdapter
import java.util.regex.Matcher

@RunWith(AndroidJUnit4::class)
@LargeTest
class MPowerProfileSettingsUITest {

    val LIST_ITEM_IN_TEST = 2

    @Test
    fun test_isDataVisiable() {

        val bundle = Bundle().apply {
            putString(ARG_PROFILE_KEY, "SettingsDataSource")
            putBoolean(ARG_IS_MAIN_VIEW, false)
        }
        //TODO launch issue :(
        val scenario = launchFragmentInContainer<MPowerProfileSettingsFragment>(bundle)
       onView(withId(R.id.list)).check(matches(isDisplayed()))
       /* onView(withId(R.id.list))
                .perform(actionOnItemAtPosition<ProfileSettingsRecyclerViewAdapter.ViewHolder>(LIST_ITEM_IN_TEST, click()))
*/
    //    onView(withId(R.id.item_detail)).check(matches(withText("Enabled")))
        /*typeText(R.id.item_detail, "Enabled")*/

        /*   onView(withId(R.id.settings_icon)).check(matches(isClickable()))
           onView(withId(R.id.settings_icon)).check(matches(isEnabled()))
           onView(withId(R.id.settings_icon)).perform(click())*/
    }

   /* fun atPosition(position: Int, @NonNull itemMatcher: Matcher<View?>): Matcher<View?>? {
        checkNotNull(itemMatcher)
        return object : BoundedMatcher<View?, RecyclerView?>(RecyclerView::class.java) {
            fun describeTo(description: Description) {
                description.appendText("has item at position $position: ")
                itemMatcher.describeTo(description)
            }

            protected fun matchesSafely(view: RecyclerView): Boolean {
                val viewHolder: RecyclerView.ViewHolder = view.findViewHolderForAdapterPosition(position)
                        ?: // has no item on such position
                        return false
                return itemMatcher.matches(viewHolder.itemView)
            }
        }
    }*/
}