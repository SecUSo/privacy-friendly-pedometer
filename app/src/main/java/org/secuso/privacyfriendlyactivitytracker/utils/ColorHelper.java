package org.secuso.privacyfriendlyactivitytracker.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Color helper util
 */
public class ColorHelper {

    /**
     * Material color list
     * http://stackoverflow.com/a/29549014
     */
    private static List<Integer> materialColors = Arrays.asList(
            0xffe57373,
            0xfff06292,
            0xffba68c8,
            0xff9575cd,
            0xff7986cb,
            0xff64b5f6,
            0xff4fc3f7,
            0xff4dd0e1,
            0xff4db6ac,
            0xff81c784,
            0xffaed581,
            0xffff8a65,
            0xffd4e157,
            0xffffd54f,
            0xffffb74d,
            0xffa1887f,
            0xff90a4ae
    );

    /**
     * Returns a 'unique' pseudo random material color for given object
     *
     * @param key the key for which the color should be generated
     * @return the color related to the key
     * @link http://stackoverflow.com/a/29549014
     */
    public static int getMaterialColor(Object key) {
        return materialColors.get(Math.abs(key.hashCode()) % materialColors.size());
    }
}
