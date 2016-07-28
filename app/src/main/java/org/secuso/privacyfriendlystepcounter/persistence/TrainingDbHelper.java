package org.secuso.privacyfriendlystepcounter.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Database helper class for storing walking modes
 *
 * @author Tobias Neidig
 * @version 20160727
 */
public class TrainingDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TrainingSessions.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String STRING_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TrainingSessionEntry.TABLE_NAME + " (" +
                    TrainingSessionEntry._ID + " INTEGER PRIMARY KEY," +
                    TrainingSessionEntry.COLUMN_NAME_NAME + STRING_TYPE + COMMA_SEP +
                    TrainingSessionEntry.COLUMN_NAME_DESCRIPTION + STRING_TYPE + COMMA_SEP +
                    TrainingSessionEntry.COLUMN_NAME_STEPS + REAL_TYPE + COMMA_SEP +
                    TrainingSessionEntry.COLUMN_NAME_DISTANCE + REAL_TYPE + COMMA_SEP +
                    TrainingSessionEntry.COLUMN_NAME_CALORIES + REAL_TYPE + COMMA_SEP +
                    TrainingSessionEntry.COLUMN_NAME_START + REAL_TYPE + COMMA_SEP +
                    TrainingSessionEntry.COLUMN_NAME_END + REAL_TYPE + COMMA_SEP +
                    TrainingSessionEntry.COLUMN_NAME_FEELING + REAL_TYPE +
            " )";

    public TrainingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Fill when upgrading DB
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /* Inner class that defines the table contents */
    public static abstract class TrainingSessionEntry implements BaseColumns {
        public static final String TABLE_NAME = "walkingmodes";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_STEPS = "steps";
        public static final String COLUMN_NAME_CALORIES = "calories";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_START = "start";
        public static final String COLUMN_NAME_END = "end";
        public static final String COLUMN_NAME_FEELING = "feeling";
    }
}
