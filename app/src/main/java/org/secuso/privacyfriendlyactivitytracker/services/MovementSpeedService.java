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
package org.secuso.privacyfriendlyactivitytracker.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;
import org.secuso.privacyfriendlyactivitytracker.R;
import org.secuso.privacyfriendlyactivitytracker.activities.MainActivity;
import org.secuso.privacyfriendlyactivitytracker.utils.UnitHelper;

import static org.secuso.privacyfriendlyactivitytracker.services.AbstractStepDetectorService.CHANNEL_ID;


/**
 * @author Tobias Neidig
 * @version 20160810
 */

public class MovementSpeedService extends JobIntentService implements LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Broadcast action identifier for messages broadcasted when new speed is detected
     */
    public static final String BROADCAST_ACTION_SPEED_CHANGED = "org.secuso.privacyfriendlystepcounter.SPEED_CHANGED";
    /**
     * Extra key for current speed
     */
    public static final String EXTENDED_DATA_CURRENT_SPEED = "org.secuso.privacyfriendlystepcounter.CURRENT_SPEED";

    private static final String LOG_TAG = MovementSpeedService.class.getName();
    private final IBinder mBinder = new MovementSpeedBinder();
    private LocationManager mLocationManager;
    private Float speed;
    private double curTime = 0;
    private double oldLat = 0.0;
    private double oldLon = 0.0;
    private int MOVEMENT_NOTIFICATION_ID = 43;
    private NotificationManager mNotifyManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public MovementSpeedService() {
        super();
    }

    @Override
    public void onLocationChanged(final Location location) {
        //your code here
        Log.i(LOG_TAG, "Location changed");
        calculateSpeed(location);
        mNotifyManager.notify(MOVEMENT_NOTIFICATION_ID, buildNotification());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onCreate() {
        createNotificationChannel();
        startForeground(MOVEMENT_NOTIFICATION_ID, buildNotification());
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private Notification buildNotification(){
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder mBuilder = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            mBuilder.setOnlyAlertOnce(true);
        } else {
            mBuilder = new NotificationCompat.Builder(this);
        }
                mBuilder.setSmallIcon(R.drawable.ic_stat_directions_walk);
        if (speed !=null){
            mBuilder.setContentTitle(UnitHelper.formatKilometersPerHour(UnitHelper.metersPerSecondToKilometersPerHour(speed),getApplicationContext()));
        }
        mBuilder.setSilent(true);
        mBuilder.setContentIntent(pIntent);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        return mBuilder.build();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Destroying MovementSpeedService.");
        this.mLocationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull @NotNull Intent intent) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Starting MovementSpeedService.");
        String providerName = getProviderName();
        Log.i(LOG_TAG, "Using " + providerName + " as location provider");
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                providerName != null) {
            mLocationManager.requestLocationUpdates(providerName, 0, 0, this);
        }
        return START_STICKY;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Detect changes on preferences and update our internal variable
        if (key.equals(getString(R.string.pref_daily_step_goal))) {

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    /**
     * @return Name of location provider
     */
    private String getProviderName() {
        Criteria criteria = new Criteria();
        criteria.setSpeedRequired(true);
        return mLocationManager.getBestProvider(criteria, true);
    }

    /**
     * Calculates the speed from current location object.
     * If location has speed, this one will be used else the speed will be calculated from current
     * and last position.
     */
    private void calculateSpeed(Location location){
        double newTime= System.currentTimeMillis();
        double newLat = location.getLatitude();
        double newLon = location.getLongitude();
        if(location.hasSpeed()){
            Log.i(LOG_TAG, "Location has speed");
            speed = location.getSpeed();
        } else {
            Log.i(LOG_TAG, "Location has no speed");
            double distance = calculationBydistance(newLat,newLon,oldLat,oldLon);
            double timeDifferent = (newTime - curTime) / 1000; // seconds
            speed = (float) (distance / timeDifferent);
            curTime = newTime;
            oldLat = newLat;
            oldLon = newLon;
        }

        Intent localIntent = new Intent(BROADCAST_ACTION_SPEED_CHANGED)
                // Add new step count
                .putExtra(EXTENDED_DATA_CURRENT_SPEED, speed);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Log.i(LOG_TAG, "New speed is " + speed + "m/sec " + speed * 3.6 + "km/h" );
    }

    /**
     * Calculates the distance between two lat/lon-pairs.
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return Distance in meters
     */
    private double calculationBydistance(double lat1, double lon1, double lat2, double lon2){
        double radius = 6371000.785;//EARTH_RADIUS;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return radius * c;
    }

    /**
     * Class used for the client Binder.
     *
     * @author Tobias Neidig
     * @version 20170530
     */
    public class MovementSpeedBinder extends Binder {
        public Float getSpeed() { return MovementSpeedService.this.speed; } // TODO
        public MovementSpeedService getService() {
            return MovementSpeedService.this;
        }
    }

    void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name_long);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
