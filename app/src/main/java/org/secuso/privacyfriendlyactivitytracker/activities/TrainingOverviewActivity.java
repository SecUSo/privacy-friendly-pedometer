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
package org.secuso.privacyfriendlyactivitytracker.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.adapters.TrainingOverviewAdapter;
import org.secuso.privacyfriendlyactivitytracker.models.Training;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;
import org.secuso.privacyfriendlyactivitytracker.persistence.TrainingPersistenceHelper;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModePersistenceHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
            // show current training session if there is one.
            Log.w(LOG_CLASS, "Found active training session");
            startTrainingActivity();
        }

        mEmptyView = findViewById(R.id.empty_view);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        RecyclerView mRecyclerView = findViewById(R.id.training_overview_list);
        if (mRecyclerView == null) {
            Log.e(LOG_CLASS, "Cannot find recycler view");
            return;
        }
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // init fab
        FloatingActionButton mStartTrainingFAB = findViewById(R.id.start_training);
        if (mStartTrainingFAB == null) {
            Log.e(LOG_CLASS, "Cannot find fab.");
            return;
        }
        mStartTrainingFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start new session
                startTrainingActivity();
            }
        });

        // init recycler view
        // specify the adapter
        mAdapter = new TrainingOverviewAdapter(new ArrayList<Training>());
        mAdapter.setOnItemClickListener(this);
        mAdapter.setRecyclerView(mRecyclerView);
        showTrainings();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force refresh of trainings.
        showTrainings();
    }

    protected void startTrainingActivity(){
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    /**
     * Loads and shows the trainings.
     */
    protected void showTrainings() {
        // Load training sessions
        List<Training> trainingsLoadFromDatabase = TrainingPersistenceHelper.getAllItems(this);
        trainings = new ArrayList<>();
        int steps = 0;
        double distance = 0;
        double duration = 0;
        double calories = 0;

        // Add month labels
        Calendar cal = Calendar.getInstance();
        int month = -1;
        for(int i = 0; i < trainingsLoadFromDatabase.size(); i++){
            Training training = trainingsLoadFromDatabase.get(i);
            cal.setTimeInMillis(training.getStart());
            if(month != cal.get(Calendar.MONTH)){
                month = cal.get(Calendar.MONTH);
                DateFormat df = new SimpleDateFormat("MMMM yyyy", getResources().getConfiguration().locale);
                // create dummy training entry to display the new month
                Training monthHeadline = new Training();
                monthHeadline.setName(df.format(cal.getTime()));
                monthHeadline.setViewType(TrainingOverviewAdapter.VIEW_TYPE_MONTH_HEADLINE);
                trainings.add(monthHeadline);
            }
            steps += training.getSteps();
            distance += training.getDistance();
            duration += training.getDuration();
            calories += training.getCalories();
            trainings.add(training);
        }

        // Add summary
        Training summary = new Training();
        summary.setEnd(-1);
        summary.setViewType(TrainingOverviewAdapter.VIEW_TYPE_SUMMARY);
        if(trainings.size() > 0) {
            summary.setStart(trainings.get(trainings.size()-1).getStart());
            summary.setEnd(summary.getStart() + (Double.valueOf(duration * 1000)).longValue());
        }
        summary.setSteps(steps);
        summary.setDistance(distance);
        summary.setCalories(calories);
        trainings.add(0, summary);
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
        final EditText edittext = dialogLayout.findViewById(R.id.input_name);
        final EditText descriptionEditText = dialogLayout.findViewById(R.id.input_description);
        final RatingBar feelingBar = dialogLayout.findViewById(R.id.input_feeling);
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
