package org.secuso.privacyfriendlyactivitytracker.models;

/**
 *
 */
public class ActivitySummary {
    private int steps;
    private double distance;
    private int calories;
    private String title;

    public ActivitySummary(int steps, double distance, int calories) {
        this(steps, distance, calories, "");
    }

    public ActivitySummary(int steps, double distance, int calories, String title) {
        this.steps = steps;
        this.distance = distance;
        this.calories = calories;
        this.title = title;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
