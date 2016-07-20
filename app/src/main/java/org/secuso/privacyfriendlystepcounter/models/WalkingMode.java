package org.secuso.privacyfriendlystepcounter.models;

/**
 * A walking mode has a user defined name and a custom step length.
 * Examples for walking modes are "running", "walking", "going"...
 */
public class WalkingMode {
    private int id;
    private double stepLength;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getStepLength() {
        return stepLength;
    }

    public void setStepLength(double stepLength) {
        this.stepLength = stepLength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "WalkingMode{" +
                "id=" + id +
                ", stepLength=" + stepLength +
                ", name='" + name + '\'' +
                '}';
    }
}
