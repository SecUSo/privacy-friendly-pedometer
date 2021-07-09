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
package org.secuso.privacyfriendlyactivitytracker.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.TriggerEventListener;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountDbHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModeDbHelper;
import org.secuso.privacyfriendlyactivitytracker.utils.AndroidVersionHelper;

import java.util.Calendar;

import static org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED;

/**
 * Hardware step counter service - this service uses STEP_COUNTER to detect steps.
 *
 * @author Tobias Neidig
 * @version 20170814
 */

public class HardwareStepCounterService extends AbstractStepDetectorService{
    private static final String LOG_TAG = HardwareStepCounterService.class.getName();
    protected TriggerEventListener listener;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public HardwareStepCounterService() {
        super();
    }

    @Override
    protected void onHandleWork(@NonNull @NotNull Intent intent) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(LOG_TAG, "Received onSensorChanged");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HardwareStepCounterService.this);
        float numberOfHWStepsSinceLastReboot = event.values[0];
        float numberOfHWStepsOnLastSave = sharedPref.getFloat(HardwareStepCounterService.this.getString(R.string.pref_hw_steps_on_last_save), 0);
        float numberOfNewSteps = numberOfHWStepsSinceLastReboot - numberOfHWStepsOnLastSave;
        Log.i(LOG_TAG, numberOfHWStepsSinceLastReboot + " - " + numberOfHWStepsOnLastSave + " = " + numberOfNewSteps);
        // Store new steps
        onStepDetected((int) numberOfNewSteps);
        // store steps since last reboot
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(getString(R.string.pref_hw_steps_on_last_save), numberOfHWStepsSinceLastReboot);
        editor.apply();
    }

    /**
     * Notifies any subscriber about the detected amount of steps
     *
     * @param count The number of detected steps (greater zero)
     */
    @Override
    protected void onStepDetected(int count) {
        if (count <= 0) {
            return;
        }
        Log.i(LOG_TAG, count + " Step(s) detected");
        // broadcast the new steps
        Intent localIntent = new Intent(BROADCAST_ACTION_STEPS_DETECTED)
                // Add new step count
                .putExtra(EXTENDED_DATA_NEW_STEPS, count)
                .putExtra(EXTENDED_DATA_TOTAL_STEPS, count);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        // Save new steps
        StepCount stepCount = new StepCount();
        stepCount.setStepCount(count);
        stepCount.setWalkingMode(new WalkingModeDbHelper(this).getActiveWalkingMode());
        stepCount.setEndTime(Calendar.getInstance().getTime().getTime());
        new StepCountDbHelper(this).addStepCount(stepCount);
        Log.i(LOG_TAG, "Stored " + count + " steps");

        // broadcast the event
        localIntent = new Intent(BROADCAST_ACTION_STEPS_SAVED);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        updateNotification();
        stopSelf();
    }

    @Override
    public int getSensorType() {
        Log.i(LOG_TAG, "getSensorType STEP_COUNTER");
        if (AndroidVersionHelper.isHardwareStepCounterEnabled(this.getPackageManager())) {
            return Sensor.TYPE_STEP_COUNTER;
        } else {
            return 0;
        }
    }

    @Override
    protected boolean cancelNotificationOnDestroy(){
        return false;
    }
}
