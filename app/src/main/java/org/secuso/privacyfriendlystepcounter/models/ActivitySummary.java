package org.secuso.privacyfriendlystepcounter.models;

/**
 *
 */
public class ActivitySummary {
    private int steps;
    private int distance;
    private int calories;
    private String title;

    public ActivitySummary(int steps, int distance, int calories) {
        this(steps, distance, calories, "");
    }

    public ActivitySummary(int steps, int distance, int calories, String title) {
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

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
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
