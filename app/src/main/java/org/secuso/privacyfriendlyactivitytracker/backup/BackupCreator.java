package org.secuso.privacyfriendlyactivitytracker.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.JsonWriter;
import android.util.Log;

import androidx.annotation.NonNull;

import org.secuso.privacyfriendlyactivitytracker.PFAPedometerApplication;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountDbHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.TrainingDbHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModeDbHelper;
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil;
import org.secuso.privacyfriendlybackup.api.backup.PreferenceUtil;
import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;


public class BackupCreator implements IBackupCreator {

    @Override
    public void writeBackup(@NonNull Context context, @NonNull OutputStream outputStream) {
        // lock application, so no changes can be made as long as this backup is created
        // depending on the size of the application - this could take a bit
        ((PFAPedometerApplication) context.getApplicationContext()).lock();

        Log.d("PFA BackupCreator", "createBackup() started");
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
        JsonWriter writer = new JsonWriter(outputStreamWriter);
        writer.setIndent("");

        try {
            writer.beginObject();
            SQLiteDatabase dataBase1 = SQLiteDatabase.openDatabase(context.getDatabasePath(StepCountDbHelper.DATABASE_NAME).getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Log.d("PFA BackupCreator", "Writing StepCount database");
            writer.name("database_stepCount");
            DatabaseUtil.writeDatabase(writer, dataBase1);
            dataBase1.close();

            SQLiteDatabase dataBase2 = SQLiteDatabase.openDatabase(context.getDatabasePath(TrainingDbHelper.DATABASE_NAME).getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Log.d("PFA BackupCreator", "Writing Training database");
            writer.name("database_trainings");
            DatabaseUtil.writeDatabase(writer, dataBase2);
            dataBase2.close();

            SQLiteDatabase dataBase3 = SQLiteDatabase.openDatabase(context.getDatabasePath(WalkingModeDbHelper.DATABASE_NAME).getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Log.d("PFA BackupCreator", "Writing WalkingMode database");
            writer.name("database_walkingMode");
            DatabaseUtil.writeDatabase(writer, dataBase3);
            dataBase3.close();


            Log.d("PFA BackupCreator", "Writing preferences");
            writer.name("preferences");
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            PreferenceUtil.writePreferences(writer, pref);

            writer.endObject();
            writer.close();
        } catch (Exception e) {
            Log.e("PFA BackupCreator", "Error occurred", e);
            e.printStackTrace();
        }

        Log.d("PFA BackupCreator", "Backup created successfully");

        ((PFAPedometerApplication) context.getApplicationContext()).release();
    }
}