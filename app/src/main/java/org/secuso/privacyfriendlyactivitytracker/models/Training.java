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

import org.secuso.privacyfriendlyactivitytracker.adapters.TrainingOverviewAdapter;
import org.secuso.privacyfriendlyactivitytracker.persistence.TrainingDbHelper;

import java.util.Calendar;

/**
 * A walking mode has a user defined name and a custom step length.
 * Examples for walking modes are "running", "walking", "going"...
 */

public class Training {
    private long id;
    private String name;
    private String description;
    private double steps;
    private double distance;
    private double calories;
    private float feeling;
    private long start;
    private long end;
    /**
     * The view type for TrainingOverviewAdapter
     */
    private int viewType = TrainingOverviewAdapter.VIEW_TYPE_TRAINING_SESSION;

    public static Training from(Cursor c) {
        Training trainingSession = new Training();
        trainingSession.setId(c.getLong(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry._ID)));
        trainingSession.setName(c.getString(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.KEY_NAME)));
        trainingSession.setDescription(c.getString(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.KEY_DESCRIPTION)));
        trainingSession.setSteps(c.getInt(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.KEY_STEPS)));
        trainingSession.setDistance(c.getDouble(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.KEY_DISTANCE)));
        trainingSession.setCalories(c.getDouble(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.KEY_CALORIES)));
        trainingSession.setFeeling(c.getFloat(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.KEY_FEELING)));
        trainingSession.setStart(c.getLong(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.KEY_START)));
        trainingSession.setEnd(c.getLong(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.KEY_END)));
        return trainingSession;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSteps() {
        return steps;
    }

    public void setSteps(double steps) {
        this.steps = steps;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public float getFeeling() {
        return feeling;
    }

    public void setFeeling(float feeling) {
        this.feeling = feeling;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    /**
     * Returns the duration in seconds
     * @return seconds
     */
    public int getDuration(){
        long end = this.getEnd();
        if(end == 0){
            end = Calendar.getInstance().getTimeInMillis();
        }
        return (Double.valueOf((end - this.getStart())/1000)).intValue();
    }

    /**
     * Returns the velocity in meters per second
     * @return m/s
     */
    public double getVelocity(){
        if(this.getDuration() == 0){
            return 0;
        }
        return this.getDistance()/this.getDuration();
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(TrainingDbHelper.TrainingSessionEntry.KEY_NAME, this.getName());
        values.put(TrainingDbHelper.TrainingSessionEntry.KEY_DESCRIPTION, this.getDescription());
        values.put(TrainingDbHelper.TrainingSessionEntry.KEY_STEPS, this.getSteps());
        values.put(TrainingDbHelper.TrainingSessionEntry.KEY_DISTANCE, String.valueOf(this.getDistance()));
        values.put(TrainingDbHelper.TrainingSessionEntry.KEY_CALORIES, String.valueOf(this.getCalories()));
        values.put(TrainingDbHelper.TrainingSessionEntry.KEY_FEELING, String.valueOf(this.getFeeling()));
        values.put(TrainingDbHelper.TrainingSessionEntry.KEY_START, String.valueOf(this.getStart()));
        values.put(TrainingDbHelper.TrainingSessionEntry.KEY_END, String.valueOf(this.getEnd()));
        return values;
    }
}
