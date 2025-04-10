/*
    Privacy Friendly Pedometer is licensed under the GPLv3.
    Copyright (C) 2017  Tobias Neidig

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlyactivitytracker;

import static android.content.Context.SENSOR_SERVICE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeNotNull;
import static org.secuso.privacyfriendlyactivitytracker.TestUtils.getResourceString;
import static org.secuso.privacyfriendlyactivitytracker.TestUtils.getText;
import static org.secuso.privacyfriendlyactivitytracker.TestUtils.greaterOrEqual;
import static org.secuso.privacyfriendlyactivitytracker.TestUtils.waitFor;
import static org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper.getLastStepCountEntryForDay;
import static org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper.getStepCountForDay;
import static org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper.storeStepCount;
import static org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper.storeStepCounts;

import android.Manifest;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModePersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.services.AccelerometerStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.tutorial.TutorialActivity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Objects;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StepTest {
    final long startTime = Calendar.getInstance().getTimeInMillis();
    @Rule
    public ActivityScenarioRule<TutorialActivity> activityRule =
            new ActivityScenarioRule<>(TutorialActivity.class);
    @Rule
    public GrantPermissionRule activityRecognitionPermission = (android.os.Build.VERSION.SDK_INT >= 29 ? GrantPermissionRule.grant(Manifest.permission.ACTIVITY_RECOGNITION) : null);
    @Rule
    public GrantPermissionRule writeExternalPermission = (android.os.Build.VERSION.SDK_INT < 19 ? GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE) : null);
    @Rule
    public GrantPermissionRule foregroundServicePermission = (android.os.Build.VERSION.SDK_INT >= 34 ? GrantPermissionRule.grant(Manifest.permission.FOREGROUND_SERVICE_HEALTH) : null);
    @Rule
    public GrantPermissionRule postNotificatuionsPermission = (android.os.Build.VERSION.SDK_INT >= 32 ? GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS) : null);

    AccelerometerStepDetectorService sensorService;
    Context context = getApplicationContext();
    AbstractStepDetectorService.StepDetectorBinder myBinder;
    SensorEvent sensorEvent;
    SensorManager sensorManager;
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String updateIntervalBackup; // back this up, in case this is accidentally run on a physical device
    private ServiceConnection mServiceConnection;

    @Before
    public void setup() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        sharedPref.edit().putBoolean(getResourceString(R.string.pref_step_counter_enabled), true).apply();
        sensorService = AccelerometerStepDetectorService.accelerometerStepDetectorService;
        assumeNotNull(sensorService);
        myBinder = (AbstractStepDetectorService.StepDetectorBinder) sensorService.getmStepDetectorBinder();
        assumeNotNull(myBinder);
        sensorEvent = getEvent();
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorEvent.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        assumeNotNull(sensorEvent.sensor);
        updateIntervalBackup = sharedPref.getString(context.getString(R.string.pref_hw_background_counter_frequency), "3600000");
        try {
            onView(withText(R.string.skip)).perform(ViewActions.click());
        } catch (Exception e) {
            // ignore
        }
    }

    @After
    public void cleanUp() {
        sharedPref.edit().putString(getResourceString(R.string.pref_hw_background_counter_frequency), updateIntervalBackup).apply();
    }

    void fakeSteps(int numberOfSteps) {
        final int betweenStepsDelayMillis = 75;

        for (int i = 0; i < numberOfSteps * 2; i++) {
            sensorEvent.values[1] = 10;
            sensorService.onSensorChanged(sensorEvent);
            onView(isRoot()).perform(waitFor(betweenStepsDelayMillis));
            sensorEvent.values[1] = -10;
            sensorService.onSensorChanged(sensorEvent);
            onView(isRoot()).perform(waitFor(betweenStepsDelayMillis));
        }
    }

    @Test
    public void A_stepsAreCounted() throws Exception {
        final int minStepDelta = 10;

        onView(withText(R.string.day)).perform(ViewActions.click());
        int stepCountBefore = Integer.parseInt(Objects.requireNonNull(getText(TestUtils.withIndex(withId(R.id.stepCount), 1))));
        fakeSteps(40);
        int stepCountAfter = Integer.parseInt(Objects.requireNonNull(getText(TestUtils.withIndex(withId(R.id.stepCount), 1))));
        int stepsSinceLastSave = myBinder.stepsSinceLastSave();
        assertThat(stepsSinceLastSave, is(stepCountAfter - stepCountBefore));
        onView(Objects.requireNonNull(TestUtils.withIndex(withId(R.id.stepCount), 1))).check(matches(Objects.requireNonNull(greaterOrEqual(stepCountBefore + minStepDelta - 1))));

        onView(withText(R.string.week)).perform(ViewActions.click());
        onView(Objects.requireNonNull(TestUtils.withIndex(withId(R.id.stepCount), 1))).check(matches(Objects.requireNonNull(greaterOrEqual(stepCountAfter))));
    }

    private SensorEvent getEvent() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<SensorEvent> constructor = SensorEvent.class.getDeclaredConstructor(int.class);
        constructor.setAccessible(true);
        return constructor.newInstance(3);
    }

    private StepCount fakeStepCount(int steps) {
        StepCount stepCount = new StepCount();
        stepCount.setStartTime(startTime);
        stepCount.setEndTime(Calendar.getInstance().getTimeInMillis());
        stepCount.setStepCount(steps);
        stepCount.setWalkingMode(WalkingModePersistenceHelper.getActiveMode(context));
        return stepCount;
    }

    @Test
    public void B_stepCountsAreStoredInDB() {
        int stepCountBefore = getStepCountForDay(Calendar.getInstance(), context);
        int stepsSinceLastSave = myBinder.stepsSinceLastSave();
        storeStepCounts(myBinder, context, WalkingModePersistenceHelper.getActiveMode(getApplicationContext()));
        int stepCountAfter = getStepCountForDay(Calendar.getInstance(), context);
        assertThat(stepCountAfter, is(stepCountBefore + stepsSinceLastSave));
    }


    private int fakeStepsWithUpdateIntervalMillisPtref(long updateInterValPrefMillis) {
        sharedPref.edit().putString(getResourceString(R.string.pref_hw_background_counter_frequency), Long.toString(updateInterValPrefMillis)).apply();
        storeStepCount(fakeStepCount(1), context);
        waitFor(500);
        fakeSteps(10);
        int stepsSinceLastSave = myBinder.stepsSinceLastSave();
        storeStepCounts(myBinder, context, WalkingModePersistenceHelper.getActiveMode(getApplicationContext()));
        return stepsSinceLastSave;
    }

    @Test
    public void C_stepCountsAreStoredInSameDBEntryifInSameInterval() {
        int actualStepsSinceLastSave = fakeStepsWithUpdateIntervalMillisPtref(3600000);
        assertThat(getLastStepCountEntryForDay(Calendar.getInstance(), context).getStepCount(), greaterThan(actualStepsSinceLastSave));
    }

    @Test
    public void D_stepCountsAreStoredinDifferentDBEntriesIfNotInSameInterval() {
        int actualStepsSinceLastSave = fakeStepsWithUpdateIntervalMillisPtref(10);
        assertThat(getLastStepCountEntryForDay(Calendar.getInstance(), context).getStepCount(), is(actualStepsSinceLastSave));
    }

}