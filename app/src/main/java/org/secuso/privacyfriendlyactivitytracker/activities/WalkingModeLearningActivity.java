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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.secuso.privacyfriendlyactivitytracker.Factory;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModePersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;
import org.secuso.privacyfriendlyactivitytracker.utils.UnitUtil;

import java.util.Calendar;

/**
 * This activity allows the user to manage the walking modes.
 *
 * @author Tobias Neidig
 * @version 20160724
 */
public class WalkingModeLearningActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String LOG_CLASS = WalkingModeLearningActivity.class.getName();

    public static final String EXTRA_WALKING_MODE_ID = "org.secuso.privacyfriendlystepcounter.walking_mode_id";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver();
    private WalkingMode walkingMode;
    private Long start = null;
    private int stepCountSaved;
    private int stepCount;
    private double distance = 100;

    private TextView mTextViewSteps;

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
        setContentView(R.layout.activity_walking_mode_learning);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // get current training instance
        long walkingModeId = getIntent().getLongExtra(EXTRA_WALKING_MODE_ID, 0);
        walkingMode = WalkingModePersistenceHelper.getItem(walkingModeId, this);
        if (walkingMode == null) {
            // Walking mode not found - return.
            Log.e(LOG_CLASS, "Walking mode not found for id=" + walkingModeId);
            Intent intent = new Intent(this, WalkingModesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.pref_walking_mode_learning_active), true);
        editor.apply();

        // store the step count
        // We have to wait to ensure, that only the steps since now are counted.
        StepDetectionServiceHelper.startPersistenceService(this);

        // Start step counter
        StepDetectionServiceHelper.startAllIfEnabled(this);

        mTextViewSteps = (TextView) findViewById(R.id.walking_mode_learning_steps);
        TextView textViewDistance = (TextView) findViewById(R.id.walking_mode_learning_distance);
        if(textViewDistance != null) {
            textViewDistance.setText(String.valueOf(UnitUtil.metersToUsersLengthUnit(this.distance, this)));
        }
        TextView textViewDistanceTitle = (TextView) findViewById(R.id.walking_mode_learning_distance_title);
        if(textViewDistanceTitle != null) {
            textViewDistanceTitle.setText(UnitUtil.usersLengthDescriptionForMeters(this));
        }
        Button buttonStop = (Button) findViewById(R.id.walking_mode_learning_stop_button);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force refresh of view.
        this.getStepCounts();
        this.updateView();
    }

    @Override
    public void onPause() {
        if (this.mServiceConnection != null && this.myBinder != null && this.myBinder.isBinderAlive()) {
            getApplicationContext().unbindService(mServiceConnection);
            myBinder = null;
        }
        super.onPause();
    }


    protected void getStepCounts() {
        if (this.start == null) {
            return;
        }
        this.stepCountSaved = StepCountPersistenceHelper.getStepCountForInterval(this.start, Calendar.getInstance().getTimeInMillis(), this);
    }

    protected void updateData() {
        if (this.start == null) {
            return;
        }
        this.stepCount = this.stepCountSaved;
        // Add the steps which are not in database.
        if (myBinder != null) {
            this.stepCount += myBinder.stepsSinceLastSave();
        }
    }

    protected void updateView() {
        mTextViewSteps.setText(String.valueOf(this.stepCount));
    }

    protected void stopLearning() {
        updateData();
        if (this.distance == 0 || this.walkingMode == null) {
            Log.e(LOG_CLASS, "Distance or walking mode is null.");
            return;
        }

        // update walking mode if the user walked at minimum one step
        if(this.stepCount != 0) {
            double meterPerStep = this.distance / ((double) this.stepCount);
            this.walkingMode.setStepLength(meterPerStep);
            WalkingModePersistenceHelper.save(this.walkingMode, this);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.pref_walking_mode_learning_active), false);
        editor.apply();
        StepDetectionServiceHelper.stopAllIfNotRequired(this);
        // redirect back
        Intent intent = new Intent(this, WalkingModesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.walking_mode_learning_stop_button:
                stopLearning();
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
                    if (start == null) {
                        start = Calendar.getInstance().getTimeInMillis();
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
