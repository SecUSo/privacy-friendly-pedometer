package org.secuso.privacyfriendlyactivitytracker.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;

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
    private static final String LOG_CLASS = WalkingModeDbHelper.class.getName();

    private Context context;

    public WalkingModeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        // Insert default walking modes
        WalkingModePersistenceHelper.setSQLiteDatabase(db);
        String[] walkingModesNames = context.getResources().getStringArray(R.array.pref_default_walking_mode_names);
        String[] walkingModesStepLengthStrings = context.getResources().getStringArray(R.array.pref_default_walking_mode_step_lenghts);
        if (walkingModesStepLengthStrings.length != walkingModesNames.length) {
            Log.e(LOG_CLASS, "Number of default walking mode step lengths and names have to be the same.");
            return;
        }
        if (walkingModesNames.length == 0) {
            Log.e(LOG_CLASS, "There are no default walking modes.");
        }
        WalkingModePersistenceHelper.setSQLiteDatabase(db);
        for (int i = 0; i < walkingModesStepLengthStrings.length; i++) {
            String stepLengthString = walkingModesStepLengthStrings[i];
            double stepLength = Double.valueOf(stepLengthString);
            String name = walkingModesNames[i];
            WalkingMode walkingMode = new WalkingMode();
            walkingMode.setStepLength(stepLength);
            walkingMode.setName(name);
            walkingMode.setIsActive(i == 0);
            WalkingModePersistenceHelper.save(walkingMode, context);
            Log.i(LOG_CLASS, "Created default walking mode " + name);
        }
        WalkingModePersistenceHelper.setSQLiteDatabase(null);
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
