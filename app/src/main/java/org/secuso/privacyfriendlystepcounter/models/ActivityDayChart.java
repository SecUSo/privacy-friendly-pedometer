package org.secuso.privacyfriendlystepcounter.models;

import java.util.Map;

/**
 * Created by tobias on 06.06.16.
 */
public class ActivityDayChart {
    public enum DataType{
        STEPS, DISTANCE, CALORIES
    }
    private String title;
    private Map<String, ActivityChartDataSet> steps;
    private Map<String, ActivityChartDataSet> distance;
    private Map<String, ActivityChartDataSet> calories;
    private DataType displayedDataType;

    public ActivityDayChart(Map<String, ActivityChartDataSet> steps, Map<String, ActivityChartDataSet> distance, Map<String, ActivityChartDataSet> calories, String title) {
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

    public Map<String, ActivityChartDataSet> getSteps() {
        return steps;
    }

    public void setSteps(Map<String, ActivityChartDataSet> steps) {
        this.steps = steps;
    }

    public Map<String, ActivityChartDataSet> getDistance() {
        return distance;
    }

    public void setDistance(Map<String, ActivityChartDataSet> distance) {
        this.distance = distance;
    }

    public Map<String, ActivityChartDataSet> getCalories() {
        return calories;
    }

    public void setCalories(Map<String, ActivityChartDataSet> calories) {
        this.calories = calories;
    }

    public DataType getDisplayedDataType() {
        return displayedDataType;
    }

    public void setDisplayedDataType(DataType displayedDataType) {
        this.displayedDataType = displayedDataType;
    }
}
