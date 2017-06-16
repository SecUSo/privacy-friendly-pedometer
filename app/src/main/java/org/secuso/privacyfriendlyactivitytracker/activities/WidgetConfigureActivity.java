package org.secuso.privacyfriendlyactivitytracker.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.receivers.WidgetReceiver;

public class WidgetConfigureActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static int widgetId;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            setContentView(R.layout.widget_config);
            setFinishOnTouchOutside(true);

            RadioButton stepsButton = (RadioButton) findViewById(R.id.stepsRadioButton);
            RadioButton distanceButton = (RadioButton) findViewById(R.id.distanceRadioButton);
            RadioButton caloriesButton = (RadioButton) findViewById(R.id.caloriesRadioButton);
            Button saveButton = (Button) findViewById(R.id.save);
            stepsButton.setOnCheckedChangeListener(this);
            distanceButton.setOnCheckedChangeListener(this);
            caloriesButton.setOnCheckedChangeListener(this);
            saveButton.setOnClickListener(this);

            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            final Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            setResult(RESULT_OK, resultValue);
        } else {
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        int value = -1;
        if(isChecked){
            switch (buttonView.getId()){
                case R.id.stepsRadioButton:
                    value = WidgetReceiver.DATA_SET_STEPS;
                    break;
                case R.id.distanceRadioButton:
                    value = WidgetReceiver.DATA_SET_DISTANCE;
                    break;
                case R.id.caloriesRadioButton:
                    value = WidgetReceiver.DATA_SET_CALORIES;
                    break;
            }
            editor.putInt(getString(R.string.pref_widget_data_set) + widgetId, value);
            editor.apply();
            WidgetReceiver.forceWidgetUpdate(this);
        }
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}