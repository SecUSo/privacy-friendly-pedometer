package org.secuso.privacyfriendlystepcounter.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.secuso.privacyfriendlystepcounter.Factory;
import org.secuso.privacyfriendlystepcounter.adapters.ReportAdapter;
import org.secuso.privacyfriendlystepcounter.models.ActivityChart;
import org.secuso.privacyfriendlystepcounter.models.ActivitySummary;
import org.secuso.privacyfriendlystepcounter.models.StepCount;
import org.secuso.privacyfriendlystepcounter.models.WalkingMode;
import org.secuso.privacyfriendlystepcounter.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlystepcounter.services.AbstractStepDetectorService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import privacyfriendlyexample.org.secuso.example.R;

/**
 * Report-fragment for one specific day
 *
 * Activities that contain this fragment must implement the
 * {@link DailyReportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DailyReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author Tobias Neidig
 * @version 20160606
 */
public class DailyReportFragment extends Fragment implements ReportAdapter.OnItemClickListener {
    public static String LOG_TAG = DailyReportFragment.class.getName();

    private RecyclerView mRecyclerView;
    private ReportAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private OnFragmentInteractionListener mListener;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver();
    private ActivitySummary activitySummary;
    private ActivityChart activityChart;
    private List<Object> reports = new ArrayList<>();
    private Calendar day;
    private AbstractStepDetectorService.StepDetectorBinder myBinder;

    public class BroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null){
                Log.w(LOG_TAG, "Received intent which is null.");
                return;
            }
            switch(intent.getAction()){
                case AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED:
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED:
                    // Steps were saved, reload step count from database
                    generateReports(true);
                    break;
                default:
            }
        }
    }

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
        day = Calendar.getInstance(); // TODO let the user choose the day
        // subscribe to onStepsSaved and onStepsDetected broadcasts
        IntentFilter filterRefreshUpdate = new IntentFilter();
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED);
        filterRefreshUpdate.addAction(AbstractStepDetectorService.BROADCAST_ACTION_STEPS_DETECTED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, filterRefreshUpdate);
        // Bind to stepDetector if today is shown
        if(isTodayShown()){
            Intent serviceIntent = new Intent(getContext(), Factory.getStepDetectorServiceClass(getContext().getPackageManager()));
            getContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     *
     * @return is the day which is currently shown today?
     */
    private boolean isTodayShown(){
        return (Calendar.getInstance().get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                Calendar.getInstance().get(Calendar.MONTH) == day.get(Calendar.MONTH) &&
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Generates the report objects and adds them to the recycler view adapter.
     * The following reports will be generated:
     *      * ActivitySummary
     *      * ActivityChart
     * If one of these reports does not exist it will be created and added at the end of view.
     *
     * @param updated determines if the method is called because of an update of current steps.
     *                If set to true and another day than today is shown the call will be ignored.
     */
    private void generateReports(boolean updated){
        Log.i(LOG_TAG, "Generating reports");
        if(!this.isTodayShown() && updated){
            // the day shown is not today
            return;
        }
        // Get all step counts for this day.
        List<StepCount> stepCounts = StepCountPersistenceHelper.getStepCountsForDay(day, getContext());
        int stepCount = 0;
        double distance = 0;
        int calories = 0;
        if(this.isTodayShown() && myBinder != null){
            // Today is shown. Add the steps which are not in database.
            StepCount s = new StepCount();
            if(stepCounts.size() > 0) {
                s.setStartTime(stepCounts.get(stepCounts.size()-1).getEndTime());
            }else{
                s.setStartTime(day.getTimeInMillis());
            }
            s.setEndTime(Calendar.getInstance().getTimeInMillis()); // now
            s.setStepCount(myBinder.stepsSinceLastSave());
            s.setWalkingMode(null); // TODO add current walking mode
            stepCounts.add(s);
        }
        Map<String, Double> stepData = new LinkedHashMap<>();
        Map<String, Double> distanceData = new LinkedHashMap<>();
        Map<String, Double> caloriesData = new LinkedHashMap<>();
        WalkingMode wm = null;
        int hour = -1;

        // fill hours without info
        int e;
        if(stepCounts.size() > 0){
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(stepCounts.get(0).getEndTime());
            e = end.get(Calendar.HOUR_OF_DAY);
        }else{
            e = 24;
        }

        for(int h = 0; h < e; h++){
            StepCount s = new StepCount();
            Calendar m = day;
            m.set(Calendar.HOUR_OF_DAY, h);
            m.set(Calendar.MINUTE, 0);
            m.set(Calendar.SECOND, 0);
            s.setStartTime(m.getTimeInMillis());
            s.setEndTime(m.getTimeInMillis());
            stepCounts.add(h, s);
        }
        // Create report data
        SimpleDateFormat formatHourMinute = new SimpleDateFormat("HH:mm",getResources().getConfiguration().locale);
        for(StepCount s : stepCounts){
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(s.getEndTime());

            stepCount += s.getStepCount();
            distance += s.getDistance();
            calories += s.getCalories();

            if(s.getWalkingMode() != wm || end.get(Calendar.HOUR_OF_DAY) != hour || stepCounts.indexOf(s) == stepCounts.size() - 1){
                // create new field
                wm = s.getWalkingMode();
                hour = end.get(Calendar.HOUR_OF_DAY);
                stepData.put(formatHourMinute.format(end.getTime()), (double) stepCount);
                distanceData.put(formatHourMinute.format(end.getTime()), distance);
                caloriesData.put(formatHourMinute.format(end.getTime()), (double) calories);
            }
        }

        // fill hours without info
        if(stepCounts.size() > 0){
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(stepCounts.get(stepCounts.size() - 1).getEndTime());
            e = end.get(Calendar.HOUR_OF_DAY);
            for(int h = e + 1; h < 24; h++){
                stepData.put(h + ":00", null);
                distanceData.put(h + ":00", null);
                caloriesData.put(h + ":00", null);
            }
        }

        distance /= 1000; // TODO Transform distance to km
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd. MMMM", getResources().getConfiguration().locale);

        // create view models
        if(activitySummary == null) {
            activitySummary = new ActivitySummary(stepCount, distance, calories, simpleDateFormat.format(day.getTime()));
            reports.add(activitySummary);
        }else{
            activitySummary.setSteps(stepCount);
            activitySummary.setDistance(distance);
            activitySummary.setCalories(calories);
            activitySummary.setTitle( simpleDateFormat.format(day.getTime()));
        }

        if(activityChart == null) {
            activityChart = new ActivityChart(stepData, distanceData, caloriesData, simpleDateFormat.format(day.getTime()));
            activityChart.setDisplayedDataType(ActivityChart.DataType.STEPS);
            reports.add(activityChart);
        }else{
            activityChart.setSteps(stepData);
            activityChart.setDistance(distanceData);
            activityChart.setCalories(caloriesData);
            activityChart.setTitle( simpleDateFormat.format(day.getTime()));
        }


        // notify ui
        if(mAdapter != null) {
            mAdapter.notifyItemChanged(reports.indexOf(activitySummary));

            mAdapter.notifyItemChanged(reports.indexOf(activityChart));
            mAdapter.notifyDataSetChanged();
        }else{
            Log.w(LOG_TAG, "Cannot inform adapter for changes.");
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

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onActivityChartDataTypeClicked(ActivityChart.DataType newDataType) {
        Log.i(LOG_TAG, "Changing  displayed data type to " + newDataType.toString());
        if(this.activityChart == null){
            return;
        }
        if(this.activityChart.getDisplayedDataType() == newDataType) {
            return;
        }
        this.activityChart.setDisplayedDataType(newDataType);
        if(this.mAdapter != null){
            this.mAdapter.notifyItemChanged(this.reports.indexOf(this.activityChart));
        }
    }
}
