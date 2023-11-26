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
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.fragments.DailyReportFragment;
import org.secuso.privacyfriendlyactivitytracker.fragments.MainFragment;
import org.secuso.privacyfriendlyactivitytracker.fragments.MonthlyReportFragment;
import org.secuso.privacyfriendlyactivitytracker.fragments.WeeklyReportFragment;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;

/**
 * Main view incl. navigation drawer and fragments
 *
 * @author Tobias Neidig, Karola Marky
 * @version 20161214
 */

public class MainActivity extends BaseActivity implements DailyReportFragment.OnFragmentInteractionListener, WeeklyReportFragment.OnFragmentInteractionListener, MonthlyReportFragment.OnFragmentInteractionListener {

    private static final int ACTIVITY_RECOGNITION = 1;
    private boolean askedAlready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        // Load first view
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new MainFragment(), "MainFragment");
        fragmentTransaction.commit();

    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.menu_home;
    }

    public static void requestPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE_HEALTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{ Manifest.permission.FOREGROUND_SERVICE_HEALTH, Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.BODY_SENSORS}, ACTIVITY_RECOGNITION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{ Manifest.permission.ACTIVITY_RECOGNITION}, ACTIVITY_RECOGNITION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // if permission is not granted ask again
        if(requestCode == ACTIVITY_RECOGNITION) {
            if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if(askedAlready) {
                    builder.setMessage(R.string.dialog_permission_activity_recognition_2);
                    builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
                } else {
                    builder.setMessage(R.string.dialog_permission_activity_recognition);
                    builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        requestPermission(this);
                        dialogInterface.dismiss();
                    });
                }
                // Start step detection if enabled and not yet started
                StepDetectionServiceHelper.startAllIfEnabled(this);
                //Log.i(LOG_TAG, "MainActivity initialized");

                builder.create().show();
            }
        }
    }
}
