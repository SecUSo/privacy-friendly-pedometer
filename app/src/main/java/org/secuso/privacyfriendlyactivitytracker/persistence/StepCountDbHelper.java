package org.secuso.privacyfriendlyactivitytracker.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Database helper class for storing steps
 * The database stores for each entry a timestamp and the number of steps since last entry.
 *
 * @author Tobias Neidig
 * @version 20160630
 */
public class StepCountDbHelper  extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "StepCount.db";

    public static final String TABLE_NAME = "stepcount";

    public static final String KEY_ID = "_id";
    public static final String KEY_STEP_COUNT = "stepcount";
    public static final String KEY_WALKING_MODE = "walking_mode";
    public static final String KEY_TIMESTAMP = "timestamp";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_STEP_COUNT + INTEGER_TYPE + COMMA_SEP +
                    KEY_WALKING_MODE + INTEGER_TYPE + COMMA_SEP +
                    KEY_TIMESTAMP + INTEGER_TYPE +
            " )";

    public StepCountDbHelper(Context context) {
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
     *             Please use {@link StepCountDbHelper} instead.
     */
    public static abstract class StepCountEntry implements BaseColumns {
        public static final String TABLE_NAME = StepCountDbHelper.TABLE_NAME;
        public static final String KEY_STEP_COUNT = StepCountDbHelper.KEY_STEP_COUNT;
        public static final String KEY_WALKING_MODE = StepCountDbHelper.KEY_WALKING_MODE;
        public static final String KEY_TIMESTAMP = KEY_WALKING_MODE;
    }
}
