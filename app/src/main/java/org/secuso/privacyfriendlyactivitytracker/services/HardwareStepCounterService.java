package org.secuso.privacyfriendlyactivitytracker.services;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import org.secuso.privacyfriendlyactivitytracker.utils.AndroidVersionHelper;

/**
 * Uses the hardware step counter sensor to detect steps.
 * Publishes the detected steps to any subscriber.
 *
 * @author Jens Kehne
 * @version 20161126
 */

public class HardwareStepCounterService extends AbstractStepDetectorService {

    private static final String LOG_TAG = HardwareStepCounterService.class.getName();

    /**
     * Number of steps which the user went today
     * This is used when step counter is used.
     */
    private float mStepOffset = -1;

    /**
     * Creates an HardwareStepDetectorService.
     */
    public HardwareStepCounterService(){
        this("");
        // required empty constructor
    }

    /**
     * Creates an HardwareStepDetectorService.
     *
     * @param name Name for the worker thread, use it for debugging purposes
     */
    public HardwareStepCounterService(String name) {
        super(name);
        Log.i(LOG_TAG, "Created step counter service");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER)
            return;

        if (this.mStepOffset < 0) {
            this.mStepOffset = event.values[0];
        }

        if (this.mStepOffset > event.values[0]) {
            // this should never happen?
            return;
        }

        // publish difference between last known step count and the current ones.
        this.onStepDetected((int) (event.values[0] - mStepOffset));
        // Set offset to current value so we know it at next event
        mStepOffset = event.values[0];
    }

    @SuppressLint("InlinedApi")
    @Override
    public int getSensorType() {
        if (AndroidVersionHelper.supportsStepCounter(getPackageManager())) {
            return Sensor.TYPE_STEP_COUNTER;
        } else {
            return 0;
        }
    }
}
