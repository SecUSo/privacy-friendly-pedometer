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
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.secuso.privacyfriendlyactivitytracker.R;

import java.util.Arrays;

/**
 * Uses the accelerometer to detect steps.
 * Publishes the detected steps to any subscriber.
 *
 * @author Tobias Neidig
 * @version 20160802
 */

public class AccelerometerStepDetectorService extends AbstractStepDetectorService {
    public static final boolean debug = false;
    private static final String LOG_TAG = AccelerometerStepDetectorService.class.getName();
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private float last_sign;
    private float[] last_extrema = new float[2];
    private float last_acceleration_value;
    private float last_acceleration_diff;
    private long last_step_time;
    private long[] mLastStepDeltas = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private int mLastStepDeltasIndex = 0;
    private float[] mLastStepAccelerationDeltas = {-1, -1, -1, -1, -1, -1};
    private int mLastStepAccelerationDeltasIndex = 0;
    private float accelerometerThreshold;
    private int valid_steps = 0;
    private int validStepsThreshold = 0;
    //private float[]

    /**
     * Creates an AccelerometerStepDetectorService.
     */
    public AccelerometerStepDetectorService() {
        super();
        // required empty constructor
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        accelerometerThreshold = Float.parseFloat(sharedPref.getString(getString(R.string.pref_accelerometer_threshold), "0.75"));
        validStepsThreshold = Integer.parseInt(sharedPref.getString(getString(R.string.pref_accelerometer_steps_threshold), "10"));
    }

    @Override
    protected void onHandleWork(@NonNull @NotNull Intent intent) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        if (event.values.length != 3) {
            Log.e(LOG_TAG, "Invalid sensor values.");
        }

        // the following part will add some basic low/high-pass filter
        // to ignore earth acceleration
        final float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
        float acceleration = linear_acceleration[0] + linear_acceleration[1] + linear_acceleration[2];
        float current_sign = Math.signum(acceleration);

        if (current_sign == last_sign) {
            // the maximum is not reached yet, keep on waiting
            return;
        }

        if (!isSignificantValue(acceleration)) {
            // not significant (acceleration delta is too small)
            return;
        }

        float acceleration_diff = Math.abs(last_extrema[current_sign < 0 ? 1 : 0] /* the opposite */ - acceleration);
        if (!isAlmostAsLargeAsPreviousOne(acceleration_diff)) {
            if (debug) Log.i(LOG_TAG, "Not as large as previous");
            last_acceleration_diff = acceleration_diff;
            return;
        }

        if (!wasPreviousLargeEnough(acceleration_diff)) {
            if (debug) Log.i(LOG_TAG, "Previous not large enough");
            last_acceleration_diff = acceleration_diff;
            return;
        }

        long current_step_time = System.currentTimeMillis();

        if (last_step_time > 0) {
            long step_time_delta = current_step_time - last_step_time;

            // Ignore steps with more than 180bpm and less than 20bpm
            if (step_time_delta < 60 * 1000 / 180) {
                if (debug) Log.i(LOG_TAG, "Too fast.");
                return;
            } else if (step_time_delta > 60 * 1000 / 20) {
                if (debug) Log.i(LOG_TAG, "Too slow.");
                last_step_time = current_step_time;
                valid_steps = 0;
                return;
            }

            // check if this occurrence is regular with regard to the step frequency data
            if (!isRegularlyOverTime(step_time_delta)) {
                last_step_time = current_step_time;
                if (debug) Log.i(LOG_TAG, "Not regularly over time.");
                return;
            }
            last_step_time = current_step_time;

            // check if this occurrence is regular with regard to the acceleration data
            if (!isRegularlyOverAcceleration(acceleration_diff)) {
                last_acceleration_value = acceleration;
                last_acceleration_diff = acceleration_diff;
                if (debug)
                    Log.i(LOG_TAG, "Not regularly over acceleration" + Arrays.toString(mLastStepAccelerationDeltas));
                valid_steps = 0;
                return;
            }
            last_acceleration_value = acceleration;
            last_acceleration_diff = acceleration_diff;
            // okay, finally this has to be a step
            valid_steps ++;
            if (debug)
                Log.i(LOG_TAG, "Detected step. Valid steps = " + valid_steps);
            // count it only if we got more than validStepsThreshold steps
            if(valid_steps == validStepsThreshold){
                this.onStepDetected(valid_steps);
            }else if(valid_steps > validStepsThreshold){
                this.onStepDetected(1);
            }
        }

        last_step_time = current_step_time;
        last_acceleration_value = acceleration;
        last_acceleration_diff = acceleration_diff;
        last_sign = current_sign;
        last_extrema[current_sign < 0 ? 0 : 1] = acceleration;
    }

    /**
     * Determines if this value is significant.
     *
     * @param val the value to check
     * @return true if it is significant else false
     */
    private boolean isSignificantValue(float val) {
        return Math.abs(val) > accelerometerThreshold;
    }

    /**
     * The current acceleration difference has to be almost as large as the last one.
     *
     * @param diff The acceleration difference between current and last value
     * @return true if almost as large as last one
     */
    private boolean isAlmostAsLargeAsPreviousOne(float diff) {
        return diff > last_acceleration_diff * 0.5;
    }

    /**
     * Determines if the last maximum was great enough
     *
     * @param diff the current acceleration diff
     * @return true if was great enough else false
     */
    private boolean wasPreviousLargeEnough(float diff) {
        return last_acceleration_diff > diff / 3;
    }

    /**
     * Checks if the given delta time (between current and last step) is regularly.
     * The value is regularly if at most 20 percent of the older values differs from the given value
     * significantly.
     *
     * @param delta The difference between current and last step time
     * @return true if is regularly else false
     */
    private boolean isRegularlyOverTime(long delta) {
        mLastStepDeltas[mLastStepDeltasIndex] = delta;
        mLastStepDeltasIndex = (mLastStepDeltasIndex + 1) % mLastStepDeltas.length;

        int numIrregularValues = 0;
        for (long mLastStepDelta : mLastStepDeltas) {
            if (Math.abs(mLastStepDelta - delta) > 200) {
                numIrregularValues++;
                break;
            }
        }

        return numIrregularValues < 1;//mLastStepDeltas.length*0.2;
    }

    /**
     * Checks if the given diff (between current and last acceleration data) is regularly in respect
     * to the older values.
     * The value is regularly if at most 20 percent of the older values differs from the given value
     * significantly.
     *
     * @param diff The difference between current and last acceleration value
     * @return true if is regularly else false
     */
    private boolean isRegularlyOverAcceleration(float diff) {
        mLastStepAccelerationDeltas[mLastStepAccelerationDeltasIndex] = diff;
        mLastStepAccelerationDeltasIndex = (mLastStepAccelerationDeltasIndex + 1) % mLastStepAccelerationDeltas.length;
        int numIrregularAccelerationValues = 0;
        for (float mLastStepAccelerationDelta : mLastStepAccelerationDeltas) {
            if (Math.abs(mLastStepAccelerationDelta - last_acceleration_diff) > 0.5) {
                numIrregularAccelerationValues++;
                break;
            }
        }
        return numIrregularAccelerationValues < mLastStepAccelerationDeltas.length * 0.2;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if (key.equals(getString(R.string.pref_accelerometer_threshold))) {
            accelerometerThreshold = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_accelerometer_threshold), "0.75"));
        }
        if (key.equals(getString(R.string.pref_accelerometer_steps_threshold))) {
            validStepsThreshold = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_accelerometer_steps_threshold), "10"));
        }
    }
}