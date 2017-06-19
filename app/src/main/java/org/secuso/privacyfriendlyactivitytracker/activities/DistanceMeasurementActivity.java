package org.secuso.privacyfriendlyactivitytracker.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.secuso.privacyfriendlyactivitytracker.Factory;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModePersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;
import org.secuso.privacyfriendlyactivitytracker.utils.UnitHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This activity allows the user to measure the walked distance.
 *
 * @author Tobias Neidig
 * @version 20170619
 */
public class DistanceMeasurementActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String LOG_CLASS = DistanceMeasurementActivity.class.getName();
    private final DistanceMeasurementActivity.BroadcastReceiver broadcastReceiver = new DistanceMeasurementActivity.BroadcastReceiver();
    private Map<Integer, WalkingMode> menuWalkingModes;
    private List<StepCount> stepCounts;
    private boolean start_after_storing_steps;
    private Long start_timestamp;
    private double distance;

    private TextView mTextViewDistance;
    private TextView mTextViewDistanceTitle;
    private Button buttonStart;
    private Button buttonStop;

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
        setContentView(R.layout.activity_distance_measurement);

        mTextViewDistance = (TextView) findViewById(R.id.distance);
        mTextViewDistanceTitle = (TextView) findViewById(R.id.distance_title);
        buttonStop = (Button) findViewById(R.id.stop_button);
        if (buttonStop != null) {
            buttonStop.setOnClickListener(this);
        }
        buttonStart = (Button) findViewById(R.id.start_button);
        if (buttonStart != null) {
            buttonStart.setOnClickListener(this);
        }

        IntentFilter filterRefreshUpdate = new IntentFilter();
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED);
        filterRefreshUpdate.addAction(AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filterRefreshUpdate);
        // Bind to stepDetector
        Intent serviceIntent = new Intent(this, Factory.getStepDetectorServiceClass(this.getPackageManager()));
        getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        start_timestamp = sharedPref.getLong(getString(R.string.pref_distance_measurement_start_timestamp), -1);
        if(start_timestamp < 0){
            start_timestamp = null;
        }

        this.getStepCounts();
        this.updateData();
        this.updateView();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filterRefreshUpdate = new IntentFilter();
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED);
        filterRefreshUpdate.addAction(AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filterRefreshUpdate);
        Intent serviceIntent = new Intent(this, Factory.getStepDetectorServiceClass(this.getPackageManager()));
        getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        start_timestamp = sharedPref.getLong(getString(R.string.pref_distance_measurement_start_timestamp), -1);
        if(start_timestamp < 0){
            start_timestamp = null;
        }
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy(){
        if(this.mServiceConnection != null && this.myBinder != null && this.myBinder.isBinderAlive()){
            getApplicationContext().unbindService(mServiceConnection);
            myBinder = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * Gets the step counts which are stored in database
     */
    protected void getStepCounts() {
        if(this.start_timestamp == null){
            return;
        }
        this.stepCounts = StepCountPersistenceHelper.getStepCountsForInterval(this.start_timestamp, Calendar.getInstance().getTimeInMillis(), this);
    }

    /**
     * Updates the data, gets the current step counts from step detector service.
     * It summaries the step data in stepCount, distance and calories.
     */
    protected void updateData() {
        if(this.start_timestamp == null){
            return;
        }
        List<StepCount> stepCounts = new ArrayList<>(this.stepCounts);
        // Add the steps which are not in database.
        if (myBinder != null) {
            StepCount s = new StepCount();
            if (stepCounts.size() > 0) {
                s.setStartTime(stepCounts.get(stepCounts.size() - 1).getEndTime());
            } else {
                s.setStartTime(this.start_timestamp);
            }
            s.setEndTime(Calendar.getInstance().getTimeInMillis()); // now
            s.setStepCount(myBinder.stepsSinceLastSave());
            s.setWalkingMode(WalkingModePersistenceHelper.getActiveMode(this)); // add current walking mode
            stepCounts.add(s);
        }
        double distance = 0;
        for (StepCount s : stepCounts) {
            distance += s.getDistance();
        }
        this.distance = distance;
    }

    /**
     * Updates the users view to current distance measurement.
     */
        protected void updateView() {
        if(this.start_after_storing_steps || this.start_timestamp != null){
            buttonStart.setVisibility(View.GONE);
            buttonStop.setVisibility(View.VISIBLE);
        }else{
            buttonStart.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.GONE);
        }
        UnitHelper.FormattedUnitPair distance = UnitHelper.formatKilometers(UnitHelper.metersToKilometers(this.distance), this);
        mTextViewDistance.setText(distance.getValue());
        mTextViewDistanceTitle.setText(distance.getUnit());
    }

    /**
     * Stops the distance measurement.
     */
    protected void stopDistanceMeasurement() {
        updateData();
        this.start_timestamp = null;
        this.start_after_storing_steps = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getString(R.string.pref_distance_measurement_start_timestamp), -1);
        editor.apply();
        StepDetectionServiceHelper.stopAllIfNotRequired(this);
        updateView();
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
            case R.id.stop_button:
                stopDistanceMeasurement();
                break;
            case R.id.start_button:
                this.start_after_storing_steps = true;
                // Start persistence service and wait for it before start counting steps
                StepDetectionServiceHelper.startPersistenceService(this);
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
                    if(start_timestamp == null){
                        start_timestamp = Calendar.getInstance().getTime().getTime();
                        start_after_storing_steps = false;
                        distance = 0;
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putLong(getString(R.string.pref_distance_measurement_start_timestamp), start_timestamp);
                        editor.apply();
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
