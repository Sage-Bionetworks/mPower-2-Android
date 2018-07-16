package org.sagebionetworks.research.mpower;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppActiveUIStepTests extends UITestHelper {
    public static final String TREMOR_ACTIVE_TITLE = "Hold the phone still in your %@ hand.";
    public static final String FORWARD_ACTION_TEXT = "Restart Test";
    public static final String SKIP_ACTION_TEXT = "Review Instructions";
    public static final String UNIT_LABEL_TEXT = "SECONDS";

    @Test
    public void test_TremorActive_Left() {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY, true);
        this.navigateForwardNTimes(1);
        // We make only the left hand go so we have a predictable order.
        onView(withText("I can only perform this activity with my LEFT hand.")).perform(click());
        this.navigateForwardNTimes(4);
        this.testCommon(TREMOR_ACTIVE_TITLE.replaceAll("%@", "%@"));
    }

    @Test
    public void test_TremorActive_Right() {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY, true);
        this.navigateForwardNTimes(1);
        // We make only the right hand go so we have a predictable order.
        onView(withText("I can only perform this activity with my RIGHT hand.")).perform(click());
        this.navigateForwardNTimes(4);
        this.testCommon(TREMOR_ACTIVE_TITLE.replaceAll("%@", "%@"));
    }

    private void testCommon(String title) {
        checkVisible(R.id.rs2_step_navigation_action_cancel, R.id.rs2_title, R.id.countdownDial,
                R.id.countLabel, R.id.unitLabel, R.id.rs2_step_navigation_action_forward,
                R.id.rs2_step_navigation_action_skip);
        onView(withId(R.id.rs2_title)).check(matches(withText(title)));
        onView(withId(R.id.unitLabel)).check(matches(withText(UNIT_LABEL_TEXT)));
        onView(withId(R.id.rs2_step_navigation_action_forward)).check(matches(withText(FORWARD_ACTION_TEXT)));
        onView(withId(R.id.rs2_step_navigation_action_skip)).check(matches(withText(SKIP_ACTION_TEXT)));

    }
}
