package org.secuso.privacyfriendlystepcounter.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.secuso.privacyfriendlystepcounter.R;
import org.secuso.privacyfriendlystepcounter.adapters.TrainingOverviewAdapter;
import org.secuso.privacyfriendlystepcounter.models.Training;
import org.secuso.privacyfriendlystepcounter.models.WalkingMode;
import org.secuso.privacyfriendlystepcounter.persistence.TrainingPersistenceHelper;
import org.secuso.privacyfriendlystepcounter.persistence.WalkingModePersistenceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This activity allows the user to manage the training phases.
 *
 * @author Tobias Neidig
 * @version 20160727
 */
public class TrainingOverviewActivity extends AppCompatActivity implements TrainingOverviewAdapter.OnItemClickListener {
    public static final String LOG_CLASS = TrainingOverviewActivity.class.getName();

    private Map<Integer, WalkingMode> menuWalkingModes;

    private TrainingOverviewAdapter mAdapter;
    private RelativeLayout mEmptyView;

    private List<Training> trainings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_overview);

        if(TrainingPersistenceHelper.getActiveItem(this) != null){
            // TODO show current training session if there is one.
            Log.w(LOG_CLASS, "Found active training session");
        }

        mEmptyView = (RelativeLayout) findViewById(R.id.empty_view);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.training_overview_list);
        if (mRecyclerView == null) {
            Log.e(LOG_CLASS, "Cannot find recycler view");
            return;
        }
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // init fab
        FloatingActionButton mStartTrainingFAB = (FloatingActionButton) findViewById(R.id.start_training);
        if (mStartTrainingFAB == null) {
            Log.e(LOG_CLASS, "Cannot find fab.");
            return;
        }
        mStartTrainingFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO start new session
            }
        });

        // init recycler view
        // specify the adapter
        mAdapter = new TrainingOverviewAdapter(new ArrayList<Training>());
        mAdapter.setOnItemClickListener(this);
        showTrainings();

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force refresh of trainings.
        showTrainings();
    }

    /**
     * Loads and shows the trainigs.
     */
    protected void showTrainings() {
        trainings = TrainingPersistenceHelper.getAllItems(this);

        this.mAdapter.setItems(trainings);
        if (trainings.size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the edit/creation dialog to user.
     *
     * @param position if it's an update give the position in array of the element which should be updated else null
     */
    protected void showEditDialog(final Integer position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(TrainingOverviewActivity.this, R.style.AppTheme_Dialog);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_training, null);
        final EditText edittext = (EditText) dialogLayout.findViewById(R.id.input_name);
        final EditText descriptionEditText = (EditText) dialogLayout.findViewById(R.id.input_description);
        final RatingBar feelingBar = (RatingBar) dialogLayout.findViewById(R.id.input_feeling);
        if (position != null) {
            edittext.setText(trainings.get(position).getName());
            descriptionEditText.setText(String.valueOf(trainings.get(position).getDescription()));
            feelingBar.setRating(trainings.get(position).getFeeling());
        }
        alert.setMessage(getString(R.string.training_input_message));
        alert.setTitle(getString(R.string.training_input_title));
        alert.setView(dialogLayout);
        alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                        /* Nothing to do here, we will set an on click listener later
                        That allows us to handle the dismiss */
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {/* nothing to do here */}
        });
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edittext.getText().toString();
                String description = descriptionEditText.getText().toString();
                float feeling = feelingBar.getRating();
                Training training;
                if (position == null) {
                    training = new Training();
                } else {
                    training = trainings.get(position);
                }
                training.setName(name);
                training.setDescription(description);
                training.setFeeling(feeling);
                training = TrainingPersistenceHelper.save(training, getApplicationContext());
                if (position == null) {
                    trainings.add(training);
                    mAdapter.setItems(trainings);
                    mAdapter.notifyItemInserted(trainings.size() - 1);
                } else {
                    mAdapter.notifyItemChanged(position);
                }
                if (trainings.size() == 1 && position == null) {
                    // force view update to hide "empty"-message
                    showTrainings();
                }
                alertDialog.dismiss();
            }
        });
    }

    /**
     * Removes the motivation text at given position.
     * Notifies the adapter and updates the view.
     * Saves the texts after deletion.
     *
     * @param position the position in array of the text which should be removed
     */
    protected void removeTrainingSession(int position) {
        Training training = trainings.get(position);
        if (!TrainingPersistenceHelper.delete(training, this)) {
            Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
            showTrainings();
            return;
        }
        mAdapter.removeItem(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, trainings.size() - 1);
        if (trainings.size() == 0) {
            // if no text exists, show default view.
            showTrainings();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        showEditDialog(position);
    }

    @Override
    public void onEditClick(View view, int position) {
        showEditDialog(position);
    }

    @Override
    public void onRemoveClick(View view, int position) {
        removeTrainingSession(position);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Add the walking modes to option menu
        menu.clear();
        menuWalkingModes = new HashMap<>();
        List<WalkingMode> walkingModes = WalkingModePersistenceHelper.getAllItems(this);
        int i = 0;
        for (WalkingMode walkingMode : walkingModes) {
            int id = Menu.FIRST + (i++);
            menuWalkingModes.put(id, walkingMode);
            menu.add(0, id, Menu.NONE, walkingMode.getName()).setChecked(walkingMode.isActive());
        }
        menu.setGroupCheckable(0, true, true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!menuWalkingModes.containsKey(item.getItemId())) {
            return false;
        }
        // update active walking mode
        WalkingMode walkingMode = menuWalkingModes.get(item.getItemId());
        WalkingModePersistenceHelper.setActiveMode(walkingMode, this);
        return true;
    }

}
