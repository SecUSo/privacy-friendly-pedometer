package org.secuso.privacyfriendlyactivitytracker.models;

import java.util.Map;

/**
 * Activity day chart model
 *
 * @author Tobias Neidig
 * @version 20160726
 */
public class ActivityChart {
    private String title;
    private Map<String, Double> steps;
    private Map<String, Double> distance;
    private Map<String, Double> calories;
    private ActivityDayChart.DataType displayedDataType;
    private int goal;
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

    public ActivityDayChart.DataType getDisplayedDataType() {
        return displayedDataType;
    }

    public void setDisplayedDataType(ActivityDayChart.DataType displayedDataType) {
        this.displayedDataType = displayedDataType;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

}
