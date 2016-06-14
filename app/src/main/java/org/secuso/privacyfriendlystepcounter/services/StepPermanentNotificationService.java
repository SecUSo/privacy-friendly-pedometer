package org.secuso.privacyfriendlystepcounter.services;

import android.app.IntentService;
import android.content.Intent;

/**
 *
 * @author Tobias Neidig
 * @version 20160611
 */
public class StepPermanentNotificationService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public StepPermanentNotificationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO
    }
}
