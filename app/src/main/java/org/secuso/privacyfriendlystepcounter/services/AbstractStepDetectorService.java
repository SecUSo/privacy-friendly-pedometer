package org.secuso.privacyfriendlystepcounter.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.secuso.privacyfriendlystepcounter.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlystepcounter.utils.StepDetectionServiceHelper;

import java.util.Calendar;

import privacyfriendlyexample.org.secuso.example.R;

/**
 * Generic class for a step detector.
 * Does not detect steps itself - the step detection has to be done in the subclasses.
 *
 * @author Tobias Neidig
 * @version 20160618
 */
public abstract class AbstractStepDetectorService extends IntentService implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

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
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver();
    /**
     * The notification id used for permanent step count notification
     */
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    /**
     * Number of steps the user wants to walk every day
     */
    private int dailyStepGoal = 0;
    /**
     * Number of in-database-saved steps.
     */
    private int totalStepsAtLastSave = 0;

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
        public int stepsSinceLastSave() {
            return total_steps;
        }

        /**
         * Resets the step count since last save
         * Is usually called when we saved the steps.
         */
        public void resetStepCount() { total_steps = 0; }
    }

    public class BroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null){
                Log.w(LOG_TAG, "Received intent which is null.");
                return;
            }
            switch(intent.getAction()){
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED:
                    // Steps were saved, reload step count from database
                    getStepsAtLastSave();
                    break;
                default:
            }
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

        // Update notification
        Notification notification = buildNotification(total_steps);
        mNotifyManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Builds the permanent step count notification
     * @param totalStepsSinceLastSave The number of steps since last save
     * @return the new notification
     */
    protected Notification buildNotification(int totalStepsSinceLastSave){
        int totalSteps = totalStepsSinceLastSave + this.totalStepsAtLastSave;

        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(String.format(getString(R.string.notification_text), totalSteps, this.dailyStepGoal))
                .setSmallIcon(R.drawable.ic_directions_walk_65black_30dp);
        mBuilder.setProgress(this.dailyStepGoal, totalSteps, false);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        return mBuilder.build();
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

        // Get daily goal(s) from preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        this.dailyStepGoal = sharedPref.getInt(getString(R.string.pref_daily_step_goal), 10000);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // register for steps-saved-event
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED));
        // load step count from database
        getStepsAtLastSave();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Destroying service.");
        // Unregister sensor listeners
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
        // Cancel notification
        mNotifyManager.cancel(NOTIFICATION_ID);
        // Unregister shared preferences listeners
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        // Force save of step count
        StepDetectionServiceHelper.startPersistenceService(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Starting service.");
        startForeground(NOTIFICATION_ID, buildNotification(this.total_steps));
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Detect changes on preferences and update our internal variable
        if(key.equals(getString(R.string.pref_daily_step_goal))){
            dailyStepGoal = sharedPreferences.getInt(getString(R.string.pref_daily_step_goal), 10000);
        }
    }

    /**
     * Fetches the step count for this day from database
     */
    private void getStepsAtLastSave(){
        totalStepsAtLastSave = StepCountPersistenceHelper.getStepCountForDay(Calendar.getInstance(), getApplicationContext());
    }
}
