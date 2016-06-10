package org.secuso.privacyfriendlystepcounter.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.secuso.privacyfriendlystepcounter.models.ActivityChart;
import org.secuso.privacyfriendlystepcounter.models.ActivitySummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import privacyfriendlyexample.org.secuso.example.R;

/**
 * This adapter is used for ReportView.
 *
 * @author Tobias Neidig
 * @version 20160606
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private List<Object> mItems;
    private OnItemClickListener mItemClickListener;
    private static final int TYPE_SUMMARY = 0;
    private static final int TYPE_CHART = 1;

    /**
     * Creates a new Adapter for RecyclerView
     *
     * @param items The data displayed
     */
    public ReportAdapter(List<Object> items) {
        mItems = items;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        View v;
        ViewHolder vh;
        switch(viewType){
            case TYPE_CHART:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_activity_chart, parent, false);
                vh = new ChartViewHolder(v);
                break;
            case TYPE_SUMMARY:
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_activity_summary, parent, false);
                vh = new SummaryViewHolder(v);
                break;
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_CHART:
                ActivityChart chartData = (ActivityChart) mItems.get(position);
                ChartViewHolder chartViewHolder = (ChartViewHolder) holder;
                chartViewHolder.mTitleTextView.setText(chartData.getTitle());

                ArrayList<Entry> chartEntries = new ArrayList<>();
                ArrayList<String> chartXValues = new ArrayList<>();
                int i = 0;
                for(Map.Entry<String, Integer> dataEntry : chartData.getSteps().entrySet()){
                    Entry chartEntry = new Entry(dataEntry.getValue(), i++);
                    chartEntries.add(chartEntry);
                    chartXValues.add(dataEntry.getKey());
                }
                LineDataSet chartLineDataSet = new LineDataSet(chartEntries, "Schritte");
                chartLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(chartLineDataSet);
                LineData data = new LineData(chartXValues, dataSets);

                chartViewHolder.mChart.setData(data);
                chartViewHolder.mChart.invalidate();
                break;
            case TYPE_SUMMARY:
                ActivitySummary summaryData = (ActivitySummary) mItems.get(position);
                SummaryViewHolder summaryViewHolder = (SummaryViewHolder) holder;
                summaryViewHolder.mTitleTextView.setText(summaryData.getTitle());
                summaryViewHolder.mStepsTextView.setText(String.valueOf(summaryData.getSteps()));
                summaryViewHolder.mDistanceTextView.setText(String.valueOf(summaryData.getDistance()));
                summaryViewHolder.mCaloriesTextView.setText(String.valueOf(summaryData.getCalories()));
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (mItems != null) ? mItems.size() : 0;
    }
    // Witht the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        Object item = mItems.get(position);
        if(item instanceof ActivityChart){
            return TYPE_CHART;
        }else if(item instanceof ActivitySummary){
            return TYPE_SUMMARY;
        }else{
            return -1;
        }
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SummaryViewHolder extends ViewHolder{

        public TextView mTitleTextView;
        public TextView mStepsTextView;
        public TextView mDistanceTextView;
        public TextView mCaloriesTextView;

        public SummaryViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.period);
            mStepsTextView = (TextView) itemView.findViewById(R.id.stepCount);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.distanceCount);
            mCaloriesTextView = (TextView) itemView.findViewById(R.id.calorieCount);
        }
    }

    public class ChartViewHolder extends ViewHolder{

        public TextView mTitleTextView;
        public LineChart mChart;

        public ChartViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.period);
            mChart = (LineChart) itemView.findViewById(R.id.chart);
            mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            mChart.getAxisRight().setEnabled(false);
        }
    }
}