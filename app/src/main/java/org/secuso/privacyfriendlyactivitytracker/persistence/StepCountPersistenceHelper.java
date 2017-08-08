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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
        // Get the steps since last save
        int stepCountSinceLastSave = myBinder.stepsSinceLastSave();

        // Get current walking mode
        long walkingModeId = -1;
        if (walkingMode != null) {
            walkingModeId = walkingMode.getId();
        }
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StepCountDbHelper.StepCountEntry.KEY_STEP_COUNT, stepCountSinceLastSave);
        values.put(StepCountDbHelper.StepCountEntry.KEY_WALKING_MODE, walkingModeId);
        values.put(StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP, Calendar.getInstance().getTime().getTime());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = getDB(context).insert(
                StepCountDbHelper.StepCountEntry.TABLE_NAME,
                null,
                values);
        // reset step count
        myBinder.resetStepCount();
        Log.i(LOG_CLASS, "Stored " + stepCountSinceLastSave + " steps (id=" + newRowId + ") for mode id=" + walkingModeId);

        // broadcast the event
        Intent localIntent = new Intent(BROADCAST_ACTION_STEPS_SAVED);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

        return true;
    }

    /**
     * Stores the given step count to database
     *
     * @param stepCount The step count to save
     * @param context       The application context
     * @return true if save was successful else false.
     */
    public static boolean storeStepCount(StepCount stepCount, Context context) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StepCountDbHelper.StepCountEntry.KEY_STEP_COUNT, stepCount.getStepCount());
        values.put(StepCountDbHelper.StepCountEntry.KEY_WALKING_MODE, (stepCount.getWalkingMode() != null) ? stepCount.getWalkingMode().getId() : 0);
        values.put(StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP, stepCount.getEndTime());

        // Insert the new row, returning the primary key value of the new row
        getDB(context).insert(
                StepCountDbHelper.StepCountEntry.TABLE_NAME,
                null,
                values);
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
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StepCountDbHelper.StepCountEntry.KEY_STEP_COUNT, stepCount.getStepCount());
        values.put(StepCountDbHelper.StepCountEntry.KEY_WALKING_MODE, (stepCount.getWalkingMode() != null) ? stepCount.getWalkingMode().getId() : 1);
        values.put(StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP, stepCount.getEndTime());

        // Insert the new row, returning the primary key value of the new row
        getDB(context).update(
                StepCountDbHelper.StepCountEntry.TABLE_NAME,
                values,
                StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP + " = ?",
                new String[]{String.valueOf(stepCount.getEndTime())}
        );
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
        Cursor c = getDB(context).query(StepCountDbHelper.StepCountEntry.TABLE_NAME,
                new String[]{StepCountDbHelper.StepCountEntry.KEY_STEP_COUNT, StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP, StepCountDbHelper.StepCountEntry.KEY_WALKING_MODE},
                StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP + " >= ? AND " + StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP + " <= ?", new String[]{String.valueOf(start_time),
                        String.valueOf(end_time)}, null, null, StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP + " ASC");
        List<StepCount> steps = new ArrayList<>();
        long start = start_time;
        while (c.moveToNext()) {
            StepCount s = new StepCount();
            s.setStartTime(start);
            s.setEndTime(c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP)));
            s.setStepCount(c.getInt(c.getColumnIndex(StepCountDbHelper.StepCountEntry.KEY_STEP_COUNT)));
            s.setWalkingMode(WalkingModePersistenceHelper.getItem(c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.KEY_WALKING_MODE)), context));
            steps.add(s);
            start = s.getEndTime();
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
        Cursor c = getDB(context).query(StepCountDbHelper.StepCountEntry.TABLE_NAME,
                new String[]{StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP}, /* columns */
                null,
                null,
                null,
                null,
                StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP + " ASC", /* orderBy */
                "1" /* limit */);
        Date date = Calendar.getInstance().getTime(); // fallback is today
        while(c.moveToNext()){
            date.setTime(c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.KEY_TIMESTAMP)));
        }
        c.close();
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
