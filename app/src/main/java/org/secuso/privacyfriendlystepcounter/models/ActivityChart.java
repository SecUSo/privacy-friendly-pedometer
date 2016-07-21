package org.secuso.privacyfriendlystepcounter.models;

import java.util.Map;

/**
 * Created by tobias on 06.06.16.
 */
public class ActivityChart {
    public enum DataType{
        STEPS, DISTANCE, CALORIES
    }
    private String title;
    private Map<String, Double> steps;
    private Map<String, Double> distance;
    private Map<String, Double> calories;
    private DataType displayedDataType;

    public ActivityChart(Map<String, Double> steps, Map<String, Double> distance, Map<String, Double> calories, String title) {
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

    public Map<String, Double> getSteps() {
        return steps;
    }

    public void setSteps(Map<String, Double> steps) {
        this.steps = steps;
    }

    public Map<String, Double> getDistance() {
        return distance;
    }

    public void setDistance(Map<String, Double> distance) {
        this.distance = distance;
    }

    public Map<String, Double> getCalories() {
        return calories;
    }

    public void setCalories(Map<String, Double> calories) {
        this.calories = calories;
    }

    public DataType getDisplayedDataType() {
        return displayedDataType;
    }

    public void setDisplayedDataType(DataType displayedDataType) {
        this.displayedDataType = displayedDataType;
    }
}
