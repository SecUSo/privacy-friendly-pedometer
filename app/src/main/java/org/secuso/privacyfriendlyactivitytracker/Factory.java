package org.secuso.privacyfriendlyactivitytracker;

import android.content.pm.PackageManager;

import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.services.AccelerometerStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.services.HardwareStepCounterService;
import org.secuso.privacyfriendlyactivitytracker.services.HardwareStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.utils.AndroidVersionHelper;

/**
 * Factory class
 *
 * @author Tobias Neidig
 * @version 20161126
 */
public class Factory {

    /**
     * Returns the class of the step detector service which should be used
     *
     * The used step detector service depends on different soft- and hardware preferences.
     * @param pm An instance of the android PackageManager
     * @return The class of step detector
     */
    public static Class<? extends AbstractStepDetectorService> getStepDetectorServiceClass(PackageManager pm){
        if(pm != null) {
            if (AndroidVersionHelper.supportsStepCounter(pm))
                return HardwareStepCounterService.class;
            if (AndroidVersionHelper.supportsStepDetector(pm))
                return HardwareStepDetectorService.class;
        }
        return AccelerometerStepDetectorService.class;
    }
}
