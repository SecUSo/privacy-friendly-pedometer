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

import org.secuso.privacyfriendlyactivitytracker.models.Training;

import java.util.List;

/**
 * Helper to save and restore training sessions from database.
 *
 * @author Tobias Neidig
 * @version 20170810
 */

public class TrainingPersistenceHelper {
    public static final String LOG_CLASS = TrainingPersistenceHelper.class.getName();

    /**
     * @deprecated Use {@link TrainingDbHelper#getAllTrainings()} instead.
     *
     * Gets all training sessions from database
     *
     * @param context The application context
     * @return a list of training sessions
     */
    public static List<Training> getAllItems(Context context) {
        return new TrainingDbHelper(context).getAllTrainings();
    }

    /**
     * @deprecated Use {@link TrainingDbHelper#getTraining(int)} instead.
     *
     * Gets the specific training session
     *
     * @param id      the id of the training session
     * @param context The application context
     * @return the requested training session or null
     */
    public static Training getItem(long id, Context context) {
        return new TrainingDbHelper(context).getTraining((int) id);
    }

    /**
     * @deprecated Use {@link TrainingDbHelper#getActiveTraining()} instead.
     *
     * Gets the active training session
     *
     * @param context The application context
     * @return the requested training session or null
     */
    public static Training getActiveItem(Context context) {
        return new TrainingDbHelper(context).getActiveTraining();
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
     * @deprecated Use {@link TrainingDbHelper#deleteTraining(Training)} instead.
     *
     * Deletes the given training session from database
     *
     * @param item    the item to delete
     * @param context The application context
     * @return true if deletion was successful else false
     */
    public static boolean delete(Training item, Context context) {
        new TrainingDbHelper(context).deleteTraining(item);
        return true;
    }

    /**
     * @deprecated Use {@link TrainingDbHelper#addTraining(Training)} instead.
     *
     * Inserts the given training session as new entry.
     *
     * @param item    The training session which should be stored
     * @param context The application context
     * @return the inserted id
     */
    protected static long insert(Training item, Context context) {
        return new TrainingDbHelper(context).addTraining(item);
    }

    /**
     * @deprecated Use {@link TrainingDbHelper#updateTraining(Training)} instead.
     *
     * Updates the given training session in database
     *
     * @param item    The training session to update
     * @param context The application context
     * @return the number of rows affected
     */
    protected static int update(Training item, Context context) {
        return new TrainingDbHelper(context).updateTraining(item);
    }
}
