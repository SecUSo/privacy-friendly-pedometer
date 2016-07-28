package org.secuso.privacyfriendlystepcounter.models;

import android.content.ContentValues;
import android.database.Cursor;

import org.secuso.privacyfriendlystepcounter.persistence.TrainingDbHelper;

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
    private double start;
    private double end;

    public static Training from(Cursor c) {
        Training trainingSession = new Training();
        trainingSession.setId(c.getLong(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry._ID)));
        trainingSession.setName(c.getString(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_NAME)));
        trainingSession.setDescription(c.getString(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_DESCRIPTION)));
        trainingSession.setSteps(c.getInt(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_STEPS)));
        trainingSession.setDistance(c.getDouble(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_DISTANCE)));
        trainingSession.setCalories(c.getDouble(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_CALORIES)));
        trainingSession.setFeeling(c.getFloat(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_FEELING)));
        trainingSession.setStart(c.getDouble(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_START)));
        trainingSession.setEnd(c.getDouble(c.getColumnIndex(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_END)));
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

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    /**
     * Returns the duration in seconds
     */
    public int getDuration(){
        return (Double.valueOf((this.getEnd() - this.getStart())/1000)).intValue();
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_NAME, this.getName());
        values.put(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_DESCRIPTION, this.getDescription());
        values.put(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_STEPS, this.getSteps());
        values.put(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_DISTANCE, String.valueOf(this.getDistance()));
        values.put(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_CALORIES, String.valueOf(this.getCalories()));
        values.put(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_FEELING, String.valueOf(this.getFeeling()));
        values.put(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_START, String.valueOf(this.getStart()));
        values.put(TrainingDbHelper.TrainingSessionEntry.COLUMN_NAME_END, String.valueOf(this.getEnd()));
        return values;
    }
}
