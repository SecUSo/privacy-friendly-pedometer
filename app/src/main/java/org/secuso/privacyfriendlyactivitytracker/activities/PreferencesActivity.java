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
package org.secuso.privacyfriendlyactivitytracker.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.receivers.StepCountPersistenceReceiver;
import org.secuso.privacyfriendlyactivitytracker.utils.AndroidVersionHelper;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class PreferencesActivity extends AppCompatPreferenceActivity {
    private static Map<String, String> additionalSummaryTexts;
    static int REQUEST_EXTERNAL_STORAGE = 2;
    static int REQUEST_LOCATION = 1;
    static int REQUEST_ACTIVITY = 3;

    private static final int CREATE_FILE = 4;

    private GeneralPreferenceFragment generalPreferenceFragment;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                String additionalSummaryText = additionalSummaryTexts.get(preference.getKey());
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? (((additionalSummaryText != null) ? additionalSummaryText : "") + listPreference.getEntries()[index])
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    public PreferencesActivity() {
        super();
        additionalSummaryTexts = new HashMap<>();
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static void bindPreferenceSummaryToLongValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getLong(preference.getKey(), 0));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
                //|| HelpFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    GeneralPreferenceFragment.writeStepsToCSVOutputStream(generalPreferenceFragment, out,
                            StepCountPersistenceHelper.getStepCountsForever(getApplicationContext()), generalPreferenceFragment.getDocumentUriDisplayName(uri));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportCSVafterPermissionGranted();
            } else {
                Toast.makeText(this, getString(R.string.export_csv_permission_needed), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_LOCATION) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        // location permission was not granted - disable velocity setting.
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(getString(R.string.pref_show_velocity), false);
                        editor.apply();
                    }
                }
            }
        }
        if (requestCode == REQUEST_ACTIVITY){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (generalPreferenceFragment !=null){
                    generalPreferenceFragment.saveStepsAndRestartService();
                    generalPreferenceFragment.checkHardwareStepUse(true);
                }
            } else {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.pref_use_step_hardware), false);
                editor.apply();
                if (generalPreferenceFragment !=null){
                    generalPreferenceFragment.checkHardwareStepUse(false);
                }
            }
        }

    }
    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private Preference exportDataPreference;
        private Preference lengthUnitPreference;
        private Preference energyUnitPreference;
        private Preference dailyStepGoalPreference;
        private Preference weightPreference;
        private Preference genderPreference;
        private Preference accelThresholdPreference;
        private Preference useStepHardwarePreference;
        private Preference stepCounterEnabledPreference;
        private static String LOG_TAG = GeneralPreferenceFragment.class.getName();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            ((PreferencesActivity) getActivity()).generalPreferenceFragment = this;

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            additionalSummaryTexts.put(getString(R.string.pref_accelerometer_threshold), getString(R.string.pref_summary_accelerometer_threshold));

            lengthUnitPreference = findPreference(getString(R.string.pref_unit_of_length));
            stepCounterEnabledPreference = findPreference(getString(R.string.pref_step_counter_enabled));
            energyUnitPreference = findPreference(getString(R.string.pref_unit_of_energy));
            dailyStepGoalPreference = findPreference(getString(R.string.pref_daily_step_goal));
            weightPreference = findPreference(getString(R.string.pref_weight));
            genderPreference = findPreference(getString(R.string.pref_gender));
            accelThresholdPreference = findPreference(getString(R.string.pref_accelerometer_threshold));
            exportDataPreference = findPreference(getString(R.string.pref_export_data));
            useStepHardwarePreference = findPreference(getString(R.string.pref_use_step_hardware));

            bindPreferenceSummaryToValue(lengthUnitPreference);
            bindPreferenceSummaryToValue(energyUnitPreference);
            bindPreferenceSummaryToValue(dailyStepGoalPreference);
            bindPreferenceSummaryToValue(weightPreference);
            bindPreferenceSummaryToValue(genderPreference);
            bindPreferenceSummaryToValue(accelThresholdPreference);

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            sharedPref.registerOnSharedPreferenceChangeListener(this);

            //correctly set background counting enabled or not
            findPreference(getString(R.string.pref_hw_background_counter_frequency)).setEnabled(sharedPref.getBoolean(getString(R.string.pref_use_step_hardware), true));
            findPreference(getString(R.string.pref_which_step_hardware)).setEnabled(sharedPref.getBoolean(getString(R.string.pref_use_step_hardware), true));


            stepCounterEnabledPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    StepCountPersistenceReceiver.unregisterSaveListener();
                    return true;
                }
            });

            useStepHardwarePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SwitchPreference pref = (SwitchPreference) preference;
                    Boolean enable = pref.isChecked();

                    saveStepsAndRestartService();

                    if(enable) {

                        if (AndroidVersionHelper.supportsStepDetector(getActivity().getApplicationContext().getPackageManager())) {

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                if(verifyActivityPermissions(getActivity())) {
                                    checkHardwareStepUse(true);
                                }
                            }
                            return true;

                        } else {
                            Toast.makeText(getActivity(), R.string.pref_use_step_hardware_not_available, Toast.LENGTH_SHORT).show();
                            checkHardwareStepUse(false);
                            return false;
                        }
                    }
                    checkHardwareStepUse(false);
                    return false;
                }
            });

            exportDataPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Check if you have the permission
                    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT || verifyStoragePermissions(getActivity()) )  {
                        generateCSVToExport();
                    }
                    return true;
                }
            });
        }

        private void checkHardwareStepUse(Boolean checked) {
            final SwitchPreference hardwarePreference = (SwitchPreference) findPreference(getString(R.string.pref_use_step_hardware));
            if (hardwarePreference!=null) hardwarePreference.setChecked(checked);
        }

        private void saveStepsAndRestartService() {
            final Context context = getActivity().getApplicationContext();
/*
            StepCountPersistenceReceiver.registerSaveListener(new StepCountPersistenceReceiver.ISaveListener() {
                @Override
                public void onSaveDone() {
                    //Log.d("save", "save done");
                    StepCountPersistenceReceiver.unregisterSaveListener();

                    if(context != null) {
                        StepDetectionServiceHelper.startAllIfEnabled(true, context);
                    }
                }
            });

            StepDetectionServiceHelper.cancelPersistenceService(true, context);
            StepDetectionServiceHelper.stopAllIfNotRequired(false, context);*/
            StepDetectionServiceHelper.restartStepDetection(context);
        }

        /**
         * Checks if the app has permission to write to device storage
         * <p>
         * If the app does not has permission then the user will be prompted to grant permissions
         *
         * @param activity
         */
        private boolean verifyStoragePermissions(Activity activity) {
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            // Check if we have write permission
            int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
                return false;
            }
            return true;
        }


        /**
         * Checks if the app has permission to access hardware step counter
         * <p>
         * If the app does not has permission then the user will be prompted to grant permission
         *
         * @param activity
         */
        private boolean verifyActivityPermissions(Activity activity) {
            String[] PERMISSION_ACTIVITY = {
                    Manifest.permission.ACTIVITY_RECOGNITION
            };

            // Check if we have permission
            int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSION_ACTIVITY,
                        REQUEST_ACTIVITY
                );
                return false;
            }
            return true;
        }

         private static void writeStepsToCSVOutputStream(GeneralPreferenceFragment generalPreferenceFragment, OutputStream outputStream, List<StepCount> steps, String fileName) throws IOException {
            //Add the header
            outputStream.write((generalPreferenceFragment.getString(R.string.export_csv_header) + "\r\n").getBytes());
            //Populate the file
            String dateFormat = "yyyy-MM-dd HH:mm:ss";
            for (StepCount s : steps) {
                String startDate = s.getStartTime() == 0 ? generalPreferenceFragment.getString(R.string.export_csv_begin) : DateFormat.format(dateFormat, new Date(s.getStartTime())).toString();
                String endDate = DateFormat.format(dateFormat, new Date(s.getEndTime())).toString();
                outputStream.write((startDate + ";" + endDate + ";" + s.getStepCount() + ";" + s.getWalkingMode().getName() + "\r\n").getBytes());
            }
            outputStream.close();
            Toast.makeText(generalPreferenceFragment.getActivity(), generalPreferenceFragment.getString(R.string.export_csv_success) + " " + fileName, Toast.LENGTH_SHORT).show();
        }

        public String getDocumentUriDisplayName(Uri uri) {
            Cursor cursor = getActivity().getContentResolver()
                    .query(uri, null, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    // Note it's called "Display Name". This is
                    // provider-specific, and might not necessarily be the file name.
                    @SuppressLint("Range") String displayName = cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    return displayName;
                }
            } finally {
                cursor.close();
            }
            return null;
        }

        public void generateCSVToExport() {
            final Context context = getActivity().getApplicationContext();
            SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String csvFileName = "exportStepCount_" + fileDateFormat.format(System.currentTimeMillis());

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/csv");
                    intent.putExtra(Intent.EXTRA_TITLE, csvFileName);
                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker when your app creates the document.
                    // intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
                    getActivity().startActivityForResult(intent, CREATE_FILE);
                } else {
                    List<StepCount> steps = StepCountPersistenceHelper.getStepCountsForever(getActivity());
                    File csvFile = new File(Environment.getExternalStoragePublicDirectory(context.getResources().getString(R.string.app_name)), csvFileName);
                    csvFile.getParentFile().mkdirs();
                    OutputStream out = new FileOutputStream(csvFile);
                    writeStepsToCSVOutputStream(this, out, steps, csvFileName + ".csv");
                }
            } catch (IOException e) {
                Toast.makeText(getActivity(), getString(R.string.export_csv_error), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                this.getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onDetach() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            sharedPref.unregisterOnSharedPreferenceChangeListener(this);
            super.onDetach();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Context context = getActivity().getApplicationContext();
            Log.d("preference check","pref changed: "+key);


            // Detect changes on preferences and update our internal variable
            if (key.equals(getString(R.string.pref_step_counter_enabled))) {
                boolean isEnabled = sharedPreferences.getBoolean(getString(R.string.pref_step_counter_enabled), true);
                if (isEnabled) {
                    StepDetectionServiceHelper.startAllIfEnabled(context);
                } else {
                    StepDetectionServiceHelper.stopAllIfNotRequired(context);
                }
            }

            // check for location permission
            if (key.equals(getString(R.string.pref_show_velocity))) {
                boolean isEnabled = sharedPreferences.getBoolean(getString(R.string.pref_show_velocity), false);
                if (isEnabled) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                    }
                } else {
                    final SwitchPreference velocityPreference = (SwitchPreference) findPreference(getString(R.string.pref_show_velocity));
                    if (velocityPreference!=null) velocityPreference.setChecked(false);
                }
            }

            if (key.equals(getString(R.string.pref_use_step_hardware))) {
                boolean isEnabled = sharedPreferences.getBoolean(getString(R.string.pref_use_step_hardware), true);
                findPreference(getString(R.string.pref_hw_background_counter_frequency)).setEnabled(isEnabled);
                findPreference(getString(R.string.pref_which_step_hardware)).setEnabled(isEnabled);
            }

            if (key.equals(getString(R.string.pref_hw_background_counter_frequency)) && sharedPreferences.getString(getString(R.string.pref_which_step_hardware), "0").equals("0")) {
                saveStepsAndRestartService();
            }
            if (key.equals(getString(R.string.pref_which_step_hardware))) {
                saveStepsAndRestartService();
            }
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class NotificationPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToLongValue(findPreference(getString(R.string.pref_notification_motivation_alert_time)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_notification_motivation_alert_criterion)));

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            sharedPref.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                this.getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onDetach() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            sharedPref.unregisterOnSharedPreferenceChangeListener(this);
            super.onDetach();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (isDetached()) {
                return;
            }
            // Detect changes on preferences and update our internal variable
            if (key.equals(getString(R.string.pref_notification_motivation_alert_enabled)) || key.equals(getString(R.string.pref_notification_motivation_alert_time))) {
                boolean isEnabled = sharedPreferences.getBoolean(getString(R.string.pref_notification_motivation_alert_enabled), true);
                if (isEnabled) {
                    StepDetectionServiceHelper.startAllIfEnabled(getActivity().getApplicationContext());
                } else {
                    StepDetectionServiceHelper.stopAllIfNotRequired(getActivity().getApplicationContext());
                }
            }
        }
    }


    void exportCSVafterPermissionGranted() {
        generalPreferenceFragment.generateCSVToExport();
    }

    /**
     * This fragment shows the help content.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public static class HelpFragment extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.help);
//            setHasOptionsMenu(false);
//        }
//
//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            int id = item.getItemId();
//            if (id == android.R.id.home) {
//                this.getActivity().onBackPressed();
//                return true;
//            }
//            return super.onOptionsItemSelected(item);
//        }
//    }
}
