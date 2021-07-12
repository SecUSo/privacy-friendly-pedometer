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
package org.secuso.privacyfriendlyactivitytracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;

import org.secuso.privacyfriendlyactivitytracker.R;

/**
 *
 * @author Tobias Neidig
 * @version 20160610
 */
public class AndroidVersionHelper {
    /**
     * Decides whether the current soft- and hardware setup allows to use hardware step detection
     * @param pm An instance of the android PackageManager
     * @return true if hardware step detection can be used otherwise false
     */
    public static boolean supportsStepDetector(PackageManager pm) {
        // (Hardware) step detection was introduced in KitKat (4.4 / API 19)
        // https://developer.android.com/about/versions/android-4.4.html
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                // In addition to the system version
                // the hardware step detection is not supported on every device
                // let's check the device's ability.
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    /**
     * Decides whether the hardware step counter should be used. In this case the step counter-service
     * will not show any notification and update the step count not in real time. This helps to save
     * energy and increases the accuracy - but is only available on some devices.
     * @param context An instance of the originating Context
     * @return true if hardware step counter should and can be used.
     */
    public static boolean isHardwareStepCounterEnabled(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return supportsStepDetector(context.getPackageManager()) && sharedPref.getBoolean(context.getString(R.string.pref_use_step_hardware), false);
    }
}
