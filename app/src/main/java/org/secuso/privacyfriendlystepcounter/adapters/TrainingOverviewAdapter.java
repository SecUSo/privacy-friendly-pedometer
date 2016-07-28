package org.secuso.privacyfriendlystepcounter.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import org.secuso.privacyfriendlystepcounter.R;
import org.secuso.privacyfriendlystepcounter.models.Training;

import java.util.List;

/**
 * This adapter is used for walking modes.
 *
 * @author Tobias Neidig
 * @version 20160728
 */
public class TrainingOverviewAdapter extends RecyclerView.Adapter<TrainingOverviewAdapter.ViewHolder> {
    private List<Training> mItems;
    private OnItemClickListener mItemClickListener;

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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_training_session, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Training item = mItems.get(position);
        if (holder.mTextViewName != null) {
            holder.mTextViewName.setText(item.getName());
        }
        if (holder.mTextViewDescription != null) {
            holder.mTextViewDescription.setText(item.getDescription());
        }
        if (holder.mTextViewSteps != null) {
            holder.mTextViewSteps.setText(String.valueOf((int) item.getSteps()));
        }
        if (holder.mTextViewDistance != null) {
            holder.mTextViewDistance.setText(String.valueOf(item.getDistance()));
        }
        if (holder.mTextViewCalories != null) {
            holder.mTextViewCalories.setText(String.valueOf(item.getCalories()));
        }
        if (holder.mTextViewDuration != null) {
            holder.mTextViewDuration.setText(String.format("%02d:%02d", ((int) (item.getDuration() / 3600)), ((item.getDuration() - (item.getDuration() / 3600) * 3600) / 60)));
        }
        if (holder.mRatingBarFeeling != null) {
            holder.mRatingBarFeeling.setRating(item.getFeeling());
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

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onEditClick(View view, int position);

        void onRemoveClick(View view, int position);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        // each data item is just a string in this case
        public TextView mTextViewName;
        public TextView mTextViewDescription;
        public TextView mTextViewSteps;
        public TextView mTextViewDistance;
        public TextView mTextViewCalories;
        public TextView mTextViewDuration;
        public ImageButton mImageButton;
        public RatingBar mRatingBarFeeling;
        private View view;

        public ViewHolder(View v) {
            super(v);
            view = v;
            mTextViewName = (TextView) v.findViewById(R.id.training_card_title);
            mTextViewDescription = (TextView) v.findViewById(R.id.training_card_description);
            mTextViewSteps = (TextView) v.findViewById(R.id.training_card_steps);
            mTextViewDistance = (TextView) v.findViewById(R.id.training_card_distance);
            mTextViewCalories = (TextView) v.findViewById(R.id.training_card_calories);
            mTextViewDuration = (TextView) v.findViewById(R.id.training_card_duration);
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
}