package org.secuso.privacyfriendlystepcounter.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.secuso.privacyfriendlystepcounter.models.WalkingMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to save and restore walking modes from database.
 *
 * @author Tobias Neidig
 * @version 20160726
 */
public class WalkingModePersistenceHelper {

    /**
     * Broadcast action identifier for messages broadcast when walking mode changed
     */
    public static final String BROADCAST_ACTION_WALKING_MODE_CHANGED = "org.secuso.privacyfriendlystepcounter.WALKING_MODE_CHANGED";
    public static final String BROADCAST_EXTRA_OLD_WALKING_MODE = "org.secuso.privacyfriendlystepcounter.EXTRA_OLD_WALKING_MODE";
    public static final String BROADCAST_EXTRA_NEW_WALKING_MODE = "org.secuso.privacyfriendlystepcounter.EXTRA_NEW_WALKING_MODE";
    public static final String LOG_CLASS = WalkingModePersistenceHelper.class.getName();

    private static SQLiteDatabase db = null;

    /**
     * Gets all not deleted walking modes from database
     *
     * @param context The application context
     * @return a list of walking modes
     */
    public static List<WalkingMode> getAllItems(Context context) {
        Cursor c = getCursor(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_DELETED + " = ?", new String[]{String.valueOf(false)}, context);
        List<WalkingMode> walkingModes = new ArrayList<>();
        if (c == null) {
            return walkingModes;
        }
        while (c.moveToNext()) {
            walkingModes.add(WalkingMode.from(c));
        }
        c.close();
        return walkingModes;
    }

    /**
     * Gets the specific walking mode
     *
     * @param id      the id of the walking mode
     * @param context The application context
     * @return the requested walking mode or null
     */
    public static WalkingMode getItem(long id, Context context) {
        Cursor c = getCursor(WalkingModeDbHelper.WalkingModeEntry._ID + " = ?", new String[]{String.valueOf(id)}, context);
        WalkingMode walkingMode = null;
        if (c == null) {
            return null;
        }
        if (c.getCount() == 0) {
            walkingMode = null;
        } else {
            c.moveToFirst();
            walkingMode = WalkingMode.from(c);
        }

        c.close();
        return walkingMode;
    }

    /**
     * Stores the given walking mode to database.
     * If id is set, the walking mode will be updated else it will be created
     *
     * @param item    the walking mode to store
     * @param context The application context
     * @return the saved walking mode (with correct id)
     */
    public static WalkingMode save(WalkingMode item, Context context) {
        if (item == null) {
            return null;
        }
        if (item.getId() <= 0) {
            long insertedId = insert(item, context);
            if (insertedId == -1) {
                return null;
            } else {
                item.setId(insertedId);
                return item;
            }
        } else {
            int affectedRows = update(item, context);
            if (affectedRows == 0) {
                return null;
            } else {
                return item;
            }
        }
    }

    /**
     * Deletes the given walking mode from database
     *
     * @param item    the item to delete
     * @param context The application context
     * @return true if deletion was successful else false
     */
    public static boolean delete(WalkingMode item, Context context) {
        if (item == null || item.getId() <= 0) {
            return false;
        }
        String selection = WalkingModeDbHelper.WalkingModeEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};
        return (0 != getDB(context).delete(WalkingModeDbHelper.WalkingModeEntry.TABLE_NAME, selection, selectionArgs));
    }

    /**
     * Soft deletes the item.
     * The item will be present via @{see #getItem()} but not in @{see #getAllItems()}.
     *
     * @param item    The item to soft delete
     * @param context The application context
     * @return true if soft deletion was successful else false
     */
    public static boolean softDelete(WalkingMode item, Context context) {
        if (item == null || item.getId() <= 0) {
            return false;
        }
        item.setIsDeleted(true);
        return save(item, context).isDeleted();
    }

    /**
     * Sets the given walking mode to the active one
     *
     * @param mode    the walking mode to activate
     * @param context The application context
     * @return true if active mode changed to given one
     */
    public static boolean setActiveMode(WalkingMode mode, Context context) {
        Log.i(LOG_CLASS, "Changing active mode to " + mode.getName());
        if (mode.isActive()) {
            // Already active
            Log.i(LOG_CLASS, "Skipping active mode change");
            return true;
        }
        WalkingMode currentActiveMode = getActiveMode(context);

        if (currentActiveMode != null) {
            currentActiveMode.setIsActive(false);
            save(currentActiveMode, context);
        }
        mode.setIsActive(true);
        boolean success = save(mode, context).isActive();
        // broadcast the event
        Intent localIntent = new Intent(BROADCAST_ACTION_WALKING_MODE_CHANGED);
        localIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        if (currentActiveMode != null) {
            localIntent.putExtra(BROADCAST_EXTRA_OLD_WALKING_MODE, currentActiveMode.getId());
        }
        localIntent.putExtra(BROADCAST_EXTRA_NEW_WALKING_MODE, mode.getId());
        // Broadcasts the Intent to receivers in this app.
        context.sendBroadcast(localIntent);
        return success;
    }

    /**
     * Gets the currently active walking mode
     *
     * @param context The application context
     * @return The walking mode with active-flag set
     */
    public static WalkingMode getActiveMode(Context context) {
        Cursor c = getCursor(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_ACTIVE + " = ?", new String[]{String.valueOf(true)}, context);
        WalkingMode walkingMode;
        if (c.getCount() == 0) {
            walkingMode = null;
        } else {
            c.moveToFirst();
            walkingMode = WalkingMode.from(c);
        }
        c.close();
        return walkingMode;
    }

    /**
     * Inserts the given walking mode as new entry.
     *
     * @param item    The walking mode which should be stored
     * @param context The application context
     * @return the inserted id
     */
    protected static long insert(WalkingMode item, Context context) {
        ContentValues values = item.toContentValues();
        long insertedId = getDB(context).insert(
                WalkingModeDbHelper.WalkingModeEntry.TABLE_NAME,
                null,
                values);
        //db.close();
        //dbHelper.close();
        return insertedId;
    }

    /**
     * Updates the given walking mode in database
     *
     * @param item    The walking mode to update
     * @param context The application context
     * @return the number of rows affected
     */
    protected static int update(WalkingMode item, Context context) {
        ContentValues values = item.toContentValues();

        String selection = WalkingModeDbHelper.WalkingModeEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};

        int rowsAffected = getDB(context).update(
                WalkingModeDbHelper.WalkingModeEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        return rowsAffected;
    }

    /**
     * Gets the database cursor for given selection arguments.
     *
     * @param selection     The selection query
     * @param selectionArgs The arguments for selection query
     * @param context       The application context
     * @return the database cursor
     */
    protected static Cursor getCursor(String selection, String[] selectionArgs, Context context) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                WalkingModeDbHelper.WalkingModeEntry._ID,
                WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_NAME,
                WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_STEP_SIZE,
                WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_STEP_FREQUENCY,
                WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_ACTIVE,
                WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_DELETED
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                WalkingModeDbHelper.WalkingModeEntry._ID + " ASC";

        return getDB(context).query(
                WalkingModeDbHelper.WalkingModeEntry.TABLE_NAME,  // The table to query
                projection,                                            // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }


    /**
     * Returns the writable database
     *
     * @param context The application context
     * @return a writable database object
     */
    protected static SQLiteDatabase getDB(Context context) {
        if (WalkingModePersistenceHelper.db == null) {
            WalkingModeDbHelper dbHelper = new WalkingModeDbHelper(context);
            WalkingModePersistenceHelper.db = dbHelper.getWritableDatabase();
        }
        return WalkingModePersistenceHelper.db;
    }

    public static void setSQLiteDatabase(SQLiteDatabase db){
        WalkingModePersistenceHelper.db = db;
    }
}
