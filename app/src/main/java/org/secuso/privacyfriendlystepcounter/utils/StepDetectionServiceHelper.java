package org.secuso.privacyfriendlystepcounter.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.secuso.privacyfriendlystepcounter.Factory;
import org.secuso.privacyfriendlystepcounter.persistence.TrainingPersistenceHelper;
import org.secuso.privacyfriendlystepcounter.receivers.StepCountPersistenceReceiver;

import java.util.Calendar;
import java.util.Date;

import org.secuso.privacyfriendlystepcounter.R;

/**
 * Helper class to start and stop the necessary services
 *
 * @author Tobias Neidig
 * @version 20160618
 */
public class StepDetectionServiceHelper {

    private static final String LOG_CLASS = StepDetectionServiceHelper.class.getName();

    /**
     * Starts the step detection, persistence service and notification service if they are enabled in settings.
     *
     * @param context The application context.
     */
    public static void startAllIfEnabled(Context context) {
        Log.i(LOG_CLASS, "Started all services");
        // Start the step detection if enabled or training is active
        if (isStepDetectionEnabled(context)) {
            StepDetectionServiceHelper.startStepDetection(context);
            // schedule stepCountPersistenceService
            StepDetectionServiceHelper.schedulePersistenceService(context);
        }
    }

    public static void stopAllIfNotRequired(Context context){
        stopAllIfNotRequired(true, context);
    }

    public static void stopAllIfNotRequired(boolean forceSave, Context context){
        // Start the step detection if enabled or training is active
        if (!isStepDetectionEnabled(context)) {
            Log.i(LOG_CLASS, "Stopping all services");
            StepDetectionServiceHelper.stopStepDetection(context);
            // schedule stepCountPersistenceService
            StepDetectionServiceHelper.unschedulePersistenceService(forceSave, context);
        }else{
            Log.i(LOG_CLASS, "Not stopping services b.c. they are required");
        }
    }
    /**
     * Starts the step detection service
     *
     * @param context The application context
     */
    public static void startStepDetection(Context context) {
        Log.i(LOG_CLASS, "Started step detection service.");
        Intent      stepDetectorServiceIntent = new Intent(context, Factory.getStepDetectorServiceClass(context.getPackageManager()));
            context.getApplicationContext().startService(stepDetectorServiceIntent);
    }

    /**
     * Stops the step detection service
     *
     * @param context The application context
     */
    public static void stopStepDetection(Context context){
        Log.i(LOG_CLASS, "Stopping step detection service.");
        boolean success = true;
        Intent stepDetectorServiceIntent= new Intent(context, Factory.getStepDetectorServiceClass(context.getPackageManager()));
       // while (success) {
            success = context.getApplicationContext().stopService(stepDetectorServiceIntent);
            Log.i(LOG_CLASS, "stopping service: " + success);
        //}
        if(!context.getApplicationContext().stopService(stepDetectorServiceIntent)){
            Log.w(LOG_CLASS, "Stopping of service failed or it is not running.");
        }
    }

    /**
     *  Schedules the step count persistence service.
     *
     * @param context The application context
     */
    public static void schedulePersistenceService(Context context) {
        Intent stepCountPersistenceServiceIntent = new Intent(context, StepCountPersistenceReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, stepCountPersistenceServiceIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Fire at next half hour
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 30;
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MINUTE, (30-mod));

        // Set repeating alarm
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTime().getTime(), AlarmManager.INTERVAL_HOUR, sender);
        Log.i(LOG_CLASS, "Scheduled repeating persistence service at start time " + calendar.toString());
    }

    // TODO add documentation
    public static void unschedulePersistenceService(boolean forceSave, Context context){
        // force save
        if(forceSave) {
            startPersistenceService(context);
        }
        Intent stepCountPersistenceServiceIntent = new Intent(context, StepCountPersistenceReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, stepCountPersistenceServiceIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

    /**
     * Starts the step detection service
     *
     * @param context The application context
     */
    public static void startPersistenceService(Context context) {
        Log.i(LOG_CLASS, "Started persistence service.");
        Intent stepCountPersistenceServiceIntent = new Intent(context, StepCountPersistenceReceiver.class);
        context.sendBroadcast(stepCountPersistenceServiceIntent);
    }

    public static boolean isStepDetectionEnabled(Context context) {
        // Get user preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isStepDetectionEnabled = sharedPref.getBoolean(context.getString(R.string.pref_step_counter_enabled), true);
        return isStepDetectionEnabled || (TrainingPersistenceHelper.getActiveItem(context) != null);
    }
}
