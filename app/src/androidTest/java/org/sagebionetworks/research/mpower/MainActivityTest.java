package org.sagebionetworks.research.mpower;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {

        // check initial layout, showing tracking tab
        onView(allOf(
                withId(R.id.container),
                withChild(allOf(withId(R.id.fragment_container), isDisplayed())),
                withChild(allOf(withId(R.id.navigation), isDisplayed())))
        ).check(matches(isDisplayed()));

        onView(withId(R.id.fragment_container))
                .check(matches(hasDescendant(allOf(
                        withId(R.id.fragment_tracking),
                        isDisplayed()))));

        onView(withId(R.id.navigation))
                .check(matches(isDisplayed()))
                .check(matches(hasDescendant(allOf(withId(R.id.navigation_tracking), isDisplayed()))))
//                .check(matches(hasDescendant(allOf(withId(R.id.navigation_history), isDisplayed()))))
//                .check(matches(hasDescendant(allOf(withId(R.id.navigation_insights), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.navigation_profile), isDisplayed()))));

//        // show history tab
//        onView(withId(R.id.navigation_history))
//                .perform(click());
//
//        onView(withId(R.id.fragment_history))
//                .check(matches(isDisplayed()));
//
//        // show insights tab
//        onView(withId(R.id.navigation_insights))
//                .perform(click());
//
//        onView(withId(R.id.fragment_insights))
//                .check(matches(isDisplayed()));

        // show profile tab
        onView(withId(R.id.navigation_profile))
                .perform(click());

        onView(withId(R.id.fragment_profile))
                .check(matches(isDisplayed()));

        // show tracking tab
        onView(withId(R.id.navigation_tracking))
                .perform(click());

        onView(withId(R.id.fragment_tracking))
                .check(matches(isDisplayed()));
    }
}
