package org.secuso.privacyfriendlystepcounter;

import android.content.pm.PackageManager;

import org.secuso.privacyfriendlystepcounter.services.AccelerometerStepDetectorService;
import org.secuso.privacyfriendlystepcounter.services.HardwareStepDetectorService;
import org.secuso.privacyfriendlystepcounter.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlystepcounter.utils.AndroidVersionHelper;

/**
 * Factory class
 *
 * @author Tobias Neidig
 * @version 20160610
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
        if(pm != null && AndroidVersionHelper.supportsStepDetector(pm)) {
            return HardwareStepDetectorService.class;
        }else{
            return AccelerometerStepDetectorService.class;
        }
    }
}
