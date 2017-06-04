package org.secuso.privacyfriendlyactivitytracker.fragments;

import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;

import org.secuso.privacyfriendlyactivitytracker.Factory;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.activities.TrainingOverviewActivity;
import org.secuso.privacyfriendlyactivitytracker.adapters.ReportAdapter;
import org.secuso.privacyfriendlyactivitytracker.models.ActivityChartDataSet;
import org.secuso.privacyfriendlyactivitytracker.models.ActivityDayChart;
import org.secuso.privacyfriendlyactivitytracker.models.ActivitySummary;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.models.Training;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.TrainingPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModePersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlyactivitytracker.services.MovementSpeedService;
import org.secuso.privacyfriendlyactivitytracker.utils.StepDetectionServiceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Report-fragment for one specific day
 * <p/>
 * Activities that contain this fragment must implement the
 * {@link DailyReportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DailyReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author Tobias Neidig
 * @version 20160727
 */
public class DailyReportFragment extends Fragment implements ReportAdapter.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static String LOG_TAG = DailyReportFragment.class.getName();
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver();
    private ReportAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OnFragmentInteractionListener mListener;
    private ActivitySummary activitySummary;
    private ActivityDayChart activityChart;
    private List<Object> reports = new ArrayList<>();
    private Calendar day;
    private boolean generatingReports;
    private Map<Integer, WalkingMode> menuWalkingModes;
    private int menuCorrectStepId;
    private AbstractStepDetectorService.StepDetectorBinder myBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (AbstractStepDetectorService.StepDetectorBinder) service;
            generateReports(true);
        }
    };
    private MovementSpeedService.MovementSpeedBinder movementSpeedBinder;
    private ServiceConnection mMovementSpeedServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            movementSpeedBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            movementSpeedBinder = (MovementSpeedService.MovementSpeedBinder) service;
            generateReports(true);
        }
    };

    public DailyReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of DailyReportFragment.
     */
    public static DailyReportFragment newInstance() {
        DailyReportFragment fragment = new DailyReportFragment();
        Bundle args = new Bundle();
        // args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
           mParam1 = getArguments().getString(ARG_PARAM1);
        }*/
        // register for steps-saved-event
        day = Calendar.getInstance();
        registerReceivers();
        // Bind to stepDetector if today is shown
        if (isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())) {
            bindService();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_report, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // Generate the reports
        generateReports(false);

        mAdapter = new ReportAdapter(reports);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if(day == null){
            day = Calendar.getInstance();
        }
        if(!day.getTimeZone().equals(TimeZone.getDefault())) {
            day = Calendar.getInstance();
            generateReports(true);
        }
        if(isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())){
            bindService();
        }
        registerReceivers();
        bindMovementSpeedService();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(day == null){
            day = Calendar.getInstance();
        }
        if(!day.getTimeZone().equals(TimeZone.getDefault())) {
            day = Calendar.getInstance();
            generateReports(true);
        }
        if(isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())){
            bindService();
        }
        registerReceivers();
        bindMovementSpeedService();
    }

    @Override
    public void onDetach() {
        unbindService();
        unbindMovementSpeedService();
        unregisterReceivers();
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onPause(){
        unbindService();
        unbindMovementSpeedService();
        unregisterReceivers();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unbindService();
        unbindMovementSpeedService();
        unregisterReceivers();
        super.onDestroy();
    }

    private void registerReceivers(){
        // subscribe to onStepsSaved and onStepsDetected broadcasts and onSpeedChanged
        IntentFilter filterRefreshUpdate = new IntentFilter();
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED);
        filterRefreshUpdate.addAction(AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED);
        filterRefreshUpdate.addAction(MovementSpeedService.BROADCAST_ACTION_SPEED_CHANGED);
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_INSERTED);
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_UPDATED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, filterRefreshUpdate);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void bindService(){
        if(myBinder == null) {
            Intent serviceIntent = new Intent(getContext(), Factory.getStepDetectorServiceClass(getContext().getPackageManager()));
            getActivity().getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindService(){
        if (this.isTodayShown() && mServiceConnection != null && myBinder != null && myBinder.getService() != null) {
            getActivity().getApplicationContext().unbindService(mServiceConnection);
            myBinder = null;
        }
    }

    private void bindMovementSpeedService(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        boolean isVelocityEnabled = sharedPref.getBoolean(getString(R.string.pref_show_velocity), false);
        if(movementSpeedBinder == null && isVelocityEnabled){
            Intent serviceIntent = new Intent(getContext(), MovementSpeedService.class);
            getActivity().getApplicationContext().startService(serviceIntent);
            getActivity().getApplicationContext().bindService(serviceIntent, mMovementSpeedServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindMovementSpeedService(){
        if(movementSpeedBinder != null && mMovementSpeedServiceConnection != null && movementSpeedBinder.getService() != null){
            getActivity().getApplicationContext().unbindService(mMovementSpeedServiceConnection);
            movementSpeedBinder = null;
        }
        Intent serviceIntent = new Intent(getContext(), MovementSpeedService.class);
        getActivity().getApplicationContext().stopService(serviceIntent);
    }

    /**
     * @return is the day which is currently shown today?
     */
    private boolean isTodayShown() {
        return (Calendar.getInstance().get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                Calendar.getInstance().get(Calendar.MONTH) == day.get(Calendar.MONTH) &&
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Generates the report objects and adds them to the recycler view adapter.
     * The following reports will be generated:
     * * ActivitySummary
     * * ActivityChart
     * If one of these reports does not exist it will be created and added at the end of view.
     *
     * @param updated determines if the method is called because of an update of current steps.
     *                If set to true and another day than today is shown the call will be ignored.
     */
    private void generateReports(boolean updated) {
        Log.i(LOG_TAG, "Generating reports");
        if (!this.isTodayShown() && updated || isDetached() || getContext() == null || generatingReports) {
            // the day shown is not today or is detached
            return;
        }
        generatingReports = true;
        // Get all step counts for this day.
        final Context context = getActivity().getApplicationContext();
        final Locale locale = context.getResources().getConfiguration().locale;
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        List<StepCount> stepCounts = new ArrayList<>();
        //StepCountPersistenceHelper.getStepCountsForDay(day, context);
        /*if (this.isTodayShown() && myBinder != null) {
            // Today is shown. Add the steps which are not in database.
            StepCount s = new StepCount();
            if (stepCounts.size() > 0) {
                s.setStartTime(stepCounts.get(stepCounts.size() - 1).getEndTime());
            } else {
                s.setStartTime(day.getTimeInMillis());
            }
            s.setEndTime(Calendar.getInstance().getTimeInMillis()); // now
            s.setStepCount(myBinder.stepsSinceLastSave());
            s.setWalkingMode(WalkingModePersistenceHelper.getActiveMode(context)); // add current walking mode
            stepCounts.add(s);
        }*/

        WalkingMode wm = null;
        int hour = -1;
        SimpleDateFormat formatHourMinute = new SimpleDateFormat("HH:mm", locale);
        Calendar m = day;
        m.set(Calendar.HOUR_OF_DAY, 0);
        m.set(Calendar.MINUTE, 0);
        m.set(Calendar.SECOND, 0);

        StepCount s = new StepCount();
        s.setStartTime(m.getTimeInMillis());
        s.setEndTime(m.getTimeInMillis()); // one hour more
        s.setStepCount(0);
        s.setWalkingMode(WalkingModePersistenceHelper.getActiveMode(context));
        stepCounts.add(s);
        Log.i(LOG_TAG, s.toString());
        StepCount previousStepCount = s;
        for (int h = 0; h < 24; h++) {
            m.set(Calendar.HOUR_OF_DAY, h);
            s = new StepCount();
            s.setStartTime(m.getTimeInMillis() + 1000);
            if(h != 23) {
                s.setEndTime(m.getTimeInMillis() + 3600000); // one hour more
            }else{
                s.setEndTime(m.getTimeInMillis() + 3599000); // one hour more - 1sec
            }
            s.setWalkingMode(previousStepCount.getWalkingMode());
            previousStepCount = s;
            // load step counts in interval [s.getStartTime(), s.getEndTime()] from database
            List<StepCount> stepCountsFromStorage = StepCountPersistenceHelper.getStepCountsForInterval(s.getStartTime(), s.getEndTime(), context);
            // add non-saved steps if today
            if(isTodayShown() && s.getStartTime() < Calendar.getInstance().getTimeInMillis() && s.getEndTime() >= Calendar.getInstance().getTimeInMillis() && myBinder != null){
                // Today is shown. Add the steps which are not in database.
                StepCount s1 = new StepCount();
                if (stepCountsFromStorage.size() > 0) {
                    s.setStartTime(stepCountsFromStorage.get(stepCountsFromStorage.size() - 1).getEndTime());
                } else {
                    s.setStartTime(s.getStartTime());
                }
                s.setEndTime(Calendar.getInstance().getTimeInMillis()); // now
                s.setStepCount(myBinder.stepsSinceLastSave());
                s.setWalkingMode(WalkingModePersistenceHelper.getActiveMode(context)); // add current walking mode
                stepCounts.add(s);
            }
            // iterate over stepcounts in interval to sum up steps and detect changes in walkingmode
            for(StepCount stepCountFromStorage : stepCountsFromStorage){
                if(!previousStepCount.getWalkingMode().equals(stepCountFromStorage.getWalkingMode())){
                    // we have to create a new stepcount entry.
                    long oldEndTime = s.getEndTime();
                    s.setEndTime(stepCountFromStorage.getStartTime() - 1000);
                    stepCounts.add(s);
                    previousStepCount = s;
                    // create new stepcount.
                    s = new StepCount();
                    s.setStartTime(stepCountFromStorage.getStartTime());
                    s.setEndTime(oldEndTime);
                    s.setStepCount(stepCountFromStorage.getStepCount());
                    s.setWalkingMode(stepCountFromStorage.getWalkingMode());
                }else{
                    s.setStepCount(s.getStepCount() + stepCountFromStorage.getStepCount());
                }
            }

            Log.i(LOG_TAG, s.toString());
            stepCounts.add(s);
        }
        // fill chart data arrays
        int stepCount = 0;
        double distance = 0;
        int calories = 0;
        Map<String, ActivityChartDataSet> stepData = new LinkedHashMap<>();
        Map<String, ActivityChartDataSet> distanceData = new LinkedHashMap<>();
        Map<String, ActivityChartDataSet> caloriesData = new LinkedHashMap<>();
        for (StepCount s1: stepCounts) {
            stepCount += s1.getStepCount();
            distance += s1.getDistance();
            calories += s1.getCalories(context);

            stepData.put(formatHourMinute.format(s1.getEndTime()), new ActivityChartDataSet(stepCount, s1));
            distanceData.put(formatHourMinute.format(s1.getEndTime()), new ActivityChartDataSet(distance, s1));
            caloriesData.put(formatHourMinute.format(s1.getEndTime()), new ActivityChartDataSet(calories, s1));
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd. MMMM", locale);

        // create view models
        if (activitySummary == null) {
            activitySummary = new ActivitySummary(stepCount, distance, calories, simpleDateFormat.format(day.getTime()));
            reports.add(activitySummary);
        } else {
            activitySummary.setSteps(stepCount);
            activitySummary.setDistance(distance);
            activitySummary.setCalories(calories);
            activitySummary.setTitle(simpleDateFormat.format(day.getTime()));
            activitySummary.setHasSuccessor(!this.isTodayShown());
            activitySummary.setHasPredecessor(StepCountPersistenceHelper.getDateOfFirstEntry(getContext()).before(day.getTime()));
            boolean isVelocityEnabled = sharedPref.getBoolean(getString(R.string.pref_show_velocity), false);
            if(movementSpeedBinder != null  && isVelocityEnabled){
                activitySummary.setCurrentSpeed(movementSpeedBinder.getSpeed());
            }else{
                activitySummary.setCurrentSpeed(null);
            }
        }

        if (activityChart == null) {
            activityChart = new ActivityDayChart(stepData, distanceData, caloriesData, simpleDateFormat.format(day.getTime()));
            activityChart.setDisplayedDataType(ActivityDayChart.DataType.STEPS);
            reports.add(activityChart);
        } else {
            activityChart.setSteps(stepData);
            activityChart.setDistance(distanceData);
            activityChart.setCalories(caloriesData);
            activityChart.setTitle(simpleDateFormat.format(day.getTime()));
        }
        String d = sharedPref.getString(context.getString(R.string.pref_daily_step_goal), "10000");
        activityChart.setGoal(Integer.valueOf(d));

        // notify ui
        if (mAdapter != null && mRecyclerView != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!mRecyclerView.isComputingLayout()) {
                        mAdapter.notifyDataSetChanged();
                    }else{
                        Log.w(LOG_TAG, "Cannot inform adapter for changes - RecyclerView is computing layout.");
                    }
                }
            });
        } else {
            Log.w(LOG_TAG, "Cannot inform adapter for changes.");
        }
        generatingReports = false;
    }

    @Override
    public void onActivityChartDataTypeClicked(ActivityDayChart.DataType newDataType) {
        Log.i(LOG_TAG, "Changing  displayed data type to " + newDataType.toString());
        if (this.activityChart == null) {
            return;
        }
        if (this.activityChart.getDisplayedDataType() == newDataType) {
            return;
        }
        this.activityChart.setDisplayedDataType(newDataType);
        if (this.mAdapter != null) {
            this.mAdapter.notifyItemChanged(this.reports.indexOf(this.activityChart));
        }
    }

    @Override
    public void setActivityChartDataTypeChecked(Menu menu) {
        if (this.activityChart == null) {
            return;
        }
        if (this.activityChart.getDisplayedDataType() == null) {
            menu.findItem(R.id.menu_steps).setChecked(true);
        }
        switch (this.activityChart.getDisplayedDataType()) {
            case DISTANCE:
                menu.findItem(R.id.menu_distance).setChecked(true);
                break;
            case CALORIES:
                menu.findItem(R.id.menu_calories).setChecked(true);
                break;
            case STEPS:
            default:
                menu.findItem(R.id.menu_steps).setChecked(true);
        }
    }

    @Override
    public void onPrevClicked() {
        this.day.add(Calendar.DAY_OF_MONTH, -1);
        this.generateReports(false);
        if (isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())) {
            bindService();
        }
    }

    @Override
    public void onNextClicked() {
        this.day.add(Calendar.DAY_OF_MONTH, 1);
        this.generateReports(false);
        if (isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())) {
            bindService();
        }
    }

    @Override
    public void onTitleClicked() {
        int year = this.day.get(Calendar.YEAR);
        int month = this.day.get(Calendar.MONTH);
        int day = this.day.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.AppTheme_DatePickerDialog, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                DailyReportFragment.this.day.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                DailyReportFragment.this.day.set(Calendar.MONTH, monthOfYear);
                DailyReportFragment.this.day.set(Calendar.YEAR, year);
                DailyReportFragment.this.generateReports(false);
                if (isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())) {
                    bindService();
                }
            }
        }, year, month, day);
        dialog.getDatePicker().setMaxDate(new Date().getTime()); // Max date is today
        dialog.getDatePicker().setMinDate(StepCountPersistenceHelper.getDateOfFirstEntry(getContext()).getTime());
        dialog.show();
    }

    @Override
    public void inflateWalkingModeMenu(Menu menu) {
        // Add the walking modes to option menu
        menu.clear();
        menuWalkingModes = new HashMap<>();
        List<WalkingMode> walkingModes = WalkingModePersistenceHelper.getAllItems(getContext());
        int i = 0;
        for (WalkingMode walkingMode : walkingModes) {
            int id = Menu.FIRST + (i++);
            menuWalkingModes.put(id, walkingMode);
            menu.add(0, id, Menu.NONE, walkingMode.getName()).setChecked(walkingMode.isActive());
        }
        menu.add(1, Menu.FIRST + i, Menu.NONE, getString(R.string.correct_steps)).setCheckable(false);
        menuCorrectStepId = Menu.FIRST + i;
        menu.setGroupCheckable(0, true, true);
    }

    @Override
    public void onWalkingModeClicked(int id) {
        if (menuWalkingModes.containsKey(id)) {
            // update active walking mode
            WalkingMode walkingMode = menuWalkingModes.get(id);
            WalkingModePersistenceHelper.setActiveMode(walkingMode, getContext());
        }else if(id == menuCorrectStepId){
            AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext(), R.style.AppTheme_Dialog);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogLayout = inflater.inflate(R.layout.dialog_correct_steps, null);
            final EditText edittext = (EditText) dialogLayout.findViewById(R.id.steps);
            edittext.setText(String.valueOf(getStepCountInclNonSavedSteps(day)));
            alert.setMessage(getString(R.string.correct_steps_dialog_message));
            alert.setTitle(getString(R.string.correct_steps_dialog_title));
            alert.setView(dialogLayout);
            alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                /* Nothing to do here, we will set an on click listener later
                That allows us to handle the dismiss */
                }
            });
            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {/* nothing to do here */}
            });
            final AlertDialog alertDialog = alert.create();
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int steps_new = Integer.parseInt(edittext.getText().toString());
                    int steps_saved = getStepCountInclNonSavedSteps(day);
                    int diff = steps_new - steps_saved;
                    StepCount stepCount = StepCountPersistenceHelper.getLastStepCountEntryForDay(day, getContext());
                    if(stepCount == null){
                        stepCount = new StepCount();
                        stepCount.setStartTime(day.getTime().getTime());
                        stepCount.setEndTime(day.getTime().getTime());
                        stepCount.setStepCount(stepCount.getStepCount() + diff);
                        StepCountPersistenceHelper.storeStepCount(stepCount, getContext());
                    }else {
                        stepCount.setStepCount(stepCount.getStepCount() + diff);
                        StepCountPersistenceHelper.updateStepCount(stepCount, getContext());
                    }
                    generateReports(false);
                    alertDialog.dismiss();
                }
            });
        }
    }

    private int getStepCountInclNonSavedSteps(Calendar day){
        int steps = StepCountPersistenceHelper.getStepCountForDay(day, getContext());
        if (this.isTodayShown() && myBinder != null) {
            // Today is shown. Add the steps which are not in database.
            steps += myBinder.stepsSinceLastSave();
        }
        return steps;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_step_counter_enabled))){
            if(!StepDetectionServiceHelper.isStepDetectionEnabled(getContext())){
                unbindService();
            }else if(this.isTodayShown()){
                bindService();
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // Currently doing nothing here.
    }

    public class BroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.w(LOG_TAG, "Received intent which is null.");
                return;
            }
            switch (intent.getAction()) {
                case AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED:
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED:
                case WalkingModePersistenceHelper.BROADCAST_ACTION_WALKING_MODE_CHANGED:
                case MovementSpeedService.BROADCAST_ACTION_SPEED_CHANGED:
                    // Steps were saved, reload step count from database
                    generateReports(true);
                    break;
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_INSERTED:
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_UPDATED:
                    generateReports(false);
                    break;
                default:
            }
        }
    }
}
