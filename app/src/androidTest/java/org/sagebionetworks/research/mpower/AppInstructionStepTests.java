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
@SuppressWarnings("PMD")
public class AppInstructionStepTests extends UITestHelper {
    public static final int TREMOR_PROGRESS_MAX = 3;
    // Instruction Step 1
    public static final int TREMOR_INSTRUCTION_STEP_1_PROGRESS = 1;
    public static final String TREMOR_INSTRUCTION_STEP_1_TITLE = "Hold the phone still";
    public static final String TREMOR_INSTRUCTION_STEP_1_TEXT = "While sitting down with your feet resting flat on the floor, "
            + "you will be holding the phone still in your lap for 30 seconds. You will do this first with one hand and "
            + "then with your other hand.";
    public static final String TREMOR_INSTRUCTION_STEP_1_FORWARD_ACTION_TEXT = "Got it";
    // Instruction Step 2
    public static final int TREMOR_INSTRUCTION_STEP_2_PROGRESS = 2;
    public static final String TREMOR_INSTRUCTION_STEP_2_TITLE = "Sit down";
    public static final String TREMOR_INSTRUCTION_STEP_2_TEXT = "Find a spot to sit where you can have your feet " +
            "resting flat on the floor.";
    public static final String TREMOR_INSTRUCTION_STEP_2_FORWARD_ACTION_TEXT = "Got a spot";
    // Hand Instruction Step
    public static final int TREMOR_HAND_INSTRUCTION_STEP_PROGRESS = 2;
    public static final String TREMOR_HAND_INSTRUCTION_STEP_TITLE = "Hold the phone with your %@ hand";
    public static final String TREMOR_HAND_INSTRUCTION_STEP_TEXT = "With your hand in your lap, hold your phone still with " +
            "the screen facing up.";
    public static final String TREMOR_HAND_INSTRUCTION_STEP_FORWARD_ACTION_TEXT = "Hold phone";

    // NOTE: For this tests with randomization involved we will deliberately selected either only left or only right hand
    // can perform this task to get a deterministic order.

    @Test
    public void test_TremorInstruction_1() {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY, true);
        this.navigateForwardNTimes(2);
        this.testCommon(TREMOR_INSTRUCTION_STEP_1_TITLE, TREMOR_INSTRUCTION_STEP_1_TEXT,
                TREMOR_INSTRUCTION_STEP_1_FORWARD_ACTION_TEXT, TREMOR_INSTRUCTION_STEP_1_PROGRESS, TREMOR_PROGRESS_MAX);
    }

    @Test
    public void test_TremorInstruction_2() {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY, true);
        this.navigateForwardNTimes(3);
        this.testCommon(TREMOR_INSTRUCTION_STEP_2_TITLE, TREMOR_INSTRUCTION_STEP_2_TEXT,
                TREMOR_INSTRUCTION_STEP_2_FORWARD_ACTION_TEXT, TREMOR_INSTRUCTION_STEP_2_PROGRESS, TREMOR_PROGRESS_MAX);
    }

    @Test
    public void test_TremorInstruction_LeftHand() {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY, true);
        this.navigateForwardNTimes(1);
        onView(withText("I can only perform this activity with my LEFT hand.")).perform(click());
        this.navigateForwardNTimes(3);
        this.testCommon(TREMOR_HAND_INSTRUCTION_STEP_TITLE.replaceAll("%@", "LEFT"),
                TREMOR_HAND_INSTRUCTION_STEP_TEXT.replaceAll("%@", "LEFT"),
                TREMOR_HAND_INSTRUCTION_STEP_FORWARD_ACTION_TEXT, TREMOR_HAND_INSTRUCTION_STEP_PROGRESS, TREMOR_PROGRESS_MAX);
    }

    @Test
    public void test_TremorInstruction_RightHand() {
        this.setupActivity(TREMOR_SHARED_PREFS_KEY, true);
        this.navigateForwardNTimes(1);
        onView(withText("I can only perform this activity with my RIGHT hand.")).perform(click());
        this.navigateForwardNTimes(3);
        this.testCommon(TREMOR_HAND_INSTRUCTION_STEP_TITLE.replaceAll("%@", "RIGHT"),
                TREMOR_HAND_INSTRUCTION_STEP_TEXT.replaceAll("%@", "RIGHT"),
                TREMOR_HAND_INSTRUCTION_STEP_FORWARD_ACTION_TEXT, TREMOR_HAND_INSTRUCTION_STEP_PROGRESS, TREMOR_PROGRESS_MAX);
    }

    private void testCommon(String title, String text, String forwardAction, int progress, int maxProgress) {
        checkVisible(R.id.beginLabel, R.id.rs2_title, R.id.rs2_step_navigation_action_forward, R.id.rs2_progress_bar,
                R.id.rs2_progress_label, R.id.rs2_image_view, R.id.rs2_step_navigation_action_cancel);
        onView(withId(R.id.beginLabel)).check(matches(withText(title)));
        onView(withId(R.id.rs2_title)).check(matches(withText(text)));
        onView(withId(R.id.rs2_step_navigation_action_forward)).check(matches(withText(forwardAction)));
        String progressString = "STEP " + progress + " OF " + maxProgress;
        onView(withId(R.id.rs2_progress_label)).check(matches(withText(progressString)));
    }
}
