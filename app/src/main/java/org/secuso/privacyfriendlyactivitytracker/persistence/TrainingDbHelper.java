package org.secuso.privacyfriendlyactivitytracker.persistence;

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

    public static final String TABLE_NAME = "walkingmodes";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_STEPS = "steps";
    public static final String KEY_CALORIES = "calories";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_START = "start";
    public static final String KEY_END = "end";
    public static final String KEY_FEELING = "feeling";

    private static final String STRING_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_NAME + STRING_TYPE + COMMA_SEP +
                    KEY_DESCRIPTION + STRING_TYPE + COMMA_SEP +
                    KEY_STEPS + REAL_TYPE + COMMA_SEP +
                    KEY_DISTANCE + REAL_TYPE + COMMA_SEP +
                    KEY_CALORIES + REAL_TYPE + COMMA_SEP +
                    KEY_START + REAL_TYPE + COMMA_SEP +
                    KEY_END + REAL_TYPE + COMMA_SEP +
                    KEY_FEELING + REAL_TYPE +
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

    /**
     * @deprecated This class is deprecated due to structural updates to match pfa sample app.
     *             Please use {@link TrainingDbHelper} instead.
     */
    public static abstract class TrainingSessionEntry implements BaseColumns {
        public static final String TABLE_NAME = TrainingDbHelper.TABLE_NAME;
        public static final String KEY_NAME = TrainingDbHelper.KEY_NAME;
        public static final String KEY_DESCRIPTION = TrainingDbHelper.KEY_DESCRIPTION;
        public static final String KEY_STEPS = TrainingDbHelper.KEY_STEPS;
        public static final String KEY_CALORIES = TrainingDbHelper.KEY_CALORIES;
        public static final String KEY_DISTANCE = TrainingDbHelper.KEY_DISTANCE;
        public static final String KEY_START = TrainingDbHelper.KEY_START;
        public static final String KEY_END = TrainingDbHelper.KEY_END;
        public static final String KEY_FEELING = TrainingDbHelper.KEY_FEELING;
    }
}
