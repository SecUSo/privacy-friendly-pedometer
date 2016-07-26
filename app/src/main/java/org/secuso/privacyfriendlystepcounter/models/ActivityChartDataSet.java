package org.secuso.privacyfriendlystepcounter.models;

/**
 * Created by tobias on 26.07.16.
 */
public class ActivityChartDataSet {
    public double value;
    public StepCount stepCount;

    public ActivityChartDataSet(double value, StepCount stepCount) {
        this.value = value;
        this.stepCount = stepCount;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public StepCount getStepCount() {
        return stepCount;
    }

    public void setStepCount(StepCount stepCount) {
        this.stepCount = stepCount;
    }
}
