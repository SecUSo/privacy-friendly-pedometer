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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        oldDBFile.delete();
        FileUtil.copyFile(databaseFile, context.getDatabasePath(dbName));
        databaseFile.delete();
    }

    private void readPreferences(@NonNull JsonReader reader, @NonNull Context context) throws IOException {
        reader.beginObject();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        while (reader.hasNext()) {
            String name = reader.nextName();

            switch (name) {
                case "org.secuso.privacyfriendlyactivitytracker.pref.step_counter_enabled":
                case "org.secuso.privacyfriendlyactivitytracker.pref.use_wake_lock":
                case "org.secuso.privacyfriendlyactivitytracker.pref.use_wake_lock_during_training":
                case "org.secuso.privacyfriendlyactivitytracker.pref.show_velocity":
                case "org.secuso.privacyfriendlyactivitytracker.pref.permanent_notification_show_steps":
                case "org.secuso.privacyfriendlyactivitytracker.pref.permanent_notification_show_distance":
                case "org.secuso.privacyfriendlyactivitytracker.pref.permanent_notification_show_calories":
                case "org.secuso.privacyfriendlyactivitytracker.pref.motivation_alert_enabled":

                    pref.edit().putBoolean(name, reader.nextBoolean()).apply();
                    break;

                case "org.secuso.privacyfriendlyactivitytracker.pref.unit_of_length":
                case "org.secuso.privacyfriendlyactivitytracker.pref.unit_of_energy":
                case "org.secuso.privacyfriendlyactivitytracker.pref.accelerometer_threshold":
                case "org.secuso.privacyfriendlyactivitytracker.pref.accelerometer_step_threshold":
                case "org.secuso.privacyfriendlyactivitytracker.pref.daily_step_goal":
                case "org.secuso.privacyfriendlyactivitytracker.pref.weight":
                case "org.secuso.privacyfriendlyactivitytracker.pref.gender":
                case "org.secuso.privacyfriendlyactivitytracker.pref.motivation_alert_criterion":

                    pref.edit().putString(name,reader.nextString() ).apply();
                    break;

                case "org.secuso.privacyfriendlyactivitytracker.pref.motivation_alert_time":
                case "org.secuso.privacyfriendlyactivitytracker.pref.distance_measurement_start_timestamp":

                    pref.edit().putLong(name, reader.nextLong()).apply();
                    break;

                case "org.secuso.privacyfriendlyactivitytracker.pref.motivation_alert_texts":

                    reader.beginArray();
                    List<String> alertTexts = new ArrayList<>();
                    while(reader.hasNext()) {
                        alertTexts.add(reader.nextString());
                    }
                    reader.endArray();
                    pref.edit().putStringSet("org.secuso.privacyfriendlyactivitytracker.pref.motivation_alert_texts", new HashSet<>(alertTexts)).apply();
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

            StepCountDbHelper.invalidateReference();
            TrainingDbHelper.invalidateReference();
            WalkingModeDbHelper.invalidateReference();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}