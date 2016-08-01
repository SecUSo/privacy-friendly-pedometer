package org.secuso.privacyfriendlystepcounter.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.secuso.privacyfriendlystepcounter.R;
import org.secuso.privacyfriendlystepcounter.utils.UnitUtil;

/**
 * A step count object represents an interval in which some steps were taken and which walking mode
 * is related to this interval.
 */
public class StepCount {

    // from https://github.com/bagilevi/android-pedometer/blob/master/src/name/bagi/levente/pedometer/CaloriesNotifier.java
    private static double METRIC_RUNNING_FACTOR = 1.02784823;
    private static double METRIC_WALKING_FACTOR = 0.708;
    private static double METRIC_AVG_FACTOR = (METRIC_RUNNING_FACTOR + METRIC_WALKING_FACTOR) / 2;


    private int stepCount;
    private long startTime;
    private long endTime;
    private WalkingMode walkingMode;

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public WalkingMode getWalkingMode() {
        return walkingMode;
    }

    public void setWalkingMode(WalkingMode walkingMode) {
        this.walkingMode = walkingMode;
    }

    /**
     * Gets the distance walked in this interval.
     *
     * @return The distance in meters
     */
    public double getDistance(){
        if(getWalkingMode() != null) {
            return getStepCount() * getWalkingMode().getStepLength();
        }else{
            return 0;
        }
    }

    /**
     * Gets the calories
     * @return the calories in cal
     */
    public double getCalories(Context context){
        // inspired by https://github.com/bagilevi/android-pedometer/blob/master/src/name/bagi/levente/pedometer/CaloriesNotifier.java
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        float bodyWeight = Float.parseFloat(sharedPref.getString(context.getString(R.string.pref_weight),context.getString(R.string.pref_default_weight)));
        return bodyWeight * METRIC_AVG_FACTOR * UnitUtil.metersToKilometers(getDistance());
    }
    @Override
    public String toString() {
        return "StepCount{" +
                "stepCount=" + stepCount +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", walkingMode=" + walkingMode +
                '}';
    }
}
