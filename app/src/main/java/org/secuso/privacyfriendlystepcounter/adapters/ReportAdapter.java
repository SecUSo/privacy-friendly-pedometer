package org.secuso.privacyfriendlystepcounter.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import org.secuso.privacyfriendlystepcounter.R;

/**
 * This adapter is used for ReportView.
 *
 * @author Tobias Neidig
 * @version 20160720
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private static final int TYPE_SUMMARY = 0;
    private static final int TYPE_CHART = 1;
    private List<Object> mItems;
    private OnItemClickListener mItemClickListener;

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
        switch (viewType) {
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
                Map<String, Double> dataMap;
                String label;
                if (chartData.getDisplayedDataType() == null) {
                    dataMap = chartData.getSteps();
                    label = chartViewHolder.context.getString(R.string.steps);
                } else {
                    switch (chartData.getDisplayedDataType()) {
                        case DISTANCE:
                            dataMap = chartData.getDistance();
                            label = chartViewHolder.context.getString(R.string.action_distance);
                            break;
                        case CALORIES:
                            dataMap = chartData.getCalories();
                            label = chartViewHolder.context.getString(R.string.calories);
                            break;
                        case STEPS:
                        default:
                            dataMap = chartData.getSteps();
                            label = chartViewHolder.context.getString(R.string.steps);
                            break;
                    }
                }

                for (Map.Entry<String, Double> dataEntry : dataMap.entrySet()) {
                    if (dataEntry.getValue() != null) {
                        Entry chartEntry = new Entry(dataEntry.getValue().floatValue(), i++);
                        chartEntries.add(chartEntry);
                    }
                    chartXValues.add(dataEntry.getKey());
                }
                LineDataSet chartLineDataSet = new LineDataSet(chartEntries, label);
                chartLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                chartLineDataSet.setLineWidth(3);
                chartLineDataSet.setCircleRadius(3.5f);
                chartLineDataSet.setCircleHoleRadius(0);
                chartLineDataSet.setColor(ContextCompat.getColor(chartViewHolder.context, R.color.colorPrimary), 200);
                chartLineDataSet.setCircleColor(ContextCompat.getColor(chartViewHolder.context, R.color.colorPrimary));

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
        if (item instanceof ActivityChart) {
            return TYPE_CHART;
        } else if (item instanceof ActivitySummary) {
            return TYPE_SUMMARY;
        } else {
            return -1;
        }
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onActivityChartDataTypeClicked(ActivityChart.DataType newDataType);

        void setActivityChartDataTypeChecked(Menu popup);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SummaryViewHolder extends ViewHolder {

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

    public class ChartViewHolder extends ViewHolder implements PopupMenu.OnMenuItemClickListener {

        public TextView mTitleTextView;
        public LineChart mChart;
        public ImageButton mMenuButton;
        public Context context;

        public ChartViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            mTitleTextView = (TextView) itemView.findViewById(R.id.period);
            mChart = (LineChart) itemView.findViewById(R.id.chart);
            mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            mChart.getAxisRight().setEnabled(false);
            mChart.setTouchEnabled(false);
            mChart.setDoubleTapToZoomEnabled(false);
            mChart.setPinchZoom(false);
            mChart.setDescription("");
            mMenuButton = (ImageButton) itemView.findViewById(R.id.periodMoreButton);
            mMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(mMenuButton, context);
                }
            });
        }

        public void showPopup(View v, Context c) {
            PopupMenu popup = new PopupMenu(c, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_card_activity_summary, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            if (mItemClickListener != null) {
                mItemClickListener.setActivityChartDataTypeChecked(popup.getMenu());
            }
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            ActivityChart.DataType dataType;
            item.setChecked(!item.isChecked());

            switch (item.getItemId()) {
                case R.id.menu_steps:
                    dataType = ActivityChart.DataType.STEPS;
                    break;
                case R.id.menu_distance:
                    dataType = ActivityChart.DataType.DISTANCE;
                    break;
                case R.id.menu_calories:
                    dataType = ActivityChart.DataType.CALORIES;
                    break;
                default:
                    return false;
            }
            if (mItemClickListener != null) {
                mItemClickListener.onActivityChartDataTypeClicked(dataType);
                return true;
            } else {
                return false;
            }
        }
    }
}