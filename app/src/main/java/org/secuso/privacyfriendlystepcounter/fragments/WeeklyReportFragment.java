package org.secuso.privacyfriendlystepcounter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.secuso.privacyfriendlystepcounter.R;
import org.secuso.privacyfriendlystepcounter.adapters.ReportAdapter;
import org.secuso.privacyfriendlystepcounter.models.ActivitySummary;

import java.util.ArrayList;
import java.util.Arrays;

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
public class WeeklyReportFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private OnFragmentInteractionListener mListener;

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
        mAdapter = new ReportAdapter(new ArrayList<>(Arrays.asList(new Object[]{new ActivitySummary(4200 * 7, 42 * 7, 420 * 7, "06. - 12. Juni")})));
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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments cotained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // Currently doing nothing here.
    }
}
