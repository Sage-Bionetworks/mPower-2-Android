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
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions.Companion.childOfViewAtPositionWithMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@Ignore
class FromEntryActivityToSettingsTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(EntryActivity::class.java)
    var skipSingInPart = false

    @Before
    fun setupTest() {
        try {
            //check that user is not signed in. If he is, skip sing in part
            val appCompatButton = Espresso.onView(
                    Matchers.allOf(ViewMatchers.withId(R.id.button_go_forward), ViewMatchers.withText("Next"),
                            ViewMatchers.isDisplayed()))

            appCompatButton.perform(ViewActions.click())
        } catch (ex: NoMatchingViewException) {
            skipSingInPart = true
        }
    }

    @Test
    fun entryActivityTest() {
        if (!skipSingInPart) {

            val appCompatButton = Espresso.onView(
                    Matchers.allOf(ViewMatchers.withId(R.id.button_go_forward), ViewMatchers.withText("Next"),
                            ViewMatchers.isDisplayed()))
            appCompatButton.perform(ViewActions.click())

            val appCompatTextView = Espresso.onView(
                    Matchers.allOf(ViewMatchers.withId(R.id.internal_sign_in_link),
                            ViewMatchers.withText("Click for External ID login [TEST BUILD]"),
                            childAtPosition(
                                    Matchers.allOf(ViewMatchers.withId(R.id.mp_root_instruction_layout),
                                            childAtPosition(
                                                    ViewMatchers.withId(R.id.mp_step_layout_container),
                                                    0)),
                                    5),
                            ViewMatchers.isDisplayed()))
            appCompatTextView.perform(ViewActions.click())

            Thread.sleep(1000)

            val appCompatEditText = Espresso.onView(
                    Matchers.allOf(ViewMatchers.withId(R.id.firstName),
                            ViewMatchers.isDisplayed()))
            appCompatEditText.perform(ViewActions.replaceText("ella"), ViewActions.closeSoftKeyboard())

            val appCompatEditText2 = Espresso.onView(
                    Matchers.allOf(ViewMatchers.withId(R.id.externalId),
                            ViewMatchers.isDisplayed()))
            appCompatEditText2.perform(ViewActions.replaceText("HF20200318C"), ViewActions.closeSoftKeyboard())

            val appCompatCheckBox = Espresso.onView(
                    Matchers.allOf(ViewMatchers.withId(R.id.skipConsent), ViewMatchers.withText("Skip Consent"),
                            ViewMatchers.isDisplayed()))
            appCompatCheckBox.perform(ViewActions.click())

            val appCompatButton2 = Espresso.onView(
                    Matchers.allOf(ViewMatchers.withId(R.id.signIn), ViewMatchers.withText("Sign In"),
                            ViewMatchers.isDisplayed()))
            appCompatButton2.perform(ViewActions.click())

            Thread.sleep(7000)
        }

        val bottomNavigationItemView = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.navigation_profile),
                        ViewMatchers.withContentDescription("Profile"),
                        ViewMatchers.isDisplayed()))
        bottomNavigationItemView.perform(ViewActions.click())

        val appCompatImageView = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.settings_icon),
                        ViewMatchers.isDisplayed()))
        appCompatImageView.perform(ViewActions.click())

        Thread.sleep(1000)

        checkOnEnable()
        checkOnDisable()
    }

    fun checkOnEnable() {
        val constraintLayout = onView(
                Matchers.allOf(ViewMatchers.withId(R.id.background),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.list),
                                        childAtPosition(
                                                ViewMatchers.withClassName(
                                                        Matchers.`is`("android.widget.RelativeLayout")),
                                                3)),
                                2),
                        ViewMatchers.isDisplayed()))
        constraintLayout.perform(ViewActions.click())

        val appCompatButton3 = onView(
                Matchers.allOf(ViewMatchers.withId(R.id.radio_okay), ViewMatchers.withText("Okay"),
                        ViewMatchers.isDisplayed()))

        appCompatButton3.perform(ViewActions.click())

        val appCompatButton4 = onView(
                Matchers.allOf(ViewMatchers.withId(R.id.done_button), ViewMatchers.withText("Save"),
                        ViewMatchers.isDisplayed()))
        appCompatButton4.perform(ViewActions.click())
        Thread.sleep(6000)

        onView(
                ViewMatchers.withId(R.id.list))
                .check(matches(
                        childOfViewAtPositionWithMatcher(R.id.item_detail, 2,
                                withText("Enabled"))))
    }

    fun checkOnDisable() {
        val constraintLayout = onView(
                Matchers.allOf(ViewMatchers.withId(R.id.background),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.list),
                                        childAtPosition(
                                                ViewMatchers.withClassName(
                                                        Matchers.`is`("android.widget.RelativeLayout")),
                                                3)),
                                2),
                        ViewMatchers.isDisplayed()))
        constraintLayout.perform(ViewActions.click())
        Thread.sleep(1000)

        val appCompatButton3 = onView(
                Matchers.allOf(ViewMatchers.withId(R.id.radio_not_now), ViewMatchers.withText("Not Now"),
                        ViewMatchers.isDisplayed()))

        appCompatButton3.perform(ViewActions.click())

        val appCompatButton4 = onView(
                Matchers.allOf(ViewMatchers.withId(R.id.done_button), ViewMatchers.withText("Save"),
                        ViewMatchers.isDisplayed()))
        appCompatButton4.perform(ViewActions.click())
        Thread.sleep(5000)

        onView(
                ViewMatchers.withId(R.id.list))
                .check(matches(
                        childOfViewAtPositionWithMatcher(R.id.item_detail, 2,
                                withText("Disabled"))))
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
