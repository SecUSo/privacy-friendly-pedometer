package org.secuso.privacyfriendlystepcounter.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.secuso.privacyfriendlystepcounter.utils.StepDetectionServiceHelper;

/**
 * Receives the broadcast if the own package is replaced and
 * starts the step detection and it's required services if
 * step detection is enabled.
 */
public class OnPackageReplacedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        StepDetectionServiceHelper.startAllIfEnabled(context);
    }
}
