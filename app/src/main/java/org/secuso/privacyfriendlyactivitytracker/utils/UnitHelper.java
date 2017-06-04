package org.secuso.privacyfriendlyactivitytracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.secuso.privacyfriendlyactivitytracker.R;

/**
 * Helper util to convert units
 *
 * @author Tobias Neidig
 * @version 20160729
 */
public class UnitHelper {
    /**
     * The unit factor for conversion from and to kilometers
     */
    public static int USER_UNIT_FACTOR = 0;
    /**
     * The short length unit descriptor (e.g. km)
     */
    public static int USER_UNIT_SHORT_DESCRIPTION = 1;
    /**
     * The length unit descriptor (e.g. kilometers)
     */
    public static int USER_UNIT_DESCRIPTION = 2;
    /**
     * The velocity unit descriptor (e.g. km/h)
     */
    public static int USER_UNIT_VELOCITY_DESCRIPTION = 3;

    /**
     * Converts meters to kilometers
     *
     * @param m the meters to convert
     * @return converted meters in kilometers
     */
    public static double metersToKilometers(double m) {
        return m / 1000;
    }

    /**
     * Converts meters per second to kilometers per hour
     *
     * @param ms the m/s to convert
     * @return converted m/s in kilometers per hour
     */
    public static double metersPerSecondToKilometersPerHour(double ms) {
        return ms * 3.6;
    }

    /**
     * Converts meters to users length unit.
     *
     * @param m       distance in meters
     * @param context The application context
     * @return the given meters in users length unit
     */
    public static double metersToUsersLengthUnit(double m, Context context) {
        return m;
    }

    /**
     * Converts kilometers to the users length unit
     *
     * @param km      the kilometers to convert
     * @param context The application context
     * @return converted kilometers
     */
    public static double kilometerToUsersLengthUnit(double km, Context context) {
        double factor = Double.parseDouble(getUsersUnit(USER_UNIT_FACTOR, context));
        return km * factor;
    }

    /**
     * Converts length in users length unit to kilometers
     *
     * @param length  the length to convert
     * @param context The application context
     * @return kilometers
     */
    public static double usersLengthUnitToKilometers(double length, Context context) {
        double factor = Double.parseDouble(getUsersUnit(USER_UNIT_FACTOR, context));
        return length / factor;
    }

    /**
     * Converts kilometers per hour to the users velocity unit
     *
     * @param kmh     the velocity in kilometers per hour to convert
     * @param context The application context
     * @return converted velocity
     */
    public static double kilometersPerHourToUsersVelocityUnit(double kmh, Context context) {
        double factor = Double.parseDouble(getUsersUnit(USER_UNIT_FACTOR, context));
        return kmh * factor;
    }

    /**
     * Returns the users velocity caption like km or mi
     *
     * @param context The application context
     * @return velocity caption (e.g. km or mi)
     */
    public static String usersLengthDescriptionShort(Context context) {
        return getUsersUnit(USER_UNIT_SHORT_DESCRIPTION, context);
    }

    /**
     * Returns the users length caption like meters
     *
     * @param context The application context
     * @return length caption (e.g. meters)
     */
    public static String usersLengthDescriptionForMeters(Context context) {
        return "meters";
    }

    /**
     * Returns the users velocity caption like km/h or mph
     *
     * @param context The application context
     * @return velocity caption (e.g. km/h or mph)
     */
    public static String usersVelocityDescription(Context context) {
        return getUsersUnit(USER_UNIT_VELOCITY_DESCRIPTION, context);
    }

    /**
     * Fetches the users unit information from preferences
     *
     * @param type    USER_UNIT_FACTOR,USER_UNIT_SHORT_DESCRIPTION, USER_UNIT_DESCRIPTION, USER_UNIT_VELOCITY_DESCRIPTION
     * @param context The application context
     * @return the requested information or "-" if not set
     */
    public static String getUsersUnit(int type, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String unit_key = sharedPref.getString(context.getString(R.string.pref_unit_of_length), "km");
        String unit;
        switch(unit_key){
            case "mi":
                unit = context.getString(R.string.unit_of_length_mi);
                break;
            case "km":
            default:
                unit = context.getString(R.string.unit_of_length_km);
        }
        String[] units = unit.split("\\|");
        if (units.length <= type) {
            return "-";
        }
        return units[type];
    }
}
