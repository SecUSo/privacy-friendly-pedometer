package org.secuso.privacyfriendlystepcounter.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_training_session, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
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
            holder.mTextViewDistance.setText(String.valueOf(item.getDistance() / 1000)); // TODO
        }
        if (holder.mTextViewCalories != null) {
            holder.mTextViewCalories.setText(String.valueOf(item.getCalories()));
        }
        String durationText = String.format("%02d:%02d", ((int) (item.getDuration() / 3600)), ((item.getDuration() - (item.getDuration() / 3600) * 3600) / 60));
        if (holder.mTextViewDuration != null) {
            holder.mTextViewDuration.setText(durationText);
        }
        if (holder.mRatingBarFeeling != null) {
            holder.mRatingBarFeeling.setRating(item.getFeeling());
        }
        if (holder.mTextViewSmallSteps != null) {
            holder.mTextViewSmallSteps.setText(String.valueOf((int) item.getSteps()));
        }
        if (holder.mTextViewSmallDuration != null) {
            holder.mTextViewSmallDuration.setText(durationText);
        }
        if (holder.mTextViewSmallDistance != null) {
            holder.mTextViewSmallDistance.setText(String.valueOf(item.getDistance()/1000)); // TODO
        }
        if (holder.mTextViewCalories != null) {
            holder.mTextViewSmallName.setText(item.getName());
        }

        if (holder.mRatingBarFeeling != null) {
            final boolean isExpanded = position == mExpandedPosition;
            holder.mExpandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.mSmallLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.mCardViewLayout.getLayoutParams();
            layoutParams.setMargins((isExpanded ? 0 : 8), (isExpanded ? 8 : 0), (isExpanded ? 0 : 8), (isExpanded ? 8 : 0));
            holder.view.setLayoutParams(layoutParams);
            holder.mCardViewLayout.setRadius((isExpanded) ? 4 : 0);
            holder.view.setActivated(isExpanded);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExpandedPosition = isExpanded ? -1 : holder.getAdapterPosition();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(recyclerView);
                    }
                    notifyDataSetChanged();
                }
            });
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

    public void setRecyclerView(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
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
        public TextView mTextViewSmallSteps;
        public TextView mTextViewSmallDuration;
        public TextView mTextViewSmallDistance;
        public TextView mTextViewSmallName;
        public RelativeLayout mSmallLayout;
        public LinearLayout mExpandedLayout;
        public View view;
        public CardView mCardViewLayout;

        public ViewHolder(View v) {
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
            mTextViewSmallSteps = (TextView) v.findViewById(R.id.training_small_card_steps);;
            mTextViewSmallDuration = (TextView) v.findViewById(R.id.training_small_card_duration);;
            mTextViewSmallDistance = (TextView) v.findViewById(R.id.training_small_card_distance);;
            mTextViewSmallName = (TextView) v.findViewById(R.id.training_small_card_name);;
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