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
