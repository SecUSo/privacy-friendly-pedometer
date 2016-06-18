package org.secuso.privacyfriendlystepcounter.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Stores the current step count in database.
 *
 * @author Tobias Neidig
 * @version 20160618
 */
public class StepCountPersistenceReceiver extends WakefulBroadcastReceiver {
    private static final String LOG_CLASS = StepCountPersistenceReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO
        Log.i(LOG_CLASS, "onReceive");
    }
}
