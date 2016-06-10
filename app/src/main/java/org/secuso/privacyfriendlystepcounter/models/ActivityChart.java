package org.secuso.privacyfriendlystepcounter.models;

import java.util.Map;

/**
 * Created by tobias on 06.06.16.
 */
public class ActivityChart {
    private String title;
    private Map<String, Integer> steps;
    private Map<String, Integer> distance;
    private Map<String, Integer> calories;

    public ActivityChart(Map<String, Integer> steps, Map<String, Integer> distance, Map<String, Integer> calories, String title) {
        this.steps = steps;
        this.title = title;
        this.distance = distance;
        this.calories = calories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Integer> getSteps() {
        return steps;
    }

    public void setSteps(Map<String, Integer> steps) {
        this.steps = steps;
    }

    public Map<String, Integer> getDistance() {
        return distance;
    }

    public void setDistance(Map<String, Integer> distance) {
        this.distance = distance;
    }

    public Map<String, Integer> getCalories() {
        return calories;
    }

    public void setCalories(Map<String, Integer> calories) {
        this.calories = calories;
    }
}
