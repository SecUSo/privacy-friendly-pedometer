package org.secuso.privacyfriendlystepcounter.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.secuso.privacyfriendlystepcounter.utils.StepDetectionServiceHelper;

/**
 * Receives the on boot complete broadcast and starts
 * the step detection and it's required services if
 * step detection is enabled.
 */
public class OnBootCompletedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        StepDetectionServiceHelper.startAllIfEnabled(context);
    }
}
