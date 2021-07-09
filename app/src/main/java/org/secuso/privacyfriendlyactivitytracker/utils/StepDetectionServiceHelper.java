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
package org.secuso.privacyfriendlyactivitytracker.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import org.secuso.privacyfriendlyactivitytracker.Factory;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.persistence.TrainingPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.receivers.HardwareStepCountReceiver;
import org.secuso.privacyfriendlyactivitytracker.receivers.MotivationAlertReceiver;
import org.secuso.privacyfriendlyactivitytracker.receivers.StepCountPersistenceReceiver;
import org.secuso.privacyfriendlyactivitytracker.receivers.WidgetReceiver;

import java.util.Calendar;
import java.util.Date;

/**
 * Helper class to start and stop the necessary services
 *
 * @author Tobias Neidig
 * @version 20160729
 */
public class StepDetectionServiceHelper {

    private static final String LOG_CLASS = StepDetectionServiceHelper.class.getName();

    /**
     * Starts the step detection, persistence service and notification service if they are enabled in settings.
     *
     * @param context The application context.
     */
    public static void startAllIfEnabled(Context context) {
        startAllIfEnabled(false, context);
    }

    public static void startAllIfEnabled(boolean forceRealTimeStepDetection, Context context){
        Log.i(LOG_CLASS, "Start of all services requested");
        // Start the step detection if enabled or training is active
        if (isStepDetectionEnabled(context)) {
            if(forceRealTimeStepDetection || isRealTimeStepDetectionRequired(context) || !AndroidVersionHelper.isHardwareStepCounterEnabled(context.getPackageManager())) {
                Log.i(LOG_CLASS, "Start step detection");
                StepDetectionServiceHelper.startStepDetection(context);
                // schedule stepCountPersistenceService
                StepDetectionServiceHelper.schedulePersistenceService(context);
            }else{
                Log.i(LOG_CLASS, "Schedule hardware step counter request");
                StepDetectionServiceHelper.startHardwareStepCounter(context);
            }
        }

        if(isMotivationAlertEnabled(context)){
            Log.i(LOG_CLASS, "Schedule motivation alert");
            // set motivation alert
            setMotivationAlert(context);
        }
    }

    public static void stopAllIfNotRequired(Context context){
        stopAllIfNotRequired(true, context);
        WidgetReceiver.forceWidgetUpdate(context);
    }

    public static void stopAllIfNotRequired(boolean forceSave, Context context){
        // Start the step detection if enabled or training is active
        if (!isStepDetectionEnabled(context)) {
            Log.i(LOG_CLASS, "Stopping all services");
            if(forceSave){
                // un-schedule stepCountPersistenceService
                // persistence service will stop step detection service.
                StepDetectionServiceHelper.cancelPersistenceService(forceSave, context);
            }else {
                StepDetectionServiceHelper.stopStepDetection(context);
            }
            StepDetectionServiceHelper.stopHardwareStepCounter(context);
        }else{
            if(!isRealTimeStepDetectionRequired(context) && AndroidVersionHelper.isHardwareStepCounterEnabled(context.getPackageManager())){
                Log.i(LOG_CLASS, "Stopping realtime step detection and scheduling hardware step counter");
                // if step detection is required but no real time step count is necessary and hw step counter
                // is available we stop the real time step detection and enable the hardware step counter.
                if(forceSave){
                    // un-schedule stepCountPersistenceService
                    // persistence service will stop step detection service.
                    StepDetectionServiceHelper.cancelPersistenceService(forceSave, context);
                }else {
                    StepDetectionServiceHelper.stopStepDetection(context);
                }
                StepDetectionServiceHelper.startHardwareStepCounter(context);
            }else {
                Log.i(LOG_CLASS, "Not stopping services b.c. they are required");
            }
        }

        if(!isMotivationAlertEnabled(context)){
            // cancel motivation alert
            cancelMotivationAlert(context);
        }
    }
    /**
     * Starts the step detection service
     *
     * @param context The application context
     */
    public static void startStepDetection(Context context) {
        Log.i(LOG_CLASS, "Started step detection service.");
        Intent stepDetectorServiceIntent = new Intent(context, Factory.getStepDetectorServiceClass(context.getPackageManager()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getApplicationContext().startForegroundService(stepDetectorServiceIntent);
        } else {
            context.getApplicationContext().startService(stepDetectorServiceIntent);
        }
        WidgetReceiver.forceWidgetUpdate(context);
    }

    /**
     * Stops the step detection service
     *
     * @param context The application context
     */
    public static void stopStepDetection(Context context){
        Log.i(LOG_CLASS, "Stopping step detection service.");
        Intent stepDetectorServiceIntent = new Intent(context, Factory.getStepDetectorServiceClass(context.getPackageManager()));
        if(!context.getApplicationContext().stopService(stepDetectorServiceIntent)){
            Log.w(LOG_CLASS, "Stopping of service failed or it is not running.");
        }
    }

    public static void startHardwareStepCounter(Context context){
        Intent hardwareStepCounterServiceIntent = new Intent(context, HardwareStepCountReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, hardwareStepCounterServiceIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 5);

        // Set inexact repeating alarm
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTime().getTime(), AlarmManager.INTERVAL_HOUR, sender);
        Log.i(LOG_CLASS, "Scheduled hardware step counter alert at start time " + calendar.toString());
    }

    public static void stopHardwareStepCounter(Context context){
        Intent hardwareStepCounterServiceIntent = new Intent(context, HardwareStepCountReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, hardwareStepCounterServiceIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
        Log.i(LOG_CLASS, "Canceling hardware step counter alert");
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

    /**
     * Cancel the scheduled persistence service
     * @param forceSave if true the persistence service will be execute now and canceled after
     * @param context The application context
     */
    public static void cancelPersistenceService(boolean forceSave, Context context){
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

    /**
     * Is the step detection enabled? This could be the case if the permanent step counter or a training
     * session is active
     * @param context The application context
     * @return true if step detection is enabled
     */
    public static boolean isStepDetectionEnabled(Context context) {
        // Get user preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isStepDetectionEnabled = sharedPref.getBoolean(context.getString(R.string.pref_step_counter_enabled), true);
        boolean isWalkingModeLearningActive = sharedPref.getBoolean(context.getString(R.string.pref_walking_mode_learning_active), false);
        boolean isDistanceMeasurementActive = sharedPref.getLong(context.getString(R.string.pref_distance_measurement_start_timestamp), -1) > 0;
        return isStepDetectionEnabled || (TrainingPersistenceHelper.getActiveItem(context) != null) || isWalkingModeLearningActive || isDistanceMeasurementActive;
    }

    /**
     * Do we need real time step detection or is it ok if we do some more calculation in background
     * and send step detection events delayed?
     * @param context The application context
     * @return true if real time step detection is required
     */
    public static boolean isRealTimeStepDetectionRequired(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isWalkingModeLearningActive = sharedPref.getBoolean(context.getString(R.string.pref_walking_mode_learning_active), false);
        boolean isDistanceMeasurementActive = sharedPref.getLong(context.getString(R.string.pref_distance_measurement_start_timestamp), -1) > 0;
        boolean isTrainingActive = (TrainingPersistenceHelper.getActiveItem(context) != null);
        return isTrainingActive || isWalkingModeLearningActive || isDistanceMeasurementActive;

    }

    /**
     * Is the motivation alert notification enabled by user?
     * @param context The application context
     * @return true if enabled
     */
    public static boolean isMotivationAlertEnabled(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(context.getString(R.string.pref_notification_motivation_alert_enabled), true);
    }

    /**
     * Schedules (or updates) the motivation alert notification alarm
     * @param context The application context
     */
    public static void setMotivationAlert(Context context){
        Log.i(LOG_CLASS, "Setting motivation alert alarm");
        Intent motivationAlertIntent = new Intent(context, MotivationAlertReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 1, motivationAlertIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        long timestamp = sharedPref.getLong(context.getString(R.string.pref_notification_motivation_alert_time), 64800000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if(calendar.before(Calendar.getInstance())){
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Set alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }else{
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
        Log.i(LOG_CLASS, "Scheduled motivation alert at start time " + calendar.toString());
    }

    /**
     * Cancels the motivation alert (if any)
     * @param context The application context
     */
    public static void cancelMotivationAlert(Context context){
        Log.i(LOG_CLASS, "Canceling motivation alert alarm");
        Intent motivationAlertIntent = new Intent(context, MotivationAlertReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 1, motivationAlertIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}
