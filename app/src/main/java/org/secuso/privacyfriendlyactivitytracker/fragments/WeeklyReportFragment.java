package org.secuso.privacyfriendlyactivitytracker.fragments;

import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.secuso.privacyfriendlyactivitytracker.Factory;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.adapters.ReportAdapter;
import org.secuso.privacyfriendlyactivitytracker.models.ActivityChart;
import org.secuso.privacyfriendlyactivitytracker.models.ActivityDayChart;
import org.secuso.privacyfriendlyactivitytracker.models.ActivitySummary;
import org.secuso.privacyfriendlyactivitytracker.models.StepCount;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;
import org.secuso.privacyfriendlyactivitytracker.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModePersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService;
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

/**
 * Report-fragment for one specific day
 * <p/>
 * Activities that contain this fragment must implement the
 * {@link WeeklyReportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeeklyReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author Tobias Neidig
 * @version 20160606
 */
public class WeeklyReportFragment extends Fragment implements ReportAdapter.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static String LOG_TAG = WeeklyReportFragment.class.getName();

    private ReportAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private OnFragmentInteractionListener mListener;

    private Calendar day;
    private ActivitySummary activitySummary;
    private ActivityChart activityChart;
    private List<Object> reports = new ArrayList<>();
    private boolean generatingReports;
    private Map<Integer, WalkingMode> menuWalkingModes;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver();
    private AbstractStepDetectorService.StepDetectorBinder myBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {Log.e("SDF", "service connected");
            myBinder = (AbstractStepDetectorService.StepDetectorBinder) service;
            generateReports(true);
        }
    };

    public WeeklyReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of WeeklyReportFragment.
     */
    public static WeeklyReportFragment newInstance() {
        WeeklyReportFragment fragment = new WeeklyReportFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_report, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // specify an adapter
        // using sample data.
        day = Calendar.getInstance();
        generateReports(false);
        mAdapter = new ReportAdapter(reports);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

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
        Log.e("ASD", "Is today shown? " + isTodayShown());
        Log.e("ASDF", "enabled?" + StepDetectionServiceHelper.isStepDetectionEnabled(getContext()));
        // Bind to stepDetector if today is shown
        if (isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())) {
            bindService();
        }
        registerReceivers();
    }

    @Override
    public void onResume(){
        super.onResume();
        // Bind to stepDetector if today is shown
        if (isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())) {
            bindService();
        }
        registerReceivers();
    }


    @Override
    public void onDetach() {
        unbindService();
        mListener = null;
        unregisterReceivers();
        super.onDetach();
    }

    @Override
    public void onPause(){
        unbindService();
        unregisterReceivers();
        super.onPause();
    }


    @Override
    public void onDestroy() {
        unbindService();
        unregisterReceivers();
        super.onDestroy();
    }

    private void registerReceivers(){
        // subscribe to onStepsSaved and onStepsDetected broadcasts
        IntentFilter filterRefreshUpdate = new IntentFilter();
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED);
        filterRefreshUpdate.addAction(AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED);
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
        Intent serviceIntent = new Intent(getContext(), Factory.getStepDetectorServiceClass(getContext().getPackageManager()));
        getActivity().getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService(){
        if (this.isTodayShown() && mServiceConnection != null && myBinder != null && myBinder.getService() != null) {
            getActivity().getApplicationContext().unbindService(mServiceConnection);
            myBinder = null;
        }
    }

    /**
     * @return is the day which is currently shown today?
     */
    private boolean isTodayShown() {
        if(day == null){
            return false;
        }
        Calendar start = getStartDay();
        Calendar end = getEndDay();
        return (start.before(day) || start.equals(day)) && end.after(day);
    }

    /**
     * The start day is midnight of fist day of week.
     * @return The start day of shown interval
     */
    private Calendar getStartDay(){
        if(day == null){
            return Calendar.getInstance();
        }
        Calendar start = (Calendar) day.clone();
        start.set(Calendar.DAY_OF_WEEK, day.getFirstDayOfWeek());
        start.set(Calendar.MILLISECOND, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        return start;
    }

    /**
     * The end day is midnight of first day of next week.
     * @return The end day of shown interval
     */
    private Calendar getEndDay(){
        Calendar end = getStartDay();
        end.add(Calendar.WEEK_OF_YEAR, 1);
        return end;
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
        if (!this.isTodayShown() && updated || isDetached() || getContext() == null || generatingReports) {
            Log.i(LOG_TAG, "Skipping generating reports");
            // the day shown is not today or is detached
            return;
        }
        Log.i(LOG_TAG, "Generating reports");
        generatingReports = true;
        final Context context = getActivity().getApplicationContext();
        final Locale locale = context.getResources().getConfiguration().locale;
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final Calendar now = Calendar.getInstance();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Get all step counts for this day.
                day.set(Calendar.DAY_OF_WEEK, day.getFirstDayOfWeek());
                Calendar start = (Calendar) day.clone();
                SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM", locale);
                Map<String, Double> stepData = new LinkedHashMap<>();
                Map<String, Double> distanceData = new LinkedHashMap<>();
                Map<String, Double> caloriesData = new LinkedHashMap<>();
                stepData.put("", null);
                distanceData.put("", null);
                caloriesData.put("", null);
                int totalSteps = 0;
                double totalDistance = 0;
                int totalCalories = 0;
                for (int i = 0; i < 7; i++) {
                    List<StepCount> stepCounts = StepCountPersistenceHelper.getStepCountsForDay(start, context);
                    int steps = 0;
                    double distance = 0;
                    int calories = 0;
                    // add current steps if today is shown
                    if(isTodayShown() && myBinder != null
                            && start.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                            && start.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                            && start.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)){

                        StepCount stepCountSinceLastSave = new StepCount();
                        stepCountSinceLastSave.setStepCount(myBinder.stepsSinceLastSave());
                        stepCountSinceLastSave.setWalkingMode(WalkingModePersistenceHelper.getActiveMode(context)); // add current walking mode
                        stepCounts.add(stepCountSinceLastSave);
                    }
                    for (StepCount stepCount : stepCounts) {
                        steps += stepCount.getStepCount();
                        distance += stepCount.getDistance();
                        calories += stepCount.getCalories(context);
                    }
                    stepData.put(formatDate.format(start.getTime()), (double) steps);
                    distanceData.put(formatDate.format(start.getTime()), distance);
                    caloriesData.put(formatDate.format(start.getTime()), (double) calories);
                    totalSteps += steps;
                    totalDistance += distance;
                    totalCalories += calories;
                    if (i != 6) {
                        start.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }

                stepData.put(" ", null);
                distanceData.put(" ", null);
                caloriesData.put(" ", null);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.", locale);
                SimpleDateFormat simpleDateMonthFormat = new SimpleDateFormat("dd. MMMM", locale);

                String title = simpleDateFormat.format(day.getTime()) + " - " + simpleDateMonthFormat.format(start.getTime());

                // create view models
                if (activitySummary == null) {
                    activitySummary = new ActivitySummary(totalSteps, totalDistance, totalCalories, title);
                    reports.add(activitySummary);
                } else {
                    activitySummary.setSteps(totalSteps);
                    activitySummary.setDistance(totalDistance);
                    activitySummary.setCalories(totalCalories);
                    activitySummary.setTitle(title);
                    activitySummary.setHasSuccessor(new Date().after(getEndDay().getTime()));
                    activitySummary.setHasPredecessor(StepCountPersistenceHelper.getDateOfFirstEntry(getContext()).before(day.getTime()));
                }
                if (activityChart == null) {
                    activityChart = new ActivityChart(stepData, distanceData, caloriesData, title);
                    activityChart.setDisplayedDataType(ActivityDayChart.DataType.STEPS);
                    reports.add(activityChart);
                } else {
                    activityChart.setSteps(stepData);
                    activityChart.setDistance(distanceData);
                    activityChart.setCalories(caloriesData);
                    activityChart.setTitle(title);
                }
                String d = sharedPref.getString(context.getString(R.string.pref_daily_step_goal), "10000");
                activityChart.setGoal(Integer.valueOf(d));
                // notify ui
                if (mAdapter != null && mRecyclerView != null && !mRecyclerView.isComputingLayout()) {
                    mAdapter.notifyItemChanged(reports.indexOf(activitySummary));
                    mAdapter.notifyItemChanged(reports.indexOf(activityChart));
                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.w(LOG_TAG, "Cannot inform adapter for changes.");
                }
                generatingReports = false;
            }
        });
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
        this.day.add(Calendar.WEEK_OF_YEAR, -1);
        this.generateReports(false);
        if (isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())) {
            bindService();
        }
    }

    @Override
    public void onNextClicked() {
        this.day.add(Calendar.WEEK_OF_YEAR, 1);
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
                WeeklyReportFragment.this.day.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                WeeklyReportFragment.this.day.set(Calendar.MONTH, monthOfYear);
                WeeklyReportFragment.this.day.set(Calendar.YEAR, year);
                WeeklyReportFragment.this.generateReports(false);
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
        menu.setGroupCheckable(0, true, true);
    }

    @Override
    public void onWalkingModeClicked(int id) {
        if (!menuWalkingModes.containsKey(id)) {
            return;
        }
        // update active walking mode
        WalkingMode walkingMode = menuWalkingModes.get(id);
        WalkingModePersistenceHelper.setActiveMode(walkingMode, getContext());
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
     * to the activity and potentially other fragments cotained in that
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
