package org.secuso.privacyfriendlystepcounter.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import org.secuso.privacyfriendlystepcounter.services.AbstractStepDetectorService;

import java.util.Date;

/**
 * Helper to save and restore step count from database.
 *
 * @author Tobias Neidig
 * @version 20160720
 */
public class StepCountPersistenceHelper {
    public static String LOG_CLASS = StepCountPersistenceHelper.class.getName();

    /**
     * Stores the current step count to database and resets the step counter in step-detector
     * @param serviceBinder The binder for stepCountService
     * @param context The application context
     * @return true if save was successful else false.
     */
    public static boolean storeStepCounts(IBinder serviceBinder, Context context){
        if(serviceBinder == null){
            Log.e(LOG_CLASS, "Cannot store step count because service binder is null.");
            return false;
        }
        AbstractStepDetectorService.StepDetectorBinder myBinder = (AbstractStepDetectorService.StepDetectorBinder) serviceBinder;
        // Get the steps since last save
        int stepCountSinceLastSave = myBinder.stepsSinceLastSave();

        StepCountDbHelper mDbHelper = new StepCountDbHelper(context);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StepCountDbHelper.StepCountEntry.COLUMN_NAME_STEP_COUNT, stepCountSinceLastSave);
        values.put(StepCountDbHelper.StepCountEntry.COLUMN_NAME_WALKING_MODE, 1); // TODO set walking mode
        values.put(StepCountDbHelper.StepCountEntry.COLUMN_NAME_TIMESTAMP, new Date().getTime());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                StepCountDbHelper.StepCountEntry.TABLE_NAME,
                null,
                values);
        // reset step count
        myBinder.resetStepCount();
        Log.i(LOG_CLASS, "Stored " + stepCountSinceLastSave + " steps (id=" + newRowId + ")");
        return true;
    }
}
