package org.secuso.privacyfriendlyactivitytracker.utils;

import java.util.TimeZone;

/**
 * Created by tobias on 03.06.17.
 */

public class TimezoneHelper {

    public static long utcToUsersTimezone(long utcTimestamp){
        return fromUtc(utcTimestamp, TimeZone.getDefault());
    }

    public static long usersTimezoneToUtc(long usersTimezoneTimestamp){
        return toUtc(usersTimezoneTimestamp, TimeZone.getDefault());
    }

    public static long toUtc(long timestamp, TimeZone timeZone){
        return timestamp; //TODO
    }

    public static long fromUtc(long utcTimestamp, TimeZone timeZone){
        return utcTimestamp; // TODO
    }
}
