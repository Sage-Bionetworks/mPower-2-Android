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

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FromEntryActivityToSettingsTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(EntryActivity::class.java)
    var skipSingInPart = false

    @Before
    fun setupTest() {
        try {
            //check that user is not signed in. If he is, skip sing in part
            val appCompatButton =  onView(
                    allOf(withId(R.id.button_go_forward), withText("Next"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.bp_next_button_container),
                                            0),
                                    2),
                            isDisplayed()))

            appCompatButton.perform(click())
        } catch (ex: NoMatchingViewException) {
            skipSingInPart = true
        }
    }

    @Test
    fun entryActivityTest() {
        if (!skipSingInPart) {

            val appCompatButton = onView(
                    allOf(withId(R.id.button_go_forward), withText("Next"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.bp_next_button_container),
                                            0),
                                    2),
                            isDisplayed()))
            appCompatButton.perform(click())

            val appCompatTextView = onView(
                    allOf(withId(R.id.internal_sign_in_link), withText("Click for External ID login [TEST BUILD]"),
                            childAtPosition(
                                    allOf(withId(R.id.mp_root_instruction_layout),
                                            childAtPosition(
                                                    withId(R.id.mp_step_layout_container),
                                                    0)),
                                    5),
                            isDisplayed()))
            appCompatTextView.perform(click())

            Thread.sleep(1000)

            val appCompatEditText = onView(
                    allOf(withId(R.id.firstName),
                            childAtPosition(
                                    childAtPosition(
                                            withId(android.R.id.content),
                                            0),
                                    2),
                            isDisplayed()))
            appCompatEditText.perform(replaceText("el"), closeSoftKeyboard())

            val appCompatEditText2 = onView(
                    allOf(withId(R.id.externalId),
                            childAtPosition(
                                    childAtPosition(
                                            withId(android.R.id.content),
                                            0),
                                    4),
                            isDisplayed()))
            appCompatEditText2.perform(replaceText("HF20200318C"), closeSoftKeyboard())

            val appCompatCheckBox = onView(
                    allOf(withId(R.id.skipConsent), withText("Skip Consent"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(android.R.id.content),
                                            0),
                                    5),
                            isDisplayed()))
            appCompatCheckBox.perform(click())

            val appCompatButton2 = onView(
                    allOf(withId(R.id.signIn), withText("Sign In"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(android.R.id.content),
                                            0),
                                    6),
                            isDisplayed()))
            appCompatButton2.perform(click())

            Thread.sleep(1000)
        }


        val bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_profile), withContentDescription("Profile"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation),
                                        0),
                                2),
                        isDisplayed()))
        bottomNavigationItemView.perform(click())

        val appCompatImageView = onView(
                allOf(withId(R.id.settings_icon),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                2),
                        isDisplayed()))
        appCompatImageView.perform(click())

        Thread.sleep(500)

        val constraintLayout = onView(
                allOf(withId(R.id.background),
                        childAtPosition(
                                allOf(withId(R.id.list),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.RelativeLayout")),
                                                3)),
                                2),
                        isDisplayed()))
        constraintLayout.perform(click())

        Thread.sleep(1000)

        val appCompatButton3 = onView(
                allOf(withId(R.id.done_button), withText("Save"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()))
        appCompatButton3.perform(click())
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
