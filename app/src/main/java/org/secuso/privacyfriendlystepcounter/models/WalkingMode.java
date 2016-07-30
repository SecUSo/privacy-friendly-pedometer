package org.secuso.privacyfriendlystepcounter.models;

import android.content.ContentValues;
import android.database.Cursor;

import org.secuso.privacyfriendlystepcounter.persistence.WalkingModeDbHelper;
import org.secuso.privacyfriendlystepcounter.utils.ColorUtil;

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
        alarmItem.setId(c.getLong(c.getColumnIndex(WalkingModeDbHelper.WalkingModeEntry._ID)));
        alarmItem.setName(c.getString(c.getColumnIndex(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_NAME)));
        alarmItem.setStepLength(c.getDouble(c.getColumnIndex(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_STEP_SIZE)));
        alarmItem.setStepFrequency(c.getDouble(c.getColumnIndex(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_STEP_FREQUENCY)));
        alarmItem.setIsActive(Boolean.valueOf(c.getString(c.getColumnIndex(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_ACTIVE))));
        alarmItem.setIsDeleted(Boolean.valueOf(c.getString(c.getColumnIndex(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_DELETED))));
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
        values.put(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_NAME, this.getName());
        values.put(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_STEP_SIZE, this.getStepLength());
        values.put(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_STEP_FREQUENCY, this.getStepFrequency());
        values.put(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_ACTIVE, String.valueOf(this.isActive()));
        values.put(WalkingModeDbHelper.WalkingModeEntry.COLUMN_NAME_IS_DELETED, String.valueOf(this.isDeleted()));
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
        return ColorUtil.getMaterialColor(this.getId() + this.getName());
    }

}
