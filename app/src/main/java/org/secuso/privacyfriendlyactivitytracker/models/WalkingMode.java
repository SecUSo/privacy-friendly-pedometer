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

import android.content.ContentValues;
import android.database.Cursor;

import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModeDbHelper;
import org.secuso.privacyfriendlyactivitytracker.utils.ColorHelper;

/**
 * A walking mode has a user defined name and a custom step length.
 * Examples for walking modes are "running", "walking", "going"...
 */

public class WalkingMode {
    private long id;
    private String name;
    private double stepLength;
    private double stepFrequency;
    private boolean is_active;
    private boolean is_deleted;

    public static WalkingMode from(Cursor c) {
        WalkingMode alarmItem = new WalkingMode();
        alarmItem.setId(c.getLong(c.getColumnIndexOrThrow(WalkingModeDbHelper.WalkingModeEntry._ID)));
        alarmItem.setName(c.getString(c.getColumnIndexOrThrow(WalkingModeDbHelper.WalkingModeEntry.KEY_NAME)));
        alarmItem.setStepLength(c.getDouble(c.getColumnIndexOrThrow(WalkingModeDbHelper.WalkingModeEntry.KEY_STEP_SIZE)));
        alarmItem.setStepFrequency(c.getDouble(c.getColumnIndexOrThrow(WalkingModeDbHelper.WalkingModeEntry.KEY_STEP_FREQUENCY)));
        alarmItem.setIsActive(Boolean.valueOf(c.getString(c.getColumnIndexOrThrow(WalkingModeDbHelper.WalkingModeEntry.KEY_IS_ACTIVE))));
        alarmItem.setIsDeleted(Boolean.valueOf(c.getString(c.getColumnIndexOrThrow(WalkingModeDbHelper.WalkingModeEntry.KEY_IS_DELETED))));
        return alarmItem;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public double getStepFrequency() {
        return stepFrequency;
    }

    public void setStepFrequency(double stepFrequency) {
        this.stepFrequency = stepFrequency;
    }

    public boolean isActive() {
        return is_active;
    }

    public void setIsActive(boolean is_active) {
        this.is_active = is_active;
    }

    public boolean isDeleted() {
        return is_deleted;
    }

    public void setIsDeleted(boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(WalkingModeDbHelper.WalkingModeEntry.KEY_NAME, this.getName());
        values.put(WalkingModeDbHelper.WalkingModeEntry.KEY_STEP_SIZE, this.getStepLength());
        values.put(WalkingModeDbHelper.WalkingModeEntry.KEY_STEP_FREQUENCY, this.getStepFrequency());
        values.put(WalkingModeDbHelper.WalkingModeEntry.KEY_IS_ACTIVE, String.valueOf(this.isActive()));
        values.put(WalkingModeDbHelper.WalkingModeEntry.KEY_IS_DELETED, String.valueOf(this.isDeleted()));
        return values;
    }

    @Override
    public String toString() {
        return "WalkingMode{" +
                "id=" + id +
                ", stepLength=" + stepLength +
                ", name='" + name + '\'' +
                '}';
    }


    /**
     * @return the walking-mode-specific color
     */
    public int getColor() {
        return ColorHelper.getMaterialColor(this.getId() + this.getName());
    }

}
