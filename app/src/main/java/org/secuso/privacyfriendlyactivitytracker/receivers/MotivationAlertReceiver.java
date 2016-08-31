package org.secuso.privacyfriendlyactivitytracker.receivers;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.secuso.privacyfriendlyactivitytracker.Factory;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Receives the motivation alert event and notifies the user.
 *
 * @author Tobias Neidig
 * @version 20160729
 */
public class MotivationAlertReceiver extends WakefulBroadcastReceiver {
    public static final int NOTIFICATION_ID = 0;
    private static final String LOG_CLASS = MotivationAlertReceiver.class.getName();
    private Context context;
    private AbstractStepDetectorService.StepDetectorBinder myBinder = null;


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (AbstractStepDetectorService.StepDetectorBinder) service;
            motivate();

            context.getApplicationContext().unbindService(mServiceConnection);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_CLASS, "Motivate the user!");

        this.context = context;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        float criterion = Float.parseFloat(sharedPref.getString(context.getString(R.string.pref_notification_motivation_alert_criterion), "100"));
        if (criterion < 0 || criterion > 100) {
            Log.e(LOG_CLASS, "Invalid motivation criterion. Cannot notify the user.");
            return;
        }

        // bind to service
        Intent serviceIntent = new Intent(context, Factory.getStepDetectorServiceClass(context.getPackageManager()));
        context.getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Shows the motivation notification to user
     */
    private void motivate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        float criterion = Float.parseFloat(sharedPref.getString(context.getString(R.string.pref_notification_motivation_alert_criterion), "100"));
        int stepCount = StepCountPersistenceHelper.getStepCountForDay(Calendar.getInstance(), context);
        if (myBinder != null) {
            stepCount += myBinder.stepsSinceLastSave();
        } else {
            Log.w(LOG_CLASS, "Cannot get steps from binder.");
        }
        int dailyGoal = Integer.parseInt(sharedPref.getString(context.getString(R.string.pref_daily_step_goal), "100"));
        if (dailyGoal * criterion / 100 <= stepCount) {
            Log.i(LOG_CLASS, "No motivation required.");
            // Reschedule alarm for tomorrow
            StepDetectionServiceHelper.startAllIfEnabled(context);
            return;
        }

        Set<String> defaultStringSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.pref_default_notification_motivation_alert_messages)));
        List<String> motivationTexts = new ArrayList<>(sharedPref.getStringSet(context.getString(R.string.pref_notification_motivation_alert_texts),  defaultStringSet));

        if (motivationTexts.size() == 0) {
            Log.e(LOG_CLASS, "Motivation texts are empty. Cannot notify the user.");
            // Reschedule alarm for tomorrow
            StepDetectionServiceHelper.startAllIfEnabled(context);
            return;
        }

        Collections.shuffle(motivationTexts);
        String motivationText = motivationTexts.get(0);

        // Build the notification
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext())
                .setSmallIcon(R.drawable.ic_walk_black_24dp)
                .setContentTitle(context.getString(R.string.motivation_alert_notification_title))
                .setContentText(motivationText)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setLights(ContextCompat.getColor(context, R.color.colorPrimary), 1000, 1000);
        // Notify
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        // Reschedule alarm for tomorrow
        StepDetectionServiceHelper.startAllIfEnabled(context);
    }
}
