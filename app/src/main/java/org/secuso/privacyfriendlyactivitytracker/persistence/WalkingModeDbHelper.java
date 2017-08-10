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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Database helper class for storing walking modes
 *
 * @author Tobias Neidig
 * @version 20170810
 */
public class WalkingModeDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "WalkingModes.db";

    public static final String TABLE_NAME = "walkingmodes";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_STEP_SIZE = "stepsize";
    public static final String KEY_STEP_FREQUENCY = "stepfrequency";
    public static final String KEY_IS_ACTIVE = "is_active";
    public static final String KEY_IS_DELETED = "deleted";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String STRING_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WalkingModeEntry.TABLE_NAME + " (" +
                    WalkingModeEntry._ID + " INTEGER PRIMARY KEY," +
                    WalkingModeEntry.KEY_NAME + STRING_TYPE + COMMA_SEP +
                    WalkingModeEntry.KEY_STEP_SIZE + REAL_TYPE + COMMA_SEP +
                    WalkingModeEntry.KEY_STEP_FREQUENCY + REAL_TYPE + COMMA_SEP +
                    WalkingModeEntry.KEY_IS_ACTIVE + INTEGER_TYPE + COMMA_SEP +
                    WalkingModeEntry.KEY_IS_DELETED + INTEGER_TYPE +
                    " )";
    private static final String LOG_CLASS = WalkingModeDbHelper.class.getName();

    private Context context;

    public WalkingModeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        // Insert default walking modes
        String[] walkingModesNames = context.getResources().getStringArray(R.array.pref_default_walking_mode_names);
        String[] walkingModesStepLengthStrings = context.getResources().getStringArray(R.array.pref_default_walking_mode_step_lenghts);
        if (walkingModesStepLengthStrings.length != walkingModesNames.length) {
            Log.e(LOG_CLASS, "Number of default walking mode step lengths and names have to be the same.");
            return;
        }
        if (walkingModesNames.length == 0) {
            Log.e(LOG_CLASS, "There are no default walking modes.");
        }
        for (int i = 0; i < walkingModesStepLengthStrings.length; i++) {
            String stepLengthString = walkingModesStepLengthStrings[i];
            double stepLength = Double.valueOf(stepLengthString);
            String name = walkingModesNames[i];
            WalkingMode walkingMode = new WalkingMode();
            walkingMode.setStepLength(stepLength);
            walkingMode.setName(name);
            walkingMode.setIsActive(i == 0);
            this.addWalkingMode(walkingMode);
            Log.i(LOG_CLASS, "Created default walking mode " + name);
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Fill when upgrading DB
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Inserts the given walking mode as new entry.
     *
     * @param walkingMode    The walking mode which should be stored
     * @return the inserted id
     */
    public long addWalkingMode(WalkingMode walkingMode){
        ContentValues values = walkingMode.toContentValues();
        return getWritableDatabase().insert(
                TABLE_NAME,
                null,
                values);
    }

    /**
     * Inserts the given walking mode as new entry and sets the id to id of given object.
     *
     * @param walkingMode    The walking mode which should be stored
     */
    public void addWalkingModeWithID(WalkingMode walkingMode){
        ContentValues values = walkingMode.toContentValues();
        values.put(KEY_ID, walkingMode.getId());
        getWritableDatabase().insert(
                TABLE_NAME,
                null,
                values);
    }

    /**
     * Gets the specific walking mode
     *
     * @param id      the id of the walking mode
     * @return the requested walking mode or null
     */
    public WalkingMode getWalkingMode(int id){
        Cursor c = getCursor(KEY_ID + " = ?", new String[]{String.valueOf(id)});
        WalkingMode walkingMode;
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
     * Gets the currently active walking mode
     *
     * @return The walking mode with active-flag set
     */
    public WalkingMode getActiveWalkingMode() {
        Cursor c = getCursor(KEY_IS_ACTIVE + " = ?", new String[]{String.valueOf(true)});
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
     * Gets all not deleted walking modes from database
     *
     * @return a list of walking modes
     */
    public List<WalkingMode> getAllWalkingModes(){
        return this.getAllWalkingModes(false);
    }

    /**
     * Gets all walking modes from database
     *
     * @param withDeleted Whether soft-deleted walking mode should be returned, too
     * @return a list of walking modes
     */
    public List<WalkingMode> getAllWalkingModes(boolean withDeleted){
        Cursor c = getCursor(KEY_IS_DELETED + " = ?", new String[]{String.valueOf(withDeleted)});
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
     * Updates the given walking mode in database
     *
     * @param walkingMode    The walking mode to update
     * @return the number of rows affected
     */
    public int updateWalkingMode(WalkingMode walkingMode){
        ContentValues values = walkingMode.toContentValues();

        String selection = KEY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(walkingMode.getId())};

        return getWritableDatabase().update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    /**
     * Deletes the given walking mode from database
     *
     * @param walkingMode the item to delete
     */
    public void deleteWalkingMode(WalkingMode walkingMode){
        if (walkingMode == null || walkingMode.getId() <= 0) {
            return;
        }
        String selection = KEY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(walkingMode.getId())};
        this.getWritableDatabase().delete(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Deletes all walking modes from database
     */
    public void deleteAllWalkingModes(){
        this.getWritableDatabase().execSQL("delete from " + TABLE_NAME);
    }

    /**
     * Gets the database cursor for given selection arguments.
     *
     * @param selection     The selection query
     * @param selectionArgs The arguments for selection query
     * @return the database cursor
     */
    private Cursor getCursor(String selection, String[] selectionArgs) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                KEY_ID,
                KEY_NAME,
                KEY_STEP_SIZE,
                KEY_STEP_FREQUENCY,
                KEY_IS_ACTIVE,
                KEY_IS_DELETED
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                KEY_ID + " ASC";

        return this.getWritableDatabase().query(
                TABLE_NAME,                         // The table to query
                projection,                         // The columns to return
                selection,                          // The columns for the WHERE clause
                selectionArgs,                      // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                sortOrder                           // The sort order
        );
    }

    /**
     * @deprecated This class is deprecated due to structural updates to match pfa sample app.
     *             Please use {@link WalkingModeDbHelper} instead.
     */
    public static abstract class WalkingModeEntry implements BaseColumns {
        public static final String TABLE_NAME = WalkingModeDbHelper.TABLE_NAME;
        public static final String KEY_NAME = WalkingModeDbHelper.KEY_NAME;
        public static final String KEY_STEP_SIZE = WalkingModeDbHelper.KEY_STEP_SIZE;
        public static final String KEY_STEP_FREQUENCY = WalkingModeDbHelper.KEY_STEP_FREQUENCY;
        public static final String KEY_IS_ACTIVE = WalkingModeDbHelper.KEY_IS_ACTIVE;
        public static final String KEY_IS_DELETED = WalkingModeDbHelper.KEY_IS_DELETED;
    }
}
