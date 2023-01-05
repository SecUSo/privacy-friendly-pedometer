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
package org.secuso.privacyfriendlyactivitytracker.persistence;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;
import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Helper to save and restore step count from database.
 *
 * @author Tobias Neidig
 * @version 20160720
 */

public class StepCountPersistenceHelper {

    /**
     * Broadcast action identifier for messages broadcast when step count was saved
     */
    public static final String BROADCAST_ACTION_STEPS_SAVED = "org.secuso.privacyfriendlystepcounter.STEPS_SAVED";
    public static final String BROADCAST_ACTION_STEPS_UPDATED = "org.secuso.privacyfriendlystepcounter.STEPS_UPDATED";
    public static final String BROADCAST_ACTION_STEPS_INSERTED = "org.secuso.privacyfriendlystepcounter.STEPS_INSERTED";
    public static String LOG_CLASS = StepCountPersistenceHelper.class.getName();
    private static SQLiteDatabase db = null;

    /**
     * Stores the current step count to database and resets the step counter in step-detector
     *
     * @param serviceBinder The binder for stepCountService
     * @param context       The application context
     * @return true if save was successful else false.
     */
    public static boolean storeStepCounts(IBinder serviceBinder, Context context, WalkingMode walkingMode) {
        if (serviceBinder == null) {
            Log.e(LOG_CLASS, "Cannot store step count because service binder is null.");
            return false;
        }
        AbstractStepDetectorService.StepDetectorBinder myBinder = (AbstractStepDetectorService.StepDetectorBinder) serviceBinder;
        StepCountDbHelper stepCountDbHelper = new StepCountDbHelper(context);

        // Get the steps since last save
        int stepCountSinceLastSave = myBinder.stepsSinceLastSave();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        long updateInterval = Long.parseLong(sharedPref.getString(context.getString(R.string.pref_hw_background_counter_frequency), "3600000"));
        StepCount lastStoredStepCount = stepCountDbHelper.getLatestStepCount();;
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long currentUpdateIntervalStartTime = currentTime - (updateInterval > 0 ? currentTime % updateInterval : 0);
        if(lastStoredStepCount == null || (lastStoredStepCount.getEndTime() < currentUpdateIntervalStartTime && stepCountSinceLastSave + lastStoredStepCount.getStepCount() > 0) ||
                lastStoredStepCount.getWalkingMode() != null && walkingMode != null && walkingMode.getId() != lastStoredStepCount.getWalkingMode().getId()) {
            // create new step count if none is stored or last one was saved before the current update interval and there are new staps to save
            // (the time interval of the previous step count may only be extended if it had 0 steps and there are 0 steps to add)
            StepCount stepCount = new StepCount();
            stepCount.setWalkingMode(walkingMode);
            stepCount.setStepCount(stepCountSinceLastSave);
            stepCount.setEndTime(currentTime);
            stepCountDbHelper.addStepCount(stepCount);
            Log.i(LOG_CLASS, "Creating new step count");
        } else {
            lastStoredStepCount.setStepCount(lastStoredStepCount.getStepCount() + stepCountSinceLastSave);
            long oldEndTime = lastStoredStepCount.getEndTime();
            lastStoredStepCount.setEndTime(currentTime);
            stepCountDbHelper.updateStepCount(lastStoredStepCount, oldEndTime);
            Log.i(LOG_CLASS, "Updating last stored step count - not creating a new one");
        }
        // reset step count
        myBinder.resetStepCount();
        Log.i(LOG_CLASS, "Stored " + stepCountSinceLastSave + " steps");

        // broadcast the event
        Intent localIntent = new Intent(BROADCAST_ACTION_STEPS_SAVED);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

        return true;
    }

    /**
     * Stores the given step count to database and sends broadcast-event
     *
     * @param stepCount The step count to save
     * @param context       The application context
     * @return true if save was successful else false.
     */
    public static boolean storeStepCount(StepCount stepCount, Context context) {
        new StepCountDbHelper(context).addStepCount(stepCount);

        // broadcast the event
        Intent localIntent = new Intent(BROADCAST_ACTION_STEPS_INSERTED);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
        return true;
    }

    /**
     * Updates the given step count in database based on end timestamp
     *
     * @param stepCount The step count to save
     * @param context       The application context
     * @return true if save was successful else false.
     */
    public static boolean updateStepCount(StepCount stepCount, Context context) {
        new StepCountDbHelper(context).updateStepCount(stepCount);
        // broadcast the event
        Intent localIntent = new Intent(BROADCAST_ACTION_STEPS_UPDATED);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
        return true;
    }

    /**
     * Get the number of steps for the given day
     *
     * @param calendar The day (in user's timezone)
     * @param context  The application context
     * @return the number of steps
     */
    public static int getStepCountForDay(Calendar calendar, Context context) {
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long start_time = calendar.getTimeInMillis();
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        long end_time = calendar.getTimeInMillis();
        return StepCountPersistenceHelper.getStepCountForInterval(start_time, end_time, context);
    }

    /**
     * Get the step count models for the given day
     *
     * @param calendar The day (in user's timezone)
     * @param context  The application context
     * @return the step count models for the day
     */
    public static List<StepCount> getStepCountsForDay(Calendar calendar, Context context) {
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long start_time = calendar.getTimeInMillis();
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        long end_time = calendar.getTimeInMillis();
        return StepCountPersistenceHelper.getStepCountsForInterval(start_time, end_time, context);
    }

    /**
     * @deprecated Use {@link StepCountDbHelper#getStepCountsForInterval(long, long)} instead.
     *
     * Returns the stepCount models for the steps walked in the given interval.
     *
     * @param start_time The start time in users timezone
     * @param end_time   The end time in users timezone
     * @param context    The application context
     * @return The @{see StepCount}-Models between start and end time
     */
    public static List<StepCount> getStepCountsForInterval(long start_time, long end_time, Context context) {
        if (context == null) {
            Log.e(LOG_CLASS, "Cannot get step count - context is null");
            return new ArrayList<>();
        }
        return new StepCountDbHelper(context).getStepCountsForInterval(start_time, end_time);
    }

    /**
     * Returns the stepCount models for the steps walked.
     *
     * @param context    The application context
     * @return The @{see StepCount}-Models
     */
    public static List<StepCount> getStepCountsForever(Context context) {
        if (context == null) {
            Log.e(LOG_CLASS, "Cannot get step count - context is null");
            return new ArrayList<>();
        }
        Cursor c = getDB(context).query(StepCountDbHelper.StepCountEntry.TABLE_NAME,
                new String[]{StepCountDbHelper.StepCountEntry.KEY_STEP_COUNT, StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP, StepCountDbHelper.StepCountEntry.KEY_WALKING_MODE},
                "", new String[]{}, null, null, null);
        List<StepCount> steps = new ArrayList<>();
        long start = 0;
        int sum = 0;
        while (c.moveToNext()) {
            StepCount s = new StepCount();
            s.setStartTime(start);
            s.setEndTime(c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP)));
            s.setStepCount(c.getInt(c.getColumnIndex(StepCountDbHelper.StepCountEntry.KEY_STEP_COUNT)));
            //Log.w("ASDF", "Getting walking mode " + c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.COLUMN_NAME_WALKING_MODE)));
            s.setWalkingMode(WalkingModePersistenceHelper.getItem(c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.KEY_WALKING_MODE)), context));
            steps.add(s);
            start = s.getEndTime();
            sum += s.getStepCount();
        }
        c.close();
        return steps;
    }

    /**
     * Returns the number of steps walked in the given time interval
     *
     * @param start_time The start time
     * @param end_time   The end time
     * @param context    The application context
     * @return Number of steps between start and end time
     */
    public static int getStepCountForInterval(long start_time, long end_time, Context context) {
        int steps = 0;
        for (StepCount s : getStepCountsForInterval(start_time, end_time, context)) {
            steps += s.getStepCount();
        }
        return steps;
    }

    /**
     * Returns the date of first entry in database
     * @param context Application context
     * @return Date of first entry or default today
     */
    public static Date getDateOfFirstEntry(Context context){
        StepCount s = new StepCountDbHelper(context).getFirstStepCount();
        Date date = Calendar.getInstance().getTime(); // fallback is today
        if(s != null){
            date.setTime(s.getEndTime());
        }
        return date;
    }

    /**
     * Returns the last step count entry for given day
     * @param day the day
     * @param context application context
     * @return the last step count entry for given day
     */
    public static StepCount getLastStepCountEntryForDay(Calendar day, Context context){
        List<StepCount> stepCounts = getStepCountsForDay(day, context);
        if(stepCounts.size() == 0){
            return null;
        }else{
            return stepCounts.get(stepCounts.size() - 1);
        }
    }

    /**
     * Returns the writable database
     *
     * @param context The application context
     * @return a writable database object
     */
    protected static SQLiteDatabase getDB(Context context) {
        if (StepCountPersistenceHelper.db == null) {
            StepCountDbHelper dbHelper = new StepCountDbHelper(context);
            StepCountPersistenceHelper.db = dbHelper.getWritableDatabase();
        }
        return StepCountPersistenceHelper.db;
    }
}
