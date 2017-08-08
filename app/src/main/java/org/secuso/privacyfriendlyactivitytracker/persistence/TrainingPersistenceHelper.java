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

import org.secuso.privacyfriendlyactivitytracker.models.Training;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to save and restore training sessions from database.
 *
 * @author Tobias Neidig
 * @version 20160727
 */
public class TrainingPersistenceHelper {
    public static final String LOG_CLASS = TrainingPersistenceHelper.class.getName();

    private static SQLiteDatabase db = null;

    /**
     * Gets all training sessions from database
     *
     * @param context The application context
     * @return a list of training sessions
     */
    public static List<Training> getAllItems(Context context) {
        Cursor c = getCursor(null, null, TrainingDbHelper.TrainingSessionEntry.KEY_START + " DESC", context);
        List<Training> trainingSessions = new ArrayList<>();
        if (c == null) {
            return trainingSessions;
        }
        while (c.moveToNext()) {
            trainingSessions.add(Training.from(c));
        }
        c.close();
        return trainingSessions;
    }

    /**
     * Gets the specific training session
     *
     * @param id      the id of the training session
     * @param context The application context
     * @return the requested training session or null
     */
    public static Training getItem(long id, Context context) {
        Cursor c = getCursor(TrainingDbHelper.TrainingSessionEntry._ID + " = ?", new String[]{String.valueOf(id)}, context);
        Training trainingSession = null;
        if (c == null) {
            return null;
        }
        if (c.getCount() == 0) {
            trainingSession = null;
        } else {
            c.moveToFirst();
            trainingSession = Training.from(c);
        }

        c.close();
        return trainingSession;
    }

    /**
     * Gets the active training session
     *
     * @param context The application context
     * @return the requested training session or null
     */
    public static Training getActiveItem(Context context) {
        Cursor c = getCursor(TrainingDbHelper.TrainingSessionEntry.KEY_END + " = ?", new String[]{"0"}, context);
        Training trainingSession = null;
        if (c == null) {
            return null;
        }
        if (c.getCount() == 0) {
            trainingSession = null;
        } else {
            c.moveToFirst();
            trainingSession = Training.from(c);
        }

        c.close();
        return trainingSession;
    }

    /**
     * Stores the given training session to database.
     * If id is set, the training session will be updated else it will be created
     *
     * @param item    the training session to store
     * @param context The application context
     * @return the saved training session (with correct id)
     */
    public static Training save(Training item, Context context) {
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
     * Deletes the given training session from database
     *
     * @param item    the item to delete
     * @param context The application context
     * @return true if deletion was successful else false
     */
    public static boolean delete(Training item, Context context) {
        if (item == null || item.getId() <= 0) {
            return false;
        }
        String selection = TrainingDbHelper.TrainingSessionEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};
        return (0 != getDB(context).delete(TrainingDbHelper.TrainingSessionEntry.TABLE_NAME, selection, selectionArgs));
    }

    /**
     * Inserts the given training session as new entry.
     *
     * @param item    The training session which should be stored
     * @param context The application context
     * @return the inserted id
     */
    protected static long insert(Training item, Context context) {
        ContentValues values = item.toContentValues();
        long insertedId = getDB(context).insert(
                TrainingDbHelper.TrainingSessionEntry.TABLE_NAME,
                null,
                values);
        return insertedId;
    }

    /**
     * Updates the given training session in database
     *
     * @param item    The training session to update
     * @param context The application context
     * @return the number of rows affected
     */
    protected static int update(Training item, Context context) {
        ContentValues values = item.toContentValues();

        String selection = TrainingDbHelper.TrainingSessionEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};

        int rowsAffected = getDB(context).update(
                TrainingDbHelper.TrainingSessionEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        return rowsAffected;
    }

    protected static Cursor getCursor(String selection, String[] selectionArgs, Context context){
        return getCursor(selection, selectionArgs, TrainingDbHelper.TrainingSessionEntry._ID + " ASC", context);
    }

    /**
     * Gets the database cursor for given selection arguments.
     *
     * @param selection     The selection query
     * @param selectionArgs The arguments for selection query
     * @param context       The application context
     * @return the database cursor
     */
    protected static Cursor getCursor(String selection, String[] selectionArgs, String sortOrder, Context context) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                TrainingDbHelper.TrainingSessionEntry._ID,
                TrainingDbHelper.TrainingSessionEntry.KEY_NAME,
                TrainingDbHelper.TrainingSessionEntry.KEY_DESCRIPTION,
                TrainingDbHelper.TrainingSessionEntry.KEY_STEPS,
                TrainingDbHelper.TrainingSessionEntry.KEY_DISTANCE,
                TrainingDbHelper.TrainingSessionEntry.KEY_CALORIES,
                TrainingDbHelper.TrainingSessionEntry.KEY_START,
                TrainingDbHelper.TrainingSessionEntry.KEY_END,
                TrainingDbHelper.TrainingSessionEntry.KEY_FEELING
        };

        return getDB(context).query(
                TrainingDbHelper.TrainingSessionEntry.TABLE_NAME,  // The table to query
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
        if (TrainingPersistenceHelper.db == null) {
            TrainingDbHelper dbHelper = new TrainingDbHelper(context);
            TrainingPersistenceHelper.db = dbHelper.getWritableDatabase();
        }
        return TrainingPersistenceHelper.db;
    }
}
