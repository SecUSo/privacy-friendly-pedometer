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

import org.secuso.privacyfriendlystepcounter.R;
import org.secuso.privacyfriendlystepcounter.models.ActivityChartDataSet;
import org.secuso.privacyfriendlystepcounter.models.ActivityDayChart;
import org.secuso.privacyfriendlystepcounter.models.ActivitySummary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This adapter is used for ReportView.
 *
 * @author Tobias Neidig
 * @version 20160720
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private static final int TYPE_SUMMARY = 0;
    private static final int TYPE_DAY_CHART = 1;
    private static final int TYPE_CHART = 2;
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
            case TYPE_DAY_CHART:
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
                break;
            case TYPE_DAY_CHART:
                ActivityDayChart chartData = (ActivityDayChart) mItems.get(position);
                ChartViewHolder chartViewHolder = (ChartViewHolder) holder;
                chartViewHolder.mTitleTextView.setText(chartData.getTitle());

                ArrayList<String> chartXValues = new ArrayList<>();
                int i = 0;
                Map<String, ActivityChartDataSet> dataMap;
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
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                float lastValue = 0;
                float lastWalkingModeId = 0;
                // Generate data for line data sets
                for (Map.Entry<String, ActivityChartDataSet> dataEntry : dataMap.entrySet()) {
                    long walkingModeId = 0;
                    if (dataEntry.getValue() != null && dataEntry.getValue().getStepCount() != null && dataEntry.getValue().getStepCount().getWalkingMode() != null) {
                        walkingModeId = dataEntry.getValue().getStepCount().getWalkingMode().getId();
                    }
                    // Generate new data set if walking mode changed
                    if (lastWalkingModeId != walkingModeId || dataSets.size() == 0) {
                        LineDataSet chartLineDataSet = new LineDataSet(new ArrayList<Entry>(), label);
                        chartLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                        chartLineDataSet.setLineWidth(3);
                        chartLineDataSet.setCircleRadius(3.5f);
                        chartLineDataSet.setCircleHoleRadius(0);
                        chartLineDataSet.setColor(ContextCompat.getColor(chartViewHolder.context, R.color.colorPrimary), 200);
                        chartLineDataSet.setCircleColor(ContextCompat.getColor(chartViewHolder.context, R.color.colorPrimary));
                        if (dataEntry.getValue() != null && dataEntry.getValue().getStepCount() != null && dataEntry.getValue().getStepCount().getWalkingMode() != null) {
                            chartLineDataSet.setFillColor(dataEntry.getValue().getStepCount().getWalkingMode().getColor());
                            chartLineDataSet.setFillAlpha(85);
                            chartLineDataSet.setDrawFilled(true);
                        }
                        chartLineDataSet.setDrawValues(false);
                        dataSets.add(chartLineDataSet);
                    }
                    // add data entry only if not null
                    if (dataEntry.getValue() != null) {
                        Entry chartEntry;
                        if (i > 0 && lastWalkingModeId != walkingModeId) {
                            chartEntry = new Entry(lastValue, i - 1);
                            ((LineDataSet) dataSets.get(dataSets.size() - 1)).getYVals().add(chartEntry);
                        }
                        chartEntry = new Entry(Double.valueOf(dataEntry.getValue().getValue()).floatValue(), i++);
                        ((LineDataSet) dataSets.get(dataSets.size() - 1)).getYVals().add(chartEntry);
                        lastValue = Double.valueOf(dataEntry.getValue().getValue()).floatValue();
                    }
                    lastWalkingModeId = walkingModeId;
                    chartXValues.add(dataEntry.getKey());
                }
                // add daily step goal
                if (chartXValues.size() > 0 && chartData.getDisplayedDataType() == ActivityDayChart.DataType.STEPS) {
                    Entry start = new Entry(chartData.getGoal(), 0);
                    Entry end = new Entry(chartData.getGoal(), chartXValues.size() - 1);
                    LineDataSet chartLineDataSet = new LineDataSet(Arrays.asList(start, end), "");
                    chartLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                    chartLineDataSet.setLineWidth(1);
                    chartLineDataSet.setCircleRadius(0);
                    chartLineDataSet.setCircleHoleRadius(0);
                    chartLineDataSet.setColor(ContextCompat.getColor(chartViewHolder.context, R.color.colorAccent), 200);
                    chartLineDataSet.setDrawValues(false);
                    dataSets.add(chartLineDataSet);
                }

                LineData data = new LineData(chartXValues, dataSets);
                chartViewHolder.mChart.setData(data);
                chartViewHolder.mChart.getLegend().setEnabled(false);
                chartViewHolder.mChart.invalidate();
                // TODO add legend
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
        if (item instanceof ActivityDayChart) {
            return TYPE_DAY_CHART;
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

        void onActivityChartDataTypeClicked(ActivityDayChart.DataType newDataType);

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
            ActivityDayChart.DataType dataType;
            item.setChecked(!item.isChecked());

            switch (item.getItemId()) {
                case R.id.menu_steps:
                    dataType = ActivityDayChart.DataType.STEPS;
                    break;
                case R.id.menu_distance:
                    dataType = ActivityDayChart.DataType.DISTANCE;
                    break;
                case R.id.menu_calories:
                    dataType = ActivityDayChart.DataType.CALORIES;
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