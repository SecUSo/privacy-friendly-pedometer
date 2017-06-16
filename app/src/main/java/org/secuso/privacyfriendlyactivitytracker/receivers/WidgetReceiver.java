package org.secuso.privacyfriendlyactivitytracker.receivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.activities.SplashActivity;
import org.secuso.privacyfriendlyactivitytracker.activities.TrainingActivity;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;
import org.secuso.privacyfriendlyactivitytracker.utils.UnitHelper;

import java.util.Calendar;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class WidgetReceiver extends AppWidgetProvider {
    public static final String PAUSE_STEP_DETECTION_ACTION = "org.secuso.privacyfriendlyactivitytracker.PAUSE_STEP_DETECTION_ACTION";
    public static final String CONTINUE_STEP_DETECTION_ACTION = "org.secuso.privacyfriendlyactivitytracker.CONTINUE_STEP_DETECTION_ACTION";
    public static final int DATA_SET_STEPS = 0;
    public static final int DATA_SET_DISTANCE = 1;
    public static final int DATA_SET_CALORIES = 2;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        List<StepCount> stepCounts = StepCountPersistenceHelper.getStepCountsForDay(Calendar.getInstance(), context);
        int stepCount = 0;
        double distance = 0;
        int calories = 0;
        for (StepCount s: stepCounts) {
            stepCount += s.getStepCount();
            distance += s.getDistance();
            calories += s.getCalories(context);
        }

        for (int appWidgetId : appWidgetIds) {
            Bundle appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            // Get view
            RemoteViews rv = getRemoteViews(context, appWidgetOptions);
            int dataSet = sharedPref.getInt(context.getString(R.string.pref_widget_data_set) + appWidgetId, -1);
            // Create an Intent to launch SplashActivity on click on widget
            Intent intent = new Intent(context, SplashActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            rv.setOnClickPendingIntent(R.id.widget, pendingIntent);
            // add intents to buttons
            intent = new Intent(context, WidgetReceiver.class);
            intent.setAction(PAUSE_STEP_DETECTION_ACTION);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            rv.setOnClickPendingIntent(R.id.pause_step_detection, pendingIntent);

            intent = new Intent(context, WidgetReceiver.class);
            intent.setAction(CONTINUE_STEP_DETECTION_ACTION);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            rv.setOnClickPendingIntent(R.id.continue_step_detection, pendingIntent);

            intent = new Intent(context, TrainingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            rv.setOnClickPendingIntent(R.id.start_training, pendingIntent);

            switch (dataSet){
                case DATA_SET_DISTANCE:
                    UnitHelper.FormattedUnitPair distanceUnitPair = UnitHelper.formatKilometers(UnitHelper.metersToKilometers(distance), context);
                    rv.setTextViewText(R.id.value, distanceUnitPair.getValue());
                    rv.setTextViewText(R.id.caption, distanceUnitPair.getUnit());
                    break;
                case DATA_SET_CALORIES:
                    UnitHelper.FormattedUnitPair caloriesUnitPair = UnitHelper.formatCalories(calories, context);
                    rv.setTextViewText(R.id.value, caloriesUnitPair.getValue());
                    rv.setTextViewText(R.id.caption, caloriesUnitPair.getUnit());
                    break;
                case DATA_SET_STEPS:
                default:
                    rv.setTextViewText(R.id.value, String.valueOf(stepCount));
                    rv.setTextViewText(R.id.caption, context.getString(R.string.steps));
                    break;
            }

            if(sharedPref.getBoolean(context.getString(R.string.pref_step_counter_enabled), true)){
                rv.setViewVisibility(R.id.continue_step_detection, GONE);
                rv.setViewVisibility(R.id.pause_step_detection, VISIBLE);
            }else{
                rv.setViewVisibility(R.id.continue_step_detection, VISIBLE);
                rv.setViewVisibility(R.id.pause_step_detection, GONE);
            }

            // Tell the AppWidgetManager to perform an update on the current app widget_1x1
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews rv = getRemoteViews(context, newOptions);
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        forceWidgetUpdate(appWidgetId, context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch(intent.getAction()){
            case PAUSE_STEP_DETECTION_ACTION:
                editor.putBoolean(context.getString(R.string.pref_step_counter_enabled), false);
                editor.apply();
                StepDetectionServiceHelper.stopAllIfNotRequired(context.getApplicationContext());
                break;
            case CONTINUE_STEP_DETECTION_ACTION:
                editor.putBoolean(context.getString(R.string.pref_step_counter_enabled), true);
                editor.apply();
                StepDetectionServiceHelper.startAllIfEnabled(context.getApplicationContext());
                break;
        }
        super.onReceive(context, intent);
    }

    /**
     * Returns the widget's remote view for given size.
     * @param context The application context
     * @param options AppWidgetOptions
     * @return Remote view for widget
     */
    private RemoteViews getRemoteViews(Context context, Bundle options){
        // Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        return getRemoteViews(context, minWidth, minHeight);
    }

    /**
     * Returns the widget's remote view for given size.
     * @param context The application context
     * @param minWidth minimal width from OPTION_APPWIDGET_MIN_WIDTH
     * @param minHeight minimal height from OPTION_APPWIDGET_MIN_HEIGHT
     * @return Remote view for widget
     */
    private RemoteViews getRemoteViews(Context context, int minWidth, int minHeight){
        int columns = (int)(Math.ceil(minWidth + 30d)/70d);
        switch(columns){
            case 1: return new RemoteViews(context.getPackageName(), R.layout.widget_1x1);
            case 2: return new RemoteViews(context.getPackageName(), R.layout.widget_2x1);
            case 3: return new RemoteViews(context.getPackageName(), R.layout.widget_3x1);
            default: return new RemoteViews(context.getPackageName(), R.layout.widget_3x1);
        }
    }

    public static void forceWidgetUpdate(Context context){
        forceWidgetUpdate(null, context);
    }

    public static void forceWidgetUpdate(Integer widgetId, Context context){
        Intent intent = new Intent(context, WidgetReceiver.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids;
        if(widgetId == null) {
            ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetReceiver.class));
        }else{
            ids = new int[]{widgetId};
        }
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}