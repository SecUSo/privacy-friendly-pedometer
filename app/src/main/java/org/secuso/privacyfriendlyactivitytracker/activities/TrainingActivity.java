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
package org.secuso.privacyfriendlyactivitytracker.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.secuso.privacyfriendlyactivitytracker.Factory;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.models.Training;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.TrainingDbHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.TrainingPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModePersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;
import org.secuso.privacyfriendlyactivitytracker.utils.UnitHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This activity allows the user to manage the training phases.
 *
 * @author Tobias Neidig
 * @version 20160730
 */

public class TrainingActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * Broadcast action identifier for messages broadcasted when new steps were detected
     */
    public static final String BROADCAST_ACTION_TRAINING_STOPPED = "org.secuso.privacyfriendlystepcounter.TRAINING_STOPPED";
    public static final String LOG_CLASS = TrainingActivity.class.getName();
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver();
    private Map<Integer, WalkingMode> menuWalkingModes;
    private Training training;
    private List<StepCount> stepCounts;
    private Timer mTimer;

    private TextView mTextViewSteps;
    private TextView mTextViewDistance;
    private TextView mTextViewDistanceTitle;
    private TextView mTextViewCalories;
    private TextView mTextViewCaloriesTitle;
    private TextView mTextViewDuration;
    private TextView mTextViewVelocity;
    private TextView mTextViewVelocityTitle;

    private AbstractStepDetectorService.StepDetectorBinder myBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (AbstractStepDetectorService.StepDetectorBinder) service;
            updateData();
            updateView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        boolean isTrainingActive = (new TrainingDbHelper(this).getActiveTraining() != null);

        if (!isTrainingActive) {
            // if no training is active, start persistence service
            StepDetectionServiceHelper.startPersistenceService(this);
            // Now wait for steps saved broadcast message and than create a new training session.
            // We have to wait to ensure, that only the steps since now are counted.
        }
        mTextViewSteps = (TextView) findViewById(R.id.training_steps);
        mTextViewDistance = (TextView) findViewById(R.id.training_distance);
        mTextViewDistanceTitle = (TextView) findViewById(R.id.training_distance_title);
        mTextViewCalories = (TextView) findViewById(R.id.training_calories);
        mTextViewCaloriesTitle = (TextView) findViewById(R.id.training_calories_title);
        mTextViewDuration = (TextView) findViewById(R.id.training_duration);
        mTextViewVelocity = (TextView) findViewById(R.id.training_velocity);
        mTextViewVelocityTitle = (TextView) findViewById(R.id.training_velocity_title);
        Button buttonStop = (Button) findViewById(R.id.training_stop_button);
        if (buttonStop != null) {
            buttonStop.setOnClickListener(this);
        }

        IntentFilter filterRefreshUpdate = new IntentFilter();
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED);
        filterRefreshUpdate.addAction(AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filterRefreshUpdate);
        // Bind to stepDetector
        Intent serviceIntent = new Intent(this, Factory.getStepDetectorServiceClass(this.getPackageManager()));
        getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        this.getStepCounts();
        this.updateData();
        this.updateView();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // Update the timer - no data update required
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateView();
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, Factory.getStepDetectorServiceClass(this.getPackageManager()));
        getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        // Force refresh of view.
        this.getStepCounts();
        this.updateView();
    }

    @Override
    public void onPause(){
        if(this.mServiceConnection != null && this.myBinder != null && this.myBinder.isBinderAlive()){
            getApplicationContext().unbindService(mServiceConnection);
            myBinder = null;
        }
        super.onPause();
    }

    @Override
    public void onDestroy(){
        if(this.mServiceConnection != null && this.myBinder != null && this.myBinder.isBinderAlive()){
            getApplicationContext().unbindService(mServiceConnection);
            myBinder = null;
        }
        super.onDestroy();
    }

    /**
     * Gets the step counts which are stored in database
     */
    protected void getStepCounts() {
        if(this.training == null){
            return;
        }
        this.stepCounts = StepCountPersistenceHelper.getStepCountsForInterval(this.training.getStart(), Calendar.getInstance().getTimeInMillis(), this);
    }

    /**
     * Updates the data, gets the current step counts from step detector service.
     * It summaries the step data in stepCount, distance and calories.
     */
    protected void updateData() {
        if(this.training == null){
            return;
        }
        List<StepCount> stepCounts = new ArrayList<>(this.stepCounts);
        // Add the steps which are not in database.
        if (myBinder != null) {
            StepCount s = new StepCount();
            if (stepCounts.size() > 0) {
                s.setStartTime(stepCounts.get(stepCounts.size() - 1).getEndTime());
            } else {
                s.setStartTime(this.training.getStart());
            }
            s.setEndTime(Calendar.getInstance().getTimeInMillis()); // now
            s.setStepCount(myBinder.stepsSinceLastSave());
            s.setWalkingMode(WalkingModePersistenceHelper.getActiveMode(this)); // add current walking mode
            stepCounts.add(s);
        }
        int stepCount = 0;
        double distance = 0;
        double calories = 0;
        for (StepCount s : stepCounts) {
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(s.getEndTime());

            stepCount += s.getStepCount();
            distance += s.getDistance();
            calories += s.getCalories(getApplicationContext());
        }
        this.training.setSteps(stepCount);
        this.training.setDistance(distance);
        this.training.setCalories(calories);
    }

    /**
     * Updates the users view to current training session.
     */
    protected void updateView() {
        if(this.training == null){
            return;
        }
        mTextViewSteps.setText(String.valueOf((int)this.training.getSteps()));
        UnitHelper.FormattedUnitPair distance = UnitHelper.formatKilometers(UnitHelper.metersToKilometers(this.training.getDistance()), this);
        UnitHelper.FormattedUnitPair calories = UnitHelper.formatCalories(UnitHelper.metersToKilometers(this.training.getCalories()), this);
        mTextViewDistance.setText(distance.getValue());
        mTextViewDistanceTitle.setText(distance.getUnit());
        mTextViewCalories.setText(calories.getValue());
        mTextViewCaloriesTitle.setText(calories.getUnit());
        int duration = this.training.getDuration();
        int hours = (duration / 3600);
        int minutes = (duration - hours * 3600) / 60;
        int seconds = (duration - hours * 3600 - minutes * 60);
        String durationText = String.format(getResources().getConfiguration().locale, "%02d:%02d:%02d", hours, minutes, seconds);
        mTextViewDuration.setText(durationText);
        mTextViewVelocity.setText(String.valueOf(String.format(getResources().getConfiguration().locale, "%.2f", UnitHelper.kilometersPerHourToUsersVelocityUnit(UnitHelper.metersPerSecondToKilometersPerHour(this.training.getVelocity()), this))));
        mTextViewVelocityTitle.setText(UnitHelper.usersVelocityDescription(this));
    }

    /**
     * Stops the training.
     * Stores the current training session in database.
     * Redirects to TrainingOverviewActivity.
     */
    protected void stopTraining() {
        updateData();
        this.training.setEnd(Calendar.getInstance().getTimeInMillis());
        training = TrainingPersistenceHelper.save(this.training, this);
        if(this.mTimer != null){
            this.mTimer.cancel();
            this.mTimer = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        // broadcast the end of training mode
        Intent localIntent = new Intent(BROADCAST_ACTION_TRAINING_STOPPED);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        StepDetectionServiceHelper.stopAllIfNotRequired(this);
        Intent intent = new Intent(this, TrainingOverviewActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Add the walking modes to option menu
        menu.clear();
        menuWalkingModes = new HashMap<>();
        List<WalkingMode> walkingModes = WalkingModePersistenceHelper.getAllItems(this);
        int i = 0;
        for (WalkingMode walkingMode : walkingModes) {
            int id = Menu.FIRST + (i++);
            menuWalkingModes.put(id, walkingMode);
            menu.add(0, id, Menu.NONE, walkingMode.getName()).setChecked(walkingMode.isActive());
        }
        menu.setGroupCheckable(0, true, true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!menuWalkingModes.containsKey(item.getItemId())) {
            return false;
        }
        // update active walking mode
        WalkingMode walkingMode = menuWalkingModes.get(item.getItemId());
        WalkingModePersistenceHelper.setActiveMode(walkingMode, this);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.training_stop_button:
                stopTraining();
                break;
        }
    }

    public class BroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.w(LOG_CLASS, "Received intent which is null.");
                return;
            }
            switch (intent.getAction()) {
                case AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED:
                    updateData();
                    updateView();
                    break;
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED:
                    if(training == null){
                        // no training is active so we create a new session now.
                        // steps were saved now. This allows us to get the exact step counts this now.
                        training = new Training();
                        Calendar cal = Calendar.getInstance();
                        training.setStart(cal.getTimeInMillis());
                        training.setName(String.format(getResources().getConfiguration().locale, getString(R.string.training_default_title), WalkingModePersistenceHelper.getActiveMode(TrainingActivity.this).getName(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH)));
                        training.setDescription("");
                        training = TrainingPersistenceHelper.save(training, TrainingActivity.this);
                        StepDetectionServiceHelper.startAllIfEnabled(getApplicationContext());
                    }
                    // continue with updating the view
                case WalkingModePersistenceHelper.BROADCAST_ACTION_WALKING_MODE_CHANGED:
                    getStepCounts();
                    updateData();
                    updateView();
                    break;
                default:
            }
        }
    }

}
