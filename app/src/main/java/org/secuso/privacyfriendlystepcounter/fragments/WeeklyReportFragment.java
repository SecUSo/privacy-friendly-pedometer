package org.secuso.privacyfriendlystepcounter.fragments;

import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.secuso.privacyfriendlystepcounter.Factory;
import org.secuso.privacyfriendlystepcounter.R;
import org.secuso.privacyfriendlystepcounter.adapters.ReportAdapter;
import org.secuso.privacyfriendlystepcounter.models.ActivityChart;
import org.secuso.privacyfriendlystepcounter.models.ActivityDayChart;
import org.secuso.privacyfriendlystepcounter.models.ActivitySummary;
import org.secuso.privacyfriendlystepcounter.models.StepCount;
import org.secuso.privacyfriendlystepcounter.persistence.StepCountPersistenceHelper;
import org.secuso.privacyfriendlystepcounter.services.AbstractStepDetectorService;
import org.secuso.privacyfriendlystepcounter.utils.StepDetectionServiceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
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
public class WeeklyReportFragment extends Fragment implements ReportAdapter.OnItemClickListener {
    public static String LOG_TAG = WeeklyReportFragment.class.getName();

    private ReportAdapter mAdapter;

    private OnFragmentInteractionListener mListener;

    private Calendar day;
    private ActivitySummary activitySummary;
    private ActivityChart activityChart;
    private List<Object> reports = new ArrayList<>();

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

        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

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

        // Bind to stepDetector if today is shown
        if (isTodayShown() && StepDetectionServiceHelper.isStepDetectionEnabled(getContext())) {
            bindService();
        }

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
        unbindService();
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onPause(){
        unbindService();
        super.onPause();
    }


    @Override
    public void onDestroy() {
        unbindService();
        super.onDestroy();
    }


    private void bindService(){
        Intent serviceIntent = new Intent(getContext(), Factory.getStepDetectorServiceClass(getContext().getPackageManager()));
        getContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService(){
        if (this.isTodayShown() && mServiceConnection != null && myBinder != null && myBinder.getService() != null) {
            getContext().unbindService(mServiceConnection);
            myBinder = null;
        }
    }

    /**
     * @return is the day which is currently shown today?
     */
    private boolean isTodayShown() {
        Calendar start = (Calendar) day.clone();
        start.set(Calendar.DAY_OF_WEEK, day.getFirstDayOfWeek());
        start.set(Calendar.MILLISECOND, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.WEEK_OF_YEAR, 1);
        return (start.before(day) || start.equals(day)) && end.after(day);
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
        if (!this.isTodayShown() && updated || isDetached() || getContext() == null) {
            // the day shown is not today or is detached
            return;
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Get all step counts for this day.
                day.set(Calendar.DAY_OF_WEEK, day.getFirstDayOfWeek());
                Calendar start = (Calendar) day.clone();
                SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM", getResources().getConfiguration().locale);
                Map<String, Double> stepData = new LinkedHashMap<>();
                Map<String, Double> distanceData = new LinkedHashMap<>();
                Map<String, Double> caloriesData = new LinkedHashMap<>();
                int totalSteps = 0;
                double totalDistance = 0;
                int totalCalories = 0;
                for (int i = 0; i < 7; i++) {
                    List<StepCount> stepCounts = StepCountPersistenceHelper.getStepCountsForDay(start, getContext());
                    // TODO add current steps if today is shown
                    int steps = 0;
                    double distance = 0;
                    int calories = 0;
                    for (StepCount stepCount : stepCounts) {
                        steps += stepCount.getStepCount();
                        distance += (stepCount.getDistance()) / 1000; // convert from m to km
                        calories += stepCount.getCalories();
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

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.", getResources().getConfiguration().locale);
                SimpleDateFormat simpleDateMonthFormat = new SimpleDateFormat("dd. MMMM", getResources().getConfiguration().locale);

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
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                String d = sharedPref.getString(getString(R.string.pref_daily_step_goal), "10000");
                activityChart.setGoal(Integer.valueOf(d));
                // notify ui
                if (mAdapter != null) {
                    mAdapter.notifyItemChanged(reports.indexOf(activitySummary));
                    mAdapter.notifyItemChanged(reports.indexOf(activityChart));
                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.w(LOG_TAG, "Cannot inform adapter for changes.");
                }
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
    }

    @Override
    public void onNextClicked() {
        this.day.add(Calendar.WEEK_OF_YEAR, 1);
        this.generateReports(false);
    }

    @Override
    public void onTitleClicked() {
        int year = this.day.get(Calendar.YEAR);
        int month = this.day.get(Calendar.MONTH);
        int day = this.day.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                WeeklyReportFragment.this.day.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                WeeklyReportFragment.this.day.set(Calendar.MONTH, monthOfYear);
                WeeklyReportFragment.this.day.set(Calendar.YEAR, year);
                WeeklyReportFragment.this.generateReports(false);
            }
        }, year, month, day);
        dialog.show();
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
}
