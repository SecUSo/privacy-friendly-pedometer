package org.secuso.privacyfriendlystepcounter.utils;

import android.content.pm.PackageManager;
import android.os.Build;

/**
 *
 * @author Tobias Neidig
 * @version 20160610
 */
public class AndroidVersionHelper {
    /**
     * Decides weather the current soft- and hardware setup allows to use hardware step detection
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
}
