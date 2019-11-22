package org.sagebionetworks.research.mpower;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.bridge.android.SingleFragmentActivity;

@LargeTest
@RunWith(AndroidJUnit4.class)
@Ignore
public class MainFragmentTest {

    @Rule
    public ActivityTestRule<SingleFragmentActivity> mActivityTestRule = new ActivityTestRule<>(SingleFragmentActivity.class);

    @Before
    public void setupTest() {
        SingleFragmentActivity activity = mActivityTestRule.getActivity();
        activity.setFragment(new MainFragment());
    }

    @Test
    public void mainFragmentTest() {

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
