package org.sagebionetworks.research.mpower;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.rule.ActivityTestRule;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.AnyOf;
import org.junit.Rule;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.threeten.bp.Instant;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;

public abstract class UITestHelper {
    public static final String TREMOR_SHARED_PREFS_KEY = "Tremor";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class,
            true, false);

    /**
     * Configures Shared Preferences to contain a last run date corresponding to a first run and then launches the
     * activity.
     */
    public void setupActivity(String sharedPrefsKey, boolean isFirstRun) {
        SharedPreferences prefs = getInstrumentation().getTargetContext()
                .getSharedPreferences(sharedPrefsKey, Context.MODE_PRIVATE);
        // lastRunMillis is set up to ensure the value of isFirstRun is the given value.
        long lastRunMillis = isFirstRun ? Instant.EPOCH.toEpochMilli() : Instant.now().minusSeconds(60).toEpochMilli();
        prefs.edit().putLong(PerformTaskFragment.LAST_RUN_KEY, lastRunMillis).apply();
        mActivityRule.launchActivity(new Intent());
    }

    /**
     * Checks that all of the given views have an effective visibility of GONE. This should be used if you really care
     * that the view is gone and doesn't just have 0 alpha.
     * @param views The views to check
     */
    protected void checkGone(int... views) {
        for (int view : views) {
            onView(withId(view))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    /**
     * Checks that all of the given views have an alpha value of 0. This should be used if you really care that the
     * view has zero alpha and isn't just gone.
     * @param views the views to check
     */
    protected void checkZeroAlpha(int... views) {
        for (int view : views) {
            onView(withId(view))
                    .check(matches(withAlpha(0)));
        }
    }

    /**
     * Checks that all of the given views are invisible to the user. This method doesn't care whether the invisibility
     * is acheived through having an alpha value of 0 or having an efficive Visibility of GONE or INVISIBLE
     * @param views the views to check
     */
    protected void checkInvisible(int... views) {
        for (int view : views) {
            onView(withId(view))
                    .check(matches(AnyOf.anyOf(withAlpha(0), withEffectiveVisibility(Visibility.GONE),
                            withEffectiveVisibility(Visibility.INVISIBLE))));
        }
    }

    /**
     * Checks that the given views are visible to the user. This means they have a Visibility of VISIBLE and and alpha
     * that isn't 0.
     * @param views the views to check
     */
    protected void checkVisible(int... views) {
        for (int view : views) {
            onView(withId(view))
                    .check(matches(AllOf.allOf(withEffectiveVisibility(Visibility.VISIBLE), not(withAlpha(0)))));
        }
    }

    /**
     * Navigates forward the given number of times
     */
    protected void navigateForwardNTimes(int numberOfTimes) {
        for (int i = 0; i < numberOfTimes; i++) {
            onView(withId(R.id.rs2_step_navigation_action_forward))
                    .perform(click());
        }
    }
}
