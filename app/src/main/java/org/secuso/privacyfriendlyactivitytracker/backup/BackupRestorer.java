package org.secuso.privacyfriendlyactivitytracker.backup;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.JsonReader;

import androidx.annotation.NonNull;

import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountDbHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.TrainingDbHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModeDbHelper;
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil;
import org.secuso.privacyfriendlybackup.api.backup.FileUtil;
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BackupRestorer implements IBackupRestorer {

    private void readDatabase(@NonNull JsonReader reader, @NonNull Context context, String dbName) throws IOException {
        reader.beginObject();

        String n1 = reader.nextName();
        if (!n1.equals("version")) {
            throw new RuntimeException("Unknown value " + n1);
        }
        int version = reader.nextInt();

        String n2 = reader.nextName();
        if (!n2.equals("content")) {
            throw new RuntimeException("Unknown value " + n2);
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("restoreDatabase"), null);
        db.beginTransaction();
        db.setVersion(version);

        DatabaseUtil.readDatabaseContent(reader, db);

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        reader.endObject();

        // copy file to correct location
        File databaseFile = context.getDatabasePath("restoreDatabase");
        File oldDBFile = context.getDatabasePath(dbName);
        FileUtil.copyFile(databaseFile, context.getDatabasePath(dbName));
        databaseFile.delete();
    }

    private void readPreferences(@NonNull JsonReader reader, @NonNull Context context) throws IOException {
        reader.beginObject();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        while (reader.hasNext()) {
            String name = reader.nextName();

            switch (name) {
                case "step_counter_enabled":
                case "use_wake_lock":
                case "use_wake_lock_during_training":
                case "show_velocity":
                case "notification_permanent_show_steps":
                case "notification_permanent_show_distance":
                case "notification_permanent_show_calories":
                case "notification_motivation_alert_enabled":

                    pref.edit().putBoolean(name, reader.nextBoolean()).apply();
                    break;
                case "unit_of_length":
                case "unit_of_energy":
                case "accelerometer_threshold":
                case "accelerometer_steps_threshold":
                case "daily_step_goal":
                case "weight":
                case "gender":
                case "notification_motivation_alert_criterion":
                case "notification_motivation_alert_time":
                    pref.edit().putString(name, reader.nextString()).apply();
                    break;
                default:
                    throw new RuntimeException("Unknown preference " + name);
            }
        }

        reader.endObject();
    }

    @Override
    public boolean restoreBackup(@NonNull Context context, @NonNull InputStream restoreData) {
        try {
            InputStreamReader isReader = new InputStreamReader(restoreData);
            JsonReader reader = new JsonReader(isReader);

            // START
            reader.beginObject();

            while (reader.hasNext()) {
                String type = reader.nextName();

                switch (type) {
                    case "database_stepCount":
                        readDatabase(reader, context, StepCountDbHelper.DATABASE_NAME);
                        break;
                    case "database_trainings":
                        readDatabase(reader, context, TrainingDbHelper.DATABASE_NAME);
                        break;
                    case "database_walkingMode":
                        readDatabase(reader, context, WalkingModeDbHelper.DATABASE_NAME);
                        break;
                    case "preferences":
                        readPreferences(reader, context);
                        break;
                    default:
                        throw new RuntimeException("Can not parse type " + type);
                }

            }

            reader.endObject();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}