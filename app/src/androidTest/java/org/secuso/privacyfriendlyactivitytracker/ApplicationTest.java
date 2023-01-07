package org.secuso.privacyfriendlyactivitytracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.secuso.privacyfriendlyactivitytracker.tutorial.TutorialActivity;

public class ApplicationTest {
    @Rule
    public ActivityScenarioRule<TutorialActivity> activityRule =
            new ActivityScenarioRule<>(TutorialActivity.class);

    @Rule
    public GrantPermissionRule activityRecognitionPermission = (android.os.Build.VERSION.SDK_INT >= 29 ? GrantPermissionRule.grant(Manifest.permission.ACTIVITY_RECOGNITION) : null);
    @Rule
    public GrantPermissionRule foregroundServicePermission = (android.os.Build.VERSION.SDK_INT >= 34 ? GrantPermissionRule.grant(Manifest.permission.FOREGROUND_SERVICE_HEALTH) : null);
    @Rule
    public GrantPermissionRule postNotificatuionsPermission = (android.os.Build.VERSION.SDK_INT >= 32 ? GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS) : null);

    @Test
    public void canStartApp() {
        onView(withText(R.string.skip)).perform(ViewActions.click());
        onView(withText(R.string.day)).perform(ViewActions.click());
        onView(withText(R.string.day)).check(ViewAssertions.matches(ViewMatchers.isSelected()));
    }
}