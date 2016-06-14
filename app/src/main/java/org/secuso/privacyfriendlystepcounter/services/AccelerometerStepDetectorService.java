package org.secuso.privacyfriendlystepcounter.services;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.Toast;

/**
 * Uses the accelerometer to detect steps.
 * Publishes the detected steps to any subscriber.
 * For debug purpose also the raw data curves are published.
 *
 * @author Tobias Neidig
 * @version 20160601
 */
public class AccelerometerStepDetectorService extends AbstractStepDetectorService
{
    /**
     * Creates an AccelerometerStepDetectorService.
     */
    public AccelerometerStepDetectorService(){
        this("");
        // required empty constructor
    }

    /**
     * Creates an AccelerometerStepDetectorService.
     *
     * @param name Name for the worker thread, use it for debugging purposes
     */
    public AccelerometerStepDetectorService(String name) {
        super(name);
        // TODO remove notification if implemented.
        Toast.makeText(this, "Step detection using accelerometer is not supported, yet.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }
}