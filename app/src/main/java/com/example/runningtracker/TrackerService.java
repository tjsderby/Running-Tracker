package com.example.runningtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TrackerService extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    private List<Double> longLat = new ArrayList<Double>();
    private int time;
    private CountUpTimer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // put long and lat into an array for calculation at the end
                longLat.add(location.getLongitude());
                longLat.add(location.getLatitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        // creates location manager and gets location updates
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, listener);
        } catch(SecurityException e) {
            Log.d("g53mdp", e.toString());
        }

        timer = new CountUpTimer(30000000) {
            @Override
            public void onTick(int second) {
                // calculates time
                time =  second;

                // broadcast back to HomeFragment to update text view
                Intent intent = new Intent("timer_update");
                intent.putExtra("time", second);
                intent.putExtra("running", 1);
                sendBroadcast(intent);
            }
        };

        timer.start();
    }

    public static float getDistance(double startLat, double startLong, double goalLat, double goalLong){
        // figures out distance between two sets of long and lats
        Location locationA = new Location("point A");

        locationA.setLatitude(startLat);
        locationA.setLongitude(startLong);

        Location locationB = new Location("point B");

        locationB.setLatitude(goalLat);
        locationB.setLongitude(goalLong);

        return locationA.distanceTo(locationB);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // stops the location manager
        if(locationManager != null){
            locationManager.removeUpdates(listener);
        }
        timer.cancel();

        // finds distance, average speed and time

        float totalDistance = 0;
        int i = 0;
        int size = longLat.size();

        if (size > 2)
        {
            while (i < size) {
                if (i + 2 != size)
                {
                    double startLong = longLat.get(i);
                    double startLat = longLat.get(i + 1);
                    double goalLong = longLat.get(i + 2);
                    double goalLat = longLat.get(i + 3);

                    float distance = getDistance(startLat, startLong, goalLat, goalLong);
                    totalDistance = totalDistance + distance; // in metres
                }
                else
                {
                    // do nothing
                }
                i = i + 2;
            }
        }
        else
        {
            // do nothing
        }

        float avgSpeed = totalDistance / time;

        // broadcast "avgSpeed", "totalDistance" and "time" back to main to be put into a database
        Intent intent = new Intent("tracker_update");
        intent.putExtra("totalTime", time);
        intent.putExtra("totalDistance", totalDistance);
        intent.putExtra("avgSpeed", avgSpeed);
        sendBroadcast(intent);
    }
}
