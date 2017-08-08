/*
    Privacy Friendly Pedometer is licensed under the GPLv3.
    Copyright (C) 2017  Tobias Neidig

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlyactivitytracker.models;

/**
 *
 */
public class ActivitySummary {
    private int steps;
    private double distance;
    private int calories;
    private String title;
    private Float currentSpeed = null;
    /**
     * Does the period of time has successors?
     */
    private boolean hasSuccessor;
    /**
     * Does the period of time has predecessors?
     */
    private boolean hasPredecessor;

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

    public boolean isHasSuccessor() {
        return hasSuccessor;
    }

    public void setHasSuccessor(boolean hasSuccessor) {
        this.hasSuccessor = hasSuccessor;
    }

    public boolean isHasPredecessor() {
        return hasPredecessor;
    }

    public void setHasPredecessor(boolean hasPredecessor) {
        this.hasPredecessor = hasPredecessor;
    }

    public Float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(Float currentSpeed) {
        this.currentSpeed = currentSpeed;
    }
}
