package org.secuso.privacyfriendlystepcounter.services;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Generic class for a step detector.
 * Does not detect steps itself - the step detection has to be done in the subclasses.
 *
 * @author Tobias Neidig
 * @version 20160522
 */
public abstract class AbstractStepDetectorService extends IntentService implements SensorEventListener {

    /**
     * Broadcast action identifier for messages broadcasted when new steps were detected
     */
    public static final String BROADCAST_ACTION_STEPS_DETECTED = "org.secuso.privacyfriendlystepcounter.STEPS_DETECTED";
    /**
     * Extra key for new steps which were added since last broadcast.
     */
    public static final String EXTENDED_DATA_NEW_STEPS = "org.secuso.privacyfriendlystepcounter.NEW_STEPS";
    /**
     * Extra key for total step count since service start
     */
    public static final String EXTENDED_DATA_TOTAL_STEPS = "org.secuso.privacyfriendlystepcounter.TOTAL_STEPS";

    private static final String LOG_TAG = AbstractStepDetectorService.class.getName();
    private final IBinder mBinder = new StepDetectorBinder();

    /**
     * Number of steps counted since service start
     */
    private int total_steps = 0;

    /**
     * Class used for the client Binder.
     *
     * @author Tobias Neidig
     * @version 20160611
     */
    public class StepDetectorBinder extends Binder {
        /**
         * Get the number of steps which were taken since service starts.
         *
         * @return Step count since service start
         */
        public int stepsSinceStartup() {
            return total_steps;
        }
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AbstractStepDetectorService(String name) {
        super(name);
    }

    /**
     * Notifies any subscriber about the detected amount of steps
     *
     * @param count The number of detected steps (greater zero)
     */
    protected void onStepDetected(int count) {
        if (count <= 0) {
            return;
        }
        this.total_steps += count;
        Log.i(LOG_TAG, count + " Step(s) detected. Steps since service start: " + this.total_steps);
        // broadcast the new steps
        Intent localIntent = new Intent(BROADCAST_ACTION_STEPS_DETECTED)
                // Add new step count
                .putExtra(EXTENDED_DATA_NEW_STEPS, count)
                .putExtra(EXTENDED_DATA_TOTAL_STEPS, total_steps);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    // has to be implemented by subclasses
    @Override
    public abstract void onSensorChanged(SensorEvent event);

    /**
     * The sensor type(s) on which the step detection service should listen
     *
     * @return Type of sensors requested
     * @see SensorManager#getDefaultSensor
     */
    public abstract int getSensorType();

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // currently doing nothing here.
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "Creating service.");
        // register for sensors
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(this.getSensorType());
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Destroying service.");
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Starting service.");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        // currently doing nothing here.
    }
}
