package org.secuso.privacyfriendlyactivitytracker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.tutorial.TutorialActivity;

public class PermissionRequestActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS
    };

    int alreadyAskedForActivityRecognition = 0;
    int alreadyAskedForOther = 0;
    private boolean launchMainActivityAfterPermissionsGranted;

    void multiplePermissionsCallback(java.util.Map<String, Boolean> isGranted) {
        if (isGranted.containsValue(false)) {
            if (Boolean.FALSE.equals(isGranted.get(Manifest.permission.ACTIVITY_RECOGNITION))) {
                alreadyAskedForActivityRecognition++;
                if (alreadyAskedForActivityRecognition > 1) {
                    Log.d(TAG, "Activity recognition permission was still not granted, showing final dialog");
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.dialog_permission_activity_recognition_2)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                            .show();
                } else {
                    Log.d(TAG, "Activity recognition permission was not granted, showing dialog");
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.dialog_permission_activity_recognition)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> multiplePermissionLauncher.launch(PERMISSIONS))
                            .show();

                }
            } else {
                alreadyAskedForOther++;
                if (alreadyAskedForOther < 2) {
                    multiplePermissionLauncher.launch(PERMISSIONS);
                }
            }
        }
        if (hasActivityRecognitionPermission()) {
            if (launchMainActivityAfterPermissionsGranted) {
                launchMainActivityAfterPermissionsGranted = false;
                Log.d(TAG, "Activity recognition permission was granted, launching main activity");
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultContracts.RequestMultiplePermissions multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract, this::multiplePermissionsCallback);
        // askPermissions(multiplePermissionLauncher);
    }

    private void askPermissions(ActivityResultLauncher<String[]> multiplePermissionLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermissions(PERMISSIONS) && alreadyAskedForOther < 2 && alreadyAskedForActivityRecognition < 2) {
                Log.d(TAG, "Launching multiple contract permission launcher for ALL required permissions");
                multiplePermissionLauncher.launch(PERMISSIONS);
            } else {
                Log.d(TAG, "All permissions are already granted");
            }
        }
    }

    public void askPermissions() {
        askPermissions(multiplePermissionLauncher);
    }

    public void askPermissionsAndLaunch() {
        launchMainActivityAfterPermissionsGranted = true;
        askPermissions(multiplePermissionLauncher);
    }

    private boolean hasPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission is not granted: " + permission);
                    return false;
                }
                Log.d(TAG, "Permission already granted: " + permission);
            }
            return true;
        }
        return false;
    }

    protected boolean hasActivityRecognitionPermission() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || hasPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}));
    }
}
