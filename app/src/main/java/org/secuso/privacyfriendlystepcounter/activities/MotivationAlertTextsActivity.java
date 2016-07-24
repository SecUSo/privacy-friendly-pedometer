package org.secuso.privacyfriendlystepcounter.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.secuso.privacyfriendlystepcounter.adapters.MotivationAlertTextsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import privacyfriendlyexample.org.secuso.example.R;

/**
 * This activity allows the user to manage the motivation texts.
 *
 * @author Tobias Neidig
 * @version 20160724
 */
public class MotivationAlertTextsActivity extends AppCompatActivity implements MotivationAlertTextsAdapter.OnItemClickListener {
    public static final String LOG_CLASS = MotivationAlertTextsActivity.class.getName();

    private MotivationAlertTextsAdapter mAdapter;
    private RelativeLayout mEmptyView;

    private List<String> motivationTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motivation_alert_texts);

        mEmptyView = (RelativeLayout) findViewById(R.id.empty_view);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.motivation_text_list);
        if (mRecyclerView == null) {
            Log.e(LOG_CLASS, "Cannot find recycler view");
            return;
        }
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // init fab
        FloatingActionButton mAddMotivationTextButton = (FloatingActionButton) findViewById(R.id.add_motivation_text_btn);
        if (mAddMotivationTextButton == null) {
            Log.e(LOG_CLASS, "Cannot find fab.");
            return;
        }
        mAddMotivationTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(null);
            }
        });

        // init recycler view
        // specify the adapter
        mAdapter = new MotivationAlertTextsAdapter(new ArrayList<String>());
        mAdapter.setOnItemClickListener(this);
        showMotivationAlertTexts();

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
                removeText(position);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force refresh of motivationTexts.
        showMotivationAlertTexts();
    }

    /**
     * Loads and shows the motivation texts. The motivation texts are loaded from shared preferences.
     * if no preference exists it will be set to the default texts given in the resources.
     * If motivation texts set is empty the view will be set to 'empty view'
     */
    protected void showMotivationAlertTexts() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> defaultStringSet = new HashSet<>(Arrays.asList(getResources().getStringArray(R.array.pref_default_notification_motivation_alert_messages)));
        Set<String> stringSet = sharedPref.getStringSet(this.getString(R.string.pref_notification_motivation_alert_texts), defaultStringSet);
        motivationTexts = new ArrayList<>(Arrays.asList(stringSet.toArray(new String[stringSet.size()])));

        this.mAdapter.setItems(motivationTexts);
        if (motivationTexts.size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Stores the motivation texts to shared preferences
     */
    protected void save() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(this.getString(R.string.pref_notification_motivation_alert_texts), new HashSet<>(motivationTexts));
        editor.apply();
    }

    /**
     * Shows the edit/creation dialog to user.
     *
     * @param position if it's an update give the position in array of the element which should be updated else null
     */
    protected void showEditDialog(final Integer position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MotivationAlertTextsActivity.this, R.style.AppTheme_Dialog);
        final EditText edittext = new EditText(MotivationAlertTextsActivity.this);
        if (position != null) {
            edittext.setText(motivationTexts.get(position));
        }
        alert.setMessage(getString(R.string.motivation_alert_input_message));
        alert.setTitle(getString(R.string.motivation_alert_input_title));
        alert.setView(edittext);
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
                String text = edittext.getText().toString();
                if (text.trim().isEmpty()) {
                    Toast.makeText(MotivationAlertTextsActivity.this, getString(R.string.motivation_alert_input_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (position == null) {
                    motivationTexts.add(text);
                    mAdapter.setItems(motivationTexts);
                    mAdapter.notifyItemInserted(motivationTexts.size() - 1);
                } else {
                    motivationTexts.set(position, text);
                    mAdapter.notifyItemChanged(position);
                }
                save();
                if (motivationTexts.size() == 1 && position == null) {
                    // force view update to hide "empty"-message
                    showMotivationAlertTexts();
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
    protected void removeText(int position) {
        mAdapter.removeItem(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, motivationTexts.size() - 1);
        save();
        if (motivationTexts.size() == 0) {
            // if no text exists, show default view.
            showMotivationAlertTexts();
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
        removeText(position);
    }
}
