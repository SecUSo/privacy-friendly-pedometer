package org.secuso.privacyfriendlystepcounter.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.secuso.privacyfriendlystepcounter.Factory;
import org.secuso.privacyfriendlystepcounter.services.StepPermanentNotificationService;

import java.util.Date;

import privacyfriendlyexample.org.secuso.example.R;

/**
 * Receives the on boot complete broadcast and starts
 * the step detection and it's required services if
 * step detection is enabled.
 */
public class OnBootCompletedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get user preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isStepDetectionEnabled = sharedPref.getBoolean(context.getString(R.string.pref_step_counter_enabled), true);
        boolean isPermanentNotificationEnabled = sharedPref.getBoolean(context.getString(R.string.pref_permanent_notification_enabled), true);

        // Start the step detection if enabled
        if (isStepDetectionEnabled) {
            Intent stepDetectorServiceIntent = new Intent(context, Factory.getStepDetectorServiceClass(context.getPackageManager()));
            context.startService(stepDetectorServiceIntent);
            // schedule stepCountPersistenceService
            Intent stepCountPersistenceServiceIntent = new Intent(context, StepCountPersistenceReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(context, 2, stepCountPersistenceServiceIntent, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long l = new Date().getTime();
            am.setRepeating(AlarmManager.RTC_WAKEUP, l, AlarmManager.INTERVAL_HOUR, sender);
        }

        // start the (permanent notification if enabled
        if(isStepDetectionEnabled && isPermanentNotificationEnabled){
            Intent stepCountNotificationServiceIntent = new Intent(context, StepPermanentNotificationService.class);
            context.startService(stepCountNotificationServiceIntent);
        }
    }
}
