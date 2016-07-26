package org.secuso.privacyfriendlystepcounter.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
     * Gets all not deleted walking modes from database
     * @param context The application context
     * @return a list of walking modes
     */
    public static List<WalkingMode> getAllItems(Context context) {
        Cursor c = getCursor(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_DELETED + " = ?", new String[]{String.valueOf(false)}, context);
        List<WalkingMode> walkingModes = new ArrayList<>();
        while(c.moveToNext()){
            walkingModes.add(WalkingMode.from(c));
        }
        c.close();
        return walkingModes;
    }

    /**
     * Gets the specific walking mode
     * @param id the id of the walking mode
     * @param context The application context
     * @return the requested walking mode or null
     */
    public static WalkingMode getItem(long id, Context context) {
        Cursor c = getCursor(WalkingModeDbHelper.WalkingModeEntry._ID + " = ?", new String[]{String.valueOf(id)}, context);
        if(c.getCount() == 0){
            return null;
        }else{
            c.moveToFirst();
            return WalkingMode.from(c);
        }
    }

    /**
     * Stores the given walking mode to database.
     * If id is set, the walking mode will be updated else it will be created
     * @param item the walking mode to store
     * @param context The application context
     * @return the saved walking mode (with correct id)
     */
    public static WalkingMode save(WalkingMode item, Context context) {
        if(item == null){
            return null;
        }
        if(item.getId() <= 0){
            long insertedId = insert(item, context);
            if(insertedId == -1){
                return null;
            }else{
                item.setId(insertedId);
                return item;
            }
        }else{
            int affectedRows = update(item, context);
            if(affectedRows == 0){
                return null;
            }else{
                return item;
            }
        }
    }

    /**
     * Deletes the given walking mode from database
     * @param item the item to delete
     * @param context The application context
     * @return true if deletion was successful else false
     */
    public static boolean delete(WalkingMode item, Context context) {
        WalkingModeDbHelper dbHelper = new WalkingModeDbHelper(context);
        if(item == null || item.getId() <= 0){
            return false;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = WalkingModeDbHelper.WalkingModeEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};
        return (0 != db.delete(WalkingModeDbHelper.WalkingModeEntry.TABLE_NAME, selection, selectionArgs));
    }

    /**
     * Soft deletes the item.
     * The item will be present via @{see #getItem()} but not in @{see #getAllItems()}.
     *
     * @param item The item to soft delete
     * @param context The application context
     * @return true if soft deletion was successful else false
     */
    public static boolean softDelete(WalkingMode item, Context context) {
        if(item == null || item.getId() <= 0){
            return false;
        }
        item.setIsDeleted(true);
        return save(item, context).isDeleted();
    }

    /**
     * Sets the given walking mode to the active one
     * @param mode the walking mode to activate
     * @param context The application context
     * @return true if active mode changed to given one
     */
    public static boolean setActiveMode(WalkingMode mode, Context context){
        WalkingMode currentActiveMode = getActiveMode(context);
        if(currentActiveMode != null){
            currentActiveMode.setIsActive(false);
            save(currentActiveMode, context);
        }
        mode.setIsActive(true);
        return save(mode, context).isActive();
    }

    /**
     * Gets the currently active walking mode
     * @param context The application context
     * @return The walking mode with active-flag set
     */
    public static WalkingMode getActiveMode(Context context){
        Cursor c = getCursor(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_ACTIVE + " = ?", new String[]{String.valueOf(true)}, context);
        if(c.getCount() == 0){
            return null;
        }else{
            c.moveToFirst();
            return WalkingMode.from(c);
        }
    }

    /**
     * Inserts the given walking mode as new entry.
     * @param item The walking mode which should be stored
     * @param context The application context
     * @return the inserted id
     */
    protected static long insert(WalkingMode item, Context context){
        WalkingModeDbHelper dbHelper = new WalkingModeDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = item.toContentValues();
        return db.insert(
                WalkingModeDbHelper.WalkingModeEntry.TABLE_NAME,
                null,
                values);
    }

    /**
     * Updates the given walking mode in database
     * @param item The walking mode to update
     * @param context The application context
     * @return the number of rows affected
     */
    protected static int update(WalkingMode item, Context context){
        WalkingModeDbHelper dbHelper = new WalkingModeDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = item.toContentValues();

        String selection = WalkingModeDbHelper.WalkingModeEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};

        return db.update(
                WalkingModeDbHelper.WalkingModeEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    /**
     * Gets the database cursor for given selection arguments.
     * @param selection The selection query
     * @param selectionArgs The arguments for selection query
     * @param context The application context
     * @return the database cursor
     */
    protected static Cursor getCursor(String selection, String[] selectionArgs, Context context){
        WalkingModeDbHelper dbHelper = new WalkingModeDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

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

        return db.query(
                WalkingModeDbHelper.WalkingModeEntry.TABLE_NAME,  // The table to query
                projection,                                            // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }
}
