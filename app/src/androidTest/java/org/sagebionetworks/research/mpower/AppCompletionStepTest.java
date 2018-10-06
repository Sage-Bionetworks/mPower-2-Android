package org.sagebionetworks.research.mpower;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppCompletionStepTest extends UITestHelper {
    public static final String TREMOR_COMPLETION_TITLE = "Great job!";
    public static final String TREMOR_COMPLETION_TEXT = "You just completed your Tremor Test activity.";
    public static final String COMPLETION_FORWARD_ACTION_TEXT = "Done";

    @Test
    public void test_TremorCompletion() throws InterruptedException {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY, true);
        this.navigateForwardNTimes(1);
        // We make only the right hand go so we have to navigate forward fewer times.
        onView(withText("I can only perform this activity with my RIGHT hand.")).perform(click());
        this.navigateForwardNTimes(4);
        // Wait 31 seconds for the Active step to finish
        Thread.sleep(31000);
        checkVisible(R.id.beginLabel, R.id.rs2_title, R.id.rs2_image_view, R.id.rs2_step_navigation_action_forward);
        onView(withId(R.id.beginLabel)).check(matches(withText(TREMOR_COMPLETION_TITLE)));
        onView(withId(R.id.rs2_title)).check(matches(withText(TREMOR_COMPLETION_TEXT)));
        onView(withId(R.id.rs2_step_navigation_action_forward)).check(matches(withText(COMPLETION_FORWARD_ACTION_TEXT)));
    }
}
