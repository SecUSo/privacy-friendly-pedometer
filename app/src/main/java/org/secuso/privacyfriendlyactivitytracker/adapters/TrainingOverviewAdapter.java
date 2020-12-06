/*
    Privacy Friendly Pedometer is licensed under the GPLv3.
    Copyright (C) 2017  Tobias Neidig

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlyactivitytracker.adapters;

import android.content.Context;
import android.os.Build;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.models.Training;
import org.secuso.privacyfriendlyactivitytracker.utils.UnitHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * This adapter is used for walking modes.
 *
 * @author Tobias Neidig
 * @version 20160728
 */

public class TrainingOverviewAdapter extends RecyclerView.Adapter<TrainingOverviewAdapter.ViewHolder> {
    public static final int VIEW_TYPE_TRAINING_SESSION = 0;
    public static final int VIEW_TYPE_MONTH_HEADLINE = 1;
    public static final int VIEW_TYPE_SUMMARY = 2;
    private List<Training> mItems;
    private OnItemClickListener mItemClickListener;
    private int mExpandedPosition = -1;
    private RecyclerView recyclerView;

    /**
     * Creates a new Adapter for RecyclerView
     *
     * @param items The data displayed
     */
    public TrainingOverviewAdapter(List<Training> items) {
        mItems = items;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TrainingOverviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        View v;
        ViewHolder vh = null;
        switch (viewType) {
            case VIEW_TYPE_SUMMARY:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_training_summary, parent, false);
                vh = new TrainingSummaryViewHolder(v);
                break;
            case VIEW_TYPE_TRAINING_SESSION:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_training_session, parent, false);
                vh = new TrainingSessionViewHolder(v);
                break;
            case VIEW_TYPE_MONTH_HEADLINE:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_training_month_headline, parent, false);
                vh = new MonthHeadlineViewHolder(v);
                break;
        }

        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        Training training = this.mItems.get(position);
        return training.getViewType();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Training item = mItems.get(position);
        UnitHelper.FormattedUnitPair distance;
        UnitHelper.FormattedUnitPair calories;
        switch (getItemViewType(position)) {
            case VIEW_TYPE_SUMMARY:
                TrainingSummaryViewHolder trainingSummaryViewHolder = (TrainingSummaryViewHolder) holder;
                distance = UnitHelper.formatKilometers(UnitHelper.metersToKilometers(item.getDistance()), trainingSummaryViewHolder.itemView.getContext());
                calories = UnitHelper.formatCalories(item.getCalories(), trainingSummaryViewHolder.itemView.getContext());
                if (trainingSummaryViewHolder.mTextViewSteps != null) {
                    trainingSummaryViewHolder.mTextViewSteps.setText(String.valueOf((int) item.getSteps()));
                }
                if (trainingSummaryViewHolder.mTextViewDistance != null) {
                    trainingSummaryViewHolder.mTextViewDistance.setText(distance.getValue());
                }
                if (trainingSummaryViewHolder.mTextViewCalories != null) {
                    trainingSummaryViewHolder.mTextViewCalories.setText(calories.getValue());
                }
                if (trainingSummaryViewHolder.mTextViewDuration != null) {
                    String durationText = String.format(trainingSummaryViewHolder.itemView.getResources().getConfiguration().locale, "%02d:%02d", ((item.getDuration() / 3600)), ((item.getDuration() - (item.getDuration() / 3600) * 3600) / 60));
                    trainingSummaryViewHolder.mTextViewDuration.setText(durationText);
                }
                if (trainingSummaryViewHolder.mTextViewDistanceTitle != null) {
                    trainingSummaryViewHolder.mTextViewDistanceTitle.setText(distance.getUnit());
                }
                if (trainingSummaryViewHolder.mTextViewCaloriesTitle != null) {
                    trainingSummaryViewHolder.mTextViewCaloriesTitle.setText(calories.getUnit());
                }
                if(trainingSummaryViewHolder.mTextViewSince != null){
                    DateFormat df = new SimpleDateFormat("MMMM yyyy", trainingSummaryViewHolder.itemView.getResources().getConfiguration().locale);
                    Calendar cal = Calendar.getInstance();
                    if(item.getStart() != 0) {
                        cal.setTimeInMillis(item.getStart());
                    }
                    trainingSummaryViewHolder.mTextViewSince.setText(df.format(cal.getTime()));
                }
                break;
            case VIEW_TYPE_MONTH_HEADLINE:
                MonthHeadlineViewHolder monthHeadlineViewHolder = (MonthHeadlineViewHolder) holder;
                if (monthHeadlineViewHolder.mTextViewName != null) {
                    monthHeadlineViewHolder.mTextViewName.setText(item.getName());
                }
                break;
            case VIEW_TYPE_TRAINING_SESSION:
                final TrainingSessionViewHolder trainingSessionViewHolder = (TrainingSessionViewHolder) holder;
                distance = UnitHelper.formatKilometers(UnitHelper.metersToKilometers(item.getDistance()), trainingSessionViewHolder.itemView.getContext());
                calories = UnitHelper.formatCalories(item.getCalories(), trainingSessionViewHolder.itemView.getContext());
                String durationText = String.format(trainingSessionViewHolder.itemView.getResources().getConfiguration().locale, "%02d:%02d", ((item.getDuration() / 3600)), ((item.getDuration() - (item.getDuration() / 3600) * 3600) / 60));

                if (trainingSessionViewHolder.mTextViewName != null) {
                    trainingSessionViewHolder.mTextViewName.setText(item.getName());
                }
                if (trainingSessionViewHolder.mTextViewDescription != null) {
                    trainingSessionViewHolder.mTextViewDescription.setText(item.getDescription());
                }
                if (trainingSessionViewHolder.mTextViewSteps != null) {
                    trainingSessionViewHolder.mTextViewSteps.setText(String.valueOf((int) item.getSteps()));
                }
                if (trainingSessionViewHolder.mTextViewDistance != null) {
                    trainingSessionViewHolder.mTextViewDistance.setText(distance.getValue());
                }
                if (trainingSessionViewHolder.mTextViewCalories != null) {
                    trainingSessionViewHolder.mTextViewCalories.setText(calories.getValue());
                }
                if (trainingSessionViewHolder.mTextViewDuration != null) {
                    trainingSessionViewHolder.mTextViewDuration.setText(durationText);
                }
                if (trainingSessionViewHolder.mRatingBarFeeling != null) {
                    trainingSessionViewHolder.mRatingBarFeeling.setRating(item.getFeeling());
                }
                if (trainingSessionViewHolder.mTextViewSmallSteps != null) {
                    trainingSessionViewHolder.mTextViewSmallSteps.setText(String.valueOf((int) item.getSteps()));
                }
                if (trainingSessionViewHolder.mTextViewSmallDuration != null) {
                    trainingSessionViewHolder.mTextViewSmallDuration.setText(durationText);
                }
                if (trainingSessionViewHolder.mTextViewSmallDistance != null) {
                    trainingSessionViewHolder.mTextViewSmallDistance.setText(distance.getValue());
                }
                if (trainingSessionViewHolder.mTextViewSmallName != null) {
                    trainingSessionViewHolder.mTextViewSmallName.setText(item.getName());
                }
                if (trainingSessionViewHolder.mTextViewDistanceTitle != null) {
                    trainingSessionViewHolder.mTextViewDistanceTitle.setText(distance.getUnit());
                }
                if (trainingSessionViewHolder.mTextViewSmallDistanceTitle != null) {
                    trainingSessionViewHolder.mTextViewSmallDistanceTitle.setText(distance.getUnit());
                }
                if (trainingSessionViewHolder.mTextViewCaloriesTitle != null) {
                    trainingSessionViewHolder.mTextViewCaloriesTitle.setText(calories.getUnit());
                }
                if (trainingSessionViewHolder.mRatingBarFeeling != null) {
                    final boolean isExpanded = position == mExpandedPosition;
                    trainingSessionViewHolder.mExpandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                    trainingSessionViewHolder.mSmallLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) trainingSessionViewHolder.mCardViewLayout.getLayoutParams();
                    layoutParams.setMargins((isExpanded ? 0 : 8), (isExpanded ? 8 : 0), (isExpanded ? 0 : 8), (isExpanded ? 8 : 0));
                    trainingSessionViewHolder.view.setLayoutParams(layoutParams);
                    trainingSessionViewHolder.mCardViewLayout.setRadius((isExpanded) ? 4 : 0);
                    trainingSessionViewHolder.view.setActivated(isExpanded);
                    trainingSessionViewHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mExpandedPosition = isExpanded ? -1 : trainingSessionViewHolder.getAdapterPosition();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                TransitionManager.beginDelayedTransition(recyclerView);
                            }
                            notifyDataSetChanged();
                        }
                    });
                }
                break;
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (mItems != null) ? mItems.size() : 0;
    }

    public void setItems(List<Training> items) {
        this.mItems = items;
        this.notifyDataSetChanged();
    }

    public void removeItem(int position) {
        this.mItems.remove(position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onEditClick(View view, int position);

        void onRemoveClick(View view, int position);
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class TrainingSessionViewHolder extends ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        public TextView mTextViewName;
        public TextView mTextViewDescription;
        public TextView mTextViewSteps;
        public TextView mTextViewDistance;
        public TextView mTextViewCalories;
        public TextView mTextViewDuration;
        public ImageButton mImageButton;
        public RatingBar mRatingBarFeeling;
        public TextView mTextViewSmallSteps;
        public TextView mTextViewSmallDuration;
        public TextView mTextViewSmallDistance;
        public TextView mTextViewSmallName;

        public TextView mTextViewDistanceTitle;
        public TextView mTextViewSmallDistanceTitle;
        public TextView mTextViewCaloriesTitle;

        public RelativeLayout mSmallLayout;
        public LinearLayout mExpandedLayout;
        public View view;
        public CardView mCardViewLayout;

        public TrainingSessionViewHolder(View v) {
            super(v);
            view = v;
            mCardViewLayout = (CardView) v.findViewById(R.id.card_training_session);
            mSmallLayout = (RelativeLayout) v.findViewById(R.id.card_training_session_small);
            mExpandedLayout = (LinearLayout) v.findViewById(R.id.card_training_session_expanded);
            mTextViewName = (TextView) v.findViewById(R.id.training_card_title);
            mTextViewDescription = (TextView) v.findViewById(R.id.training_card_description);
            mTextViewSteps = (TextView) v.findViewById(R.id.training_card_steps);
            mTextViewDistance = (TextView) v.findViewById(R.id.training_card_distance);
            mTextViewCalories = (TextView) v.findViewById(R.id.training_card_calories);
            mTextViewDuration = (TextView) v.findViewById(R.id.training_card_duration);
            mTextViewSmallSteps = (TextView) v.findViewById(R.id.training_small_card_steps);
            mTextViewSmallDuration = (TextView) v.findViewById(R.id.training_small_card_duration);
            mTextViewSmallDistance = (TextView) v.findViewById(R.id.training_small_card_distance);
            mTextViewSmallName = (TextView) v.findViewById(R.id.training_small_card_name);
            mTextViewDistanceTitle = (TextView) v.findViewById(R.id.distanceTitle);
            mTextViewSmallDistanceTitle = (TextView) v.findViewById(R.id.distance_title_small);
            mTextViewCaloriesTitle = (TextView) v.findViewById(R.id.calorieTitle);
            mRatingBarFeeling = (RatingBar) v.findViewById(R.id.training_card_feeling);
            mImageButton = (ImageButton) v.findViewById(R.id.training_card_menu);
            mImageButton.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        public void showPopup(View v, Context c) {
            PopupMenu popup = new PopupMenu(c, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_card_training_session, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.show();
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.card_training_session:
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(view, getLayoutPosition());
                    }
                    break;
                case R.id.training_card_menu:
                    showPopup(view, view.getContext());
                    break;
            }

        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_edit:
                    if (mItemClickListener != null) {
                        mItemClickListener.onEditClick(view, getLayoutPosition());
                        return true;
                    }
                    break;
                case R.id.menu_remove:
                    if (mItemClickListener != null) {
                        mItemClickListener.onRemoveClick(view, getLayoutPosition());
                        return true;
                    }
                    break;
            }
            return false;
        }
    }

    public class MonthHeadlineViewHolder extends ViewHolder {
        public TextView mTextViewName;

        public MonthHeadlineViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.training_month_headline);
        }
    }

    public class TrainingSummaryViewHolder extends ViewHolder{
        public TextView mTextViewSteps;
        public TextView mTextViewDistance;
        public TextView mTextViewCalories;
        public TextView mTextViewDuration;
        public TextView mTextViewDistanceTitle;
        public TextView mTextViewCaloriesTitle;
        public TextView mTextViewSince;

        public TrainingSummaryViewHolder(View v) {
            super(v);
            mTextViewSteps = (TextView) v.findViewById(R.id.training_card_steps);
            mTextViewDistance = (TextView) v.findViewById(R.id.training_card_distance);
            mTextViewCalories = (TextView) v.findViewById(R.id.training_card_calories);
            mTextViewDuration = (TextView) v.findViewById(R.id.training_card_duration);
            mTextViewDistanceTitle = (TextView) v.findViewById(R.id.training_distance_title);
            mTextViewCaloriesTitle = (TextView) v.findViewById(R.id.calorieTitle);
            mTextViewSince = (TextView) v.findViewById(R.id.training_card_since);

        }
    }
}