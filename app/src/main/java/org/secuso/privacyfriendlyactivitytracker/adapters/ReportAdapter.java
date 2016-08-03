package org.secuso.privacyfriendlyactivitytracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.ActivityChart;
import org.secuso.privacyfriendlyactivitytracker.models.ActivityChartDataSet;
import org.secuso.privacyfriendlyactivitytracker.models.ActivityDayChart;
import org.secuso.privacyfriendlyactivitytracker.models.ActivitySummary;
import org.secuso.privacyfriendlyactivitytracker.utils.UnitUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
            case TYPE_CHART:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_activity_bar_chart, parent, false);
                vh = new CombinedChartViewHolder(v);
                break;
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
                ActivityChart barChartData = (ActivityChart) mItems.get(position);
                CombinedChartViewHolder barChartViewHolder = (CombinedChartViewHolder) holder;
                barChartViewHolder.mTitleTextView.setText(barChartData.getTitle());
                int barChartI = 0;
                ArrayList<String> barChartXValues = new ArrayList<>();
                Map<String, Double> barChartDataMap;
                String barChartLabel;
                if (barChartData.getDisplayedDataType() == null) {
                    barChartDataMap = barChartData.getSteps();
                    barChartLabel = barChartViewHolder.context.getString(R.string.steps);
                } else {
                    switch (barChartData.getDisplayedDataType()) {
                        case DISTANCE:
                            barChartDataMap = barChartData.getDistance();
                            barChartLabel = barChartViewHolder.context.getString(R.string.action_distance);
                            break;
                        case CALORIES:
                            barChartDataMap = barChartData.getCalories();
                            barChartLabel = barChartViewHolder.context.getString(R.string.calories);
                            break;
                        case STEPS:
                        default:
                            barChartDataMap = barChartData.getSteps();
                            barChartLabel = barChartViewHolder.context.getString(R.string.steps);
                            break;
                    }
                }
                List<BarEntry> dataEntries = new ArrayList<>();
                List<BarEntry> dataEntriesReachedDailyGoal = new ArrayList<>();
                for (Map.Entry<String, Double> dataEntry : barChartDataMap.entrySet()) {
                    barChartXValues.add(barChartI, dataEntry.getKey());
                    if (dataEntry.getValue() != null) {
                        float val = dataEntry.getValue().floatValue();
                        if (barChartData.getDisplayedDataType() == ActivityDayChart.DataType.DISTANCE) {
                            val = Double.valueOf(UnitUtil.kilometerToUsersLengthUnit(UnitUtil.metersToKilometers(val), barChartViewHolder.context)).floatValue();
                        }
                        if (dataEntry.getValue() >= barChartData.getGoal() && barChartData.getDisplayedDataType() == ActivityDayChart.DataType.STEPS) {
                            dataEntriesReachedDailyGoal.add(new BarEntry(barChartI, val));
                        } else {
                            dataEntries.add(new BarEntry(barChartI, val));
                        }
                    }
                    barChartI++;
                }
                BarDataSet barDataSet = new BarDataSet(dataEntries, barChartLabel);
                BarDataSet barDataSetReachedDailyGoal = new BarDataSet(dataEntriesReachedDailyGoal, barChartLabel);
                ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
                // add daily step goal
                if (barChartXValues.size() > 0 && barChartData.getDisplayedDataType() == ActivityDayChart.DataType.STEPS) {
                    Entry start = new Entry(0, barChartData.getGoal());
                    Entry end = new Entry(barChartXValues.size() - 1, barChartData.getGoal());
                    LineDataSet chartLineDataSet = new LineDataSet(Arrays.asList(start, end), barChartViewHolder.context.getString(R.string.pref_title_daily_step_goal));
                    chartLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                    chartLineDataSet.setLineWidth(1);
                    chartLineDataSet.setDrawCircles(false);
                    chartLineDataSet.setColor(ContextCompat.getColor(barChartViewHolder.context, R.color.colorAccent), 200);
                    chartLineDataSet.setDrawValues(false);
                    lineDataSets.add(chartLineDataSet);
                }
                CombinedData combinedData = new CombinedData();
                BarData barData = new BarData(barDataSet, barDataSetReachedDailyGoal);
                barData.setBarWidth(0.5f);
                combinedData.setData(barData);
                combinedData.setData(new LineData(lineDataSets));
                barDataSet.setColor(ContextCompat.getColor(barChartViewHolder.context, R.color.colorPrimary));
                barDataSetReachedDailyGoal.setColor(ContextCompat.getColor(barChartViewHolder.context, R.color.green));
                barChartViewHolder.mChart.setData(combinedData);
                barChartViewHolder.mChart.getXAxis().setValueFormatter(new ArrayListAxisValueFormatter(barChartXValues));
                barChartViewHolder.mChart.invalidate();
                break;
            case TYPE_DAY_CHART:
                ActivityDayChart chartData = (ActivityDayChart) mItems.get(position);
                ChartViewHolder chartViewHolder = (ChartViewHolder) holder;
                chartViewHolder.mTitleTextView.setText(chartData.getTitle());

                final ArrayList<String> chartXValues = new ArrayList<>();
                //chartXValues.add("");
                int i = 1;
                Map<String, ActivityChartDataSet> dataMap;
                Map<Integer, String> legendValues = new LinkedHashMap<>();
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
                            legendValues.put(ContextCompat.getColor(chartViewHolder.itemView.getContext(), R.color.colorAccent), chartViewHolder.context.getString(R.string.pref_title_daily_step_goal));
                            break;
                    }
                }
                legendValues.put(ContextCompat.getColor(chartViewHolder.itemView.getContext(), R.color.colorPrimary), label);
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
                        chartLineDataSet.setDrawCircleHole(false);
                        chartLineDataSet.setColor(ContextCompat.getColor(chartViewHolder.context, R.color.colorPrimary), 200);
                        chartLineDataSet.setCircleColor(ContextCompat.getColor(chartViewHolder.context, R.color.colorPrimary));
                        if (dataEntry.getValue() != null && dataEntry.getValue().getStepCount() != null && dataEntry.getValue().getStepCount().getWalkingMode() != null) {
                            int color = dataEntry.getValue().getStepCount().getWalkingMode().getColor();
                            int alpha = 85;
                            int colorWithAlph = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
                            chartLineDataSet.setFillColor(color);
                            chartLineDataSet.setFillAlpha(alpha);
                            chartLineDataSet.setDrawFilled(true);
                            legendValues.put(colorWithAlph, dataEntry.getValue().getStepCount().getWalkingMode().getName());
                        }
                        chartLineDataSet.setDrawValues(false);
                        dataSets.add(chartLineDataSet);
                    }
                    // add data entry only if not null
                    if (dataEntry.getValue() != null) {
                        float val = Double.valueOf(dataEntry.getValue().getValue()).floatValue();
                        if (chartData.getDisplayedDataType() == ActivityDayChart.DataType.DISTANCE) {
                            val = Double.valueOf(UnitUtil.kilometerToUsersLengthUnit(UnitUtil.metersToKilometers(val), chartViewHolder.context)).floatValue();
                        }
                        Entry chartEntry;
                        if (i > 0 && lastWalkingModeId != walkingModeId) {
                            chartEntry = new Entry(i - 1, lastValue);
                            ((LineDataSet) dataSets.get(dataSets.size() - 1)).getValues().add(chartEntry);
                        }
                        chartEntry = new Entry(i++, val);
                        ((LineDataSet) dataSets.get(dataSets.size() - 1)).getValues().add(chartEntry);
                        lastValue = val;
                    }
                    lastWalkingModeId = walkingModeId;
                    chartXValues.add(dataEntry.getKey());
                }
                // add daily step goal
                if (chartXValues.size() > 0 && chartData.getDisplayedDataType() == ActivityDayChart.DataType.STEPS) {
                    Entry start = new Entry(0, 0);//chartData.getGoal()); // TODO
                    Entry end = new Entry(chartXValues.size() - 1, chartData.getGoal());
                    LineDataSet chartLineDataSet = new LineDataSet(Arrays.asList(start, end), "");
                    chartLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                    chartLineDataSet.setLineWidth(1);
                    chartLineDataSet.setDrawCircles(false);
                    chartLineDataSet.setColor(ContextCompat.getColor(chartViewHolder.context, R.color.colorAccent), 200);
                    chartLineDataSet.setDrawValues(false);
                    dataSets.add(chartLineDataSet);
                }
                // TODO line data not displayed correctly.
                LineData data = new LineData(dataSets);
                chartViewHolder.mChart.setData(data);
                chartViewHolder.mChart.getXAxis().setValueFormatter(new ArrayListAxisValueFormatter(chartXValues));
                // add legend
                Legend legend = chartViewHolder.mChart.getLegend();
                legend.setComputedColors(new ArrayList<Integer>());
                legend.setComputedLabels(new ArrayList<String>());
                legend.setCustom(new ArrayList<>(legendValues.keySet()), new ArrayList<>(legendValues.values()));
                chartViewHolder.mChart.invalidate();
                break;
            case TYPE_SUMMARY:
                ActivitySummary summaryData = (ActivitySummary) mItems.get(position);
                SummaryViewHolder summaryViewHolder = (SummaryViewHolder) holder;
                summaryViewHolder.mTitleTextView.setText(summaryData.getTitle());
                summaryViewHolder.mStepsTextView.setText(String.valueOf(summaryData.getSteps()));
                summaryViewHolder.mDistanceTextView.setText(String.format(summaryViewHolder.itemView.getResources().getConfiguration().locale, "%.2f", UnitUtil.kilometerToUsersLengthUnit(UnitUtil.metersToKilometers(summaryData.getDistance()), summaryViewHolder.itemView.getContext())));
                summaryViewHolder.mCaloriesTextView.setText(String.valueOf(summaryData.getCalories()));
                summaryViewHolder.mDistanceTitleTextView.setText(UnitUtil.usersLengthDescriptionShort(summaryViewHolder.itemView.getContext()));
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
        } else if (item instanceof ActivityChart) {
            return TYPE_CHART;
        } else {
            return -1;
        }
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onActivityChartDataTypeClicked(ActivityDayChart.DataType newDataType);

        void setActivityChartDataTypeChecked(Menu popup);

        void onPrevClicked();

        void onNextClicked();

        void onTitleClicked();
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
        public TextView mDistanceTitleTextView;
        public ImageButton mPrevButton;
        public ImageButton mNextButton;

        public SummaryViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.period);
            mStepsTextView = (TextView) itemView.findViewById(R.id.stepCount);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.distanceCount);
            mCaloriesTextView = (TextView) itemView.findViewById(R.id.calorieCount);
            mDistanceTitleTextView = (TextView) itemView.findViewById(R.id.distanceTitle);
            mPrevButton = (ImageButton) itemView.findViewById(R.id.prev_btn);
            mNextButton = (ImageButton) itemView.findViewById(R.id.next_btn);

            mPrevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onPrevClicked();
                    }
                }
            });
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onNextClicked();
                    }
                }
            });
            mTitleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onTitleClicked();
                    }
                }
            });
        }
    }

    public abstract class AbstractChartViewHolder extends ViewHolder implements PopupMenu.OnMenuItemClickListener {

        public TextView mTitleTextView;
        public ImageButton mMenuButton;
        public Context context;

        public AbstractChartViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            mTitleTextView = (TextView) itemView.findViewById(R.id.period);
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

    public class ChartViewHolder extends AbstractChartViewHolder {
        public LineChart mChart;

        public ChartViewHolder(View itemView) {
            super(itemView);
            mChart = (LineChart) itemView.findViewById(R.id.chart);
            mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            mChart.getAxisRight().setEnabled(false);
            mChart.setTouchEnabled(false);
            mChart.setDoubleTapToZoomEnabled(false);
            mChart.setPinchZoom(false);
            mChart.setDescription("");
        }
    }

    public class CombinedChartViewHolder extends AbstractChartViewHolder {
        public CombinedChart mChart;

        public CombinedChartViewHolder(View itemView) {
            super(itemView);
            mChart = (CombinedChart) itemView.findViewById(R.id.chart);
            mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            mChart.getAxisRight().setEnabled(false);
            mChart.setTouchEnabled(false);
            mChart.setDoubleTapToZoomEnabled(false);
            mChart.setPinchZoom(false);
            mChart.setDescription("");
            mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                    CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.BUBBLE, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
            });
        }
    }

    public class ArrayListAxisValueFormatter implements AxisValueFormatter {
        private List<String> values;

        public ArrayListAxisValueFormatter(List<String> values) {
            this.values = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if (this.values.size() <= (int) value || (int) value < 0) {
                return "--";
            }
            return this.values.get((int) value);
        }

        @Override
        public int getDecimalDigits() {
            return -1;
        }
    }
}