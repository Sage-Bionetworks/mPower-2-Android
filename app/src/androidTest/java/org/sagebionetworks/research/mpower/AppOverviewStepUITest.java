package org.sagebionetworks.research.mpower;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppOverviewStepUITest extends UITestHelper {
    public static final String TREMOR_OVERVIEW_TITLE = "Tremor Test";
    public static final String TREMOR_OVERVIEW_TEXT = "In this activity you will be holding the phone in your lap for 30 "
            + "seconds. You will do this with each hand. Goal is to measure your tremors in your hand.";
    public static final String OVERVIEW_FORWARD_ACTION_TEXT = "Get started";
    public static final String OVERVIEW_SKIP_ACTION_TEXT = "Remind me later";
    public static final String OVERVIEW_ICON_DESCRIPTION_LABEL_TEXT = "This is what you'll need";
    public static final String TREMOR_CENTER_ICON_LABEL = "COMFORTABLE PLACE TO SIT";

    @Test
    public void test_tremorOverview_FirstRun() {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY,true);
        // We test whether views have a VISIBLE visibility instead of match isDisplayed() because it is possible
        // they will be off screen due to the size of the devices screen.
        this.test_tremorOverview_Common();
        // We care that the info button is GONE specifically because someone could still press it otherwise.
        checkGone(R.id.rs2_step_navigation_action_info);
        checkVisible(R.id.rs2_title, R.id.rs2_image_view, R.id.overallIconDescriptionLabel, R.id.centerIconImageView,
                R.id.centerIconLabel);
        onView(withId(R.id.overallIconDescriptionLabel)).check(matches(withText(OVERVIEW_ICON_DESCRIPTION_LABEL_TEXT)));
        onView(withId(R.id.centerIconLabel)).check(matches(withText(TREMOR_CENTER_ICON_LABEL)));
    }

    @Test
    public void test_tremorOverview_NonFirstRun() {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY,false);
        this.test_tremorOverview_Common();
        checkVisible(R.id.rs2_step_navigation_action_info);
        checkInvisible(R.id.rs2_title, R.id.overallIconDescriptionLabel, R.id.centerIconImageView, R.id.centerIconLabel);
    }

    private void test_tremorOverview_Common() {
        checkVisible(R.id.rs2_step_navigation_action_cancel, R.id.beginLabel, R.id.rs2_step_navigation_action_forward,
                R.id.rs2_step_navigation_action_skip);
        onView(withId(R.id.beginLabel)).check(matches(withText(TREMOR_OVERVIEW_TITLE)));
        onView(withId(R.id.rs2_step_navigation_action_forward)).check(matches(withText(OVERVIEW_FORWARD_ACTION_TEXT)));
        onView(withId(R.id.rs2_step_navigation_action_skip)).check(matches(withText(OVERVIEW_SKIP_ACTION_TEXT)));
        checkInvisible(R.id.leftIconImageView, R.id.leftIconLabel, R.id.rightIconImageView, R.id.rightIconLabel);
    }
}
