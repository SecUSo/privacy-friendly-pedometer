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
        values.put(StepCountDbHelper.StepCountEntry.COLUMN_NAME_STEP_COUNT, stepCountSinceLastSave);
        values.put(StepCountDbHelper.StepCountEntry.COLUMN_NAME_WALKING_MODE, walkingModeId);
        values.put(StepCountDbHelper.StepCountEntry.COLUMN_NAME_TIMESTAMP, new Date().getTime());

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
     * Get the number of steps for the given day
     *
     * @param calendar The day
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
     * @param calendar The day
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
     * @param start_time The start time
     * @param end_time   The end time
     * @param context    The application context
     * @return The @{see StepCount}-Models between start and end time
     */
    public static List<StepCount> getStepCountsForInterval(long start_time, long end_time, Context context) {
        if (context == null) {
            Log.e(LOG_CLASS, "Cannot get step count - context is null");
            return new ArrayList<>();
        }
        Cursor c = getDB(context).query(StepCountDbHelper.StepCountEntry.TABLE_NAME,
                new String[]{StepCountDbHelper.StepCountEntry.COLUMN_NAME_STEP_COUNT, StepCountDbHelper.StepCountEntry.COLUMN_NAME_TIMESTAMP, StepCountDbHelper.StepCountEntry.COLUMN_NAME_WALKING_MODE},
                StepCountDbHelper.StepCountEntry.COLUMN_NAME_TIMESTAMP + " >= ? AND " + StepCountDbHelper.StepCountEntry.COLUMN_NAME_TIMESTAMP + " <= ?", new String[]{String.valueOf(start_time),
                        String.valueOf(end_time)}, null, null, null);
        List<StepCount> steps = new ArrayList<>();
        long start = start_time;
        int sum = 0;
        while (c.moveToNext()) {
            StepCount s = new StepCount();
            s.setStartTime(start);
            s.setEndTime(c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.COLUMN_NAME_TIMESTAMP)));
            s.setStepCount(c.getInt(c.getColumnIndex(StepCountDbHelper.StepCountEntry.COLUMN_NAME_STEP_COUNT)));
            //Log.w("ASDF", "Getting walking mode " + c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.COLUMN_NAME_WALKING_MODE)));
            s.setWalkingMode(WalkingModePersistenceHelper.getItem(c.getLong(c.getColumnIndex(StepCountDbHelper.StepCountEntry.COLUMN_NAME_WALKING_MODE)), context));
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
