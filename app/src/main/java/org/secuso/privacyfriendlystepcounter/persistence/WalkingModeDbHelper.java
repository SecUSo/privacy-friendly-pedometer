package org.secuso.privacyfriendlystepcounter.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Database helper class for storing walking modes
 *
 * @author Tobias Neidig
 * @version 20160724
 */
public class WalkingModeDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WalkingModes.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String STRING_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WalkingModeEntry.TABLE_NAME + " (" +
                    WalkingModeEntry._ID + " INTEGER PRIMARY KEY," +
                    WalkingModeEntry.COLUMN_NAME_NAME + STRING_TYPE + COMMA_SEP +
                    WalkingModeEntry.COLUMN_NAME_STEP_SIZE + REAL_TYPE + COMMA_SEP +
                    WalkingModeEntry.COLUMN_NAME_STEP_FREQUENCY + REAL_TYPE + COMMA_SEP +
                    WalkingModeEntry.COLUMN_NAME_IS_ACTIVE + INTEGER_TYPE + COMMA_SEP +
                    WalkingModeEntry.COLUMN_NAME_IS_DELETED + INTEGER_TYPE +
            " )";

    public WalkingModeDbHelper(Context context) {
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
    public static abstract class WalkingModeEntry implements BaseColumns {
        public static final String TABLE_NAME = "walkingmodes";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_STEP_SIZE = "stepsize";
        public static final String COLUMN_NAME_STEP_FREQUENCY = "stepfrequency";
        public static final String COLUMN_NAME_IS_ACTIVE = "is_active";
        public static final String COLUMN_NAME_IS_DELETED = "deleted";
    }
}
