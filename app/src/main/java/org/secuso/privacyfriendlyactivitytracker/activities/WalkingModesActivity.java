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
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.adapters.WalkingModesAdapter;
import org.secuso.privacyfriendlyactivitytracker.models.WalkingMode;
import org.secuso.privacyfriendlyactivitytracker.persistence.WalkingModePersistenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity allows the user to manage the walking modes.
 *
 * @author Tobias Neidig
 * @version 20160724
 */

public class WalkingModesActivity extends AppCompatActivity implements WalkingModesAdapter.OnItemClickListener {
    public static final String LOG_CLASS = WalkingModesActivity.class.getName();

    private WalkingModesAdapter mAdapter;
    private RelativeLayout mEmptyView;

    private List<WalkingMode> walkingModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking_modes);

        mEmptyView = findViewById(R.id.empty_view);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        RecyclerView mRecyclerView = findViewById(R.id.walking_modes_list);
        if (mRecyclerView == null) {
            Log.e(LOG_CLASS, "Cannot find recycler view");
            return;
        }
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // init fab
        FloatingActionButton mAddWalkingModeButton = findViewById(R.id.add_walking_mode_btn);
        if (mAddWalkingModeButton == null) {
            Log.e(LOG_CLASS, "Cannot find fab.");
            return;
        }
        mAddWalkingModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(null);
            }
        });

        // init recycler view
        // specify the adapter
        mAdapter = new WalkingModesAdapter(new ArrayList<WalkingMode>());
        mAdapter.setOnItemClickListener(this);
        showWalkingModes();

        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                int position = viewHolder.getLayoutPosition();
                removeWalkingMode(position);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force refresh of walkingModes.
        showWalkingModes();
    }

    /**
     * Loads and shows the walking modes.
     */
    protected void showWalkingModes() {
        walkingModes = WalkingModePersistenceHelper.getAllItems(this);

        this.mAdapter.setItems(walkingModes);
        if (walkingModes.size() == 0) {
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
        AlertDialog.Builder alert = new AlertDialog.Builder(WalkingModesActivity.this, R.style.AppTheme_Dialog);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_walking_mode, null);
        final EditText edittext = dialogLayout.findViewById(R.id.input_name);
        final EditText stepLengthText = dialogLayout.findViewById(R.id.step_length_edit);
        if (position != null) {
            edittext.setText(walkingModes.get(position).getName());
            stepLengthText.setText(String.valueOf(walkingModes.get(position).getStepLength()));
        }
        alert.setMessage(getString(R.string.walking_mode_input_message));
        alert.setTitle(getString(R.string.walking_mode_input_title));
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
                Double stepLength = Double.valueOf(stepLengthText.getText().toString());
                if (name.trim().isEmpty() || stepLength <= 0) {
                    Toast.makeText(WalkingModesActivity.this, getString(R.string.walking_mode_input_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                WalkingMode walkingMode;
                if (position == null) {
                    walkingMode = new WalkingMode();
                } else {
                    walkingMode = walkingModes.get(position);
                }
                walkingMode.setName(name);
                walkingMode.setStepLength(stepLength);
                walkingMode = WalkingModePersistenceHelper.save(walkingMode, getApplicationContext());
                if (position == null) {
                    walkingModes.add(walkingMode);
                    mAdapter.setItems(walkingModes);
                    mAdapter.notifyItemInserted(walkingModes.size() - 1);
                } else {
                    mAdapter.notifyItemChanged(position);
                }
                if (walkingModes.size() == 1 && position == null) {
                    // force view update to hide "empty"-message
                    showWalkingModes();
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
    protected void removeWalkingMode(int position) {
        if (walkingModes.size() == 1) {
            Toast.makeText(this, R.string.walking_mode_at_least_one_is_required, Toast.LENGTH_SHORT).show();
            showWalkingModes();
            return;
        }
        WalkingMode walkingMode = walkingModes.get(position);
        if (walkingMode.isActive()) {
            Toast.makeText(this, R.string.walking_mode_cannot_delete_active_one, Toast.LENGTH_SHORT).show();
            showWalkingModes();
            return;
        }
        if (!WalkingModePersistenceHelper.softDelete(walkingMode, this)) {
            Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
            showWalkingModes();
            return;
        }
        mAdapter.removeItem(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, walkingModes.size() - 1);
        if (walkingModes.size() == 0) {
            // if no text exists, show default view.
            showWalkingModes();
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
    public void onSetActiveClick(View view, int position) {
        WalkingModePersistenceHelper.setActiveMode(walkingModes.get(position), this);
        showWalkingModes();
    }

    @Override
    public void onLearnClick(View view, int position) {
        WalkingMode walkingMode = this.walkingModes.get(position);
        Intent intent = new Intent(this, WalkingModeLearningActivity.class);
        intent.putExtra(WalkingModeLearningActivity.EXTRA_WALKING_MODE_ID, walkingMode.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    @Override
    public void onRemoveClick(View view, int position) {
        removeWalkingMode(position);
    }
}
