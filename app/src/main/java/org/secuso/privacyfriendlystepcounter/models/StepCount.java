package org.secuso.privacyfriendlystepcounter.models;

/**
 * A step count object represents an interval in which some steps were taken and which walking mode
 * is related to this interval.
 */
public class StepCount {
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
    public double getCalories(){
        return 0.44 * getStepCount(); // TODO implement
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
