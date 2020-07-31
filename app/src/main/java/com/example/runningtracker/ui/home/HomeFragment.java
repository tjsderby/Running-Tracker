package com.example.runningtracker.ui.home;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.runningtracker.MyProviderContract;
import com.example.runningtracker.R;
import com.example.runningtracker.TrackerService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener {
    private Button btnStart, btnStop;
    private TextView timer;
    private BroadcastReceiver broadcastReceiver;
    private GoogleMap mMap;
    private MapView mapView;
    private MarkerOptions mo;
    private LatLng myCoords;
    private Marker marker;
    private LocationManager locationManager;

    @Override
    public void onResume() {
        super.onResume();
        if (broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("timer_update"))
                    {

                        // if the timer is running it hides the start button and shows the stop button
                        if (Integer.parseInt(String.valueOf(intent.getExtras().get("running"))) == 1){
                            btnStart.setVisibility(View.INVISIBLE);
                            btnStop.setVisibility(View.VISIBLE);
                        }

                        // updates text view with values from the TrackerService
                        int totalSecs = Integer.parseInt(String.valueOf(intent.getExtras().get("time")));
                        int hours = totalSecs / 3600;
                        int minutes = (totalSecs % 3600) / 60;
                        int seconds = totalSecs % 60;
                        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        timer.setText(timeString);

                    } else if(intent.getAction().equals("tracker_update"))
                    {
                        // gets final values from TrackerService and inserts them into database
                        int time = Integer.parseInt(String.valueOf(intent.getExtras().get("totalTime")));
                        float distance = Float.parseFloat(String.valueOf(intent.getExtras().get("totalDistance")));
                        float avgSpeed = Float.parseFloat(String.valueOf(intent.getExtras().get("avgSpeed")));

                        ContentValues newValues = new ContentValues();

                        newValues.put(MyProviderContract.TIME, time);
                        newValues.put(MyProviderContract.DISTANCE, distance);
                        newValues.put(MyProviderContract.AVERAGESPEED, avgSpeed);

                        getActivity().getContentResolver().insert(MyProviderContract.SESSIONS_URI, newValues);
                    }
                }
            };
        }
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("timer_update"));
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("tracker_update"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            // unregisters the receiver when the activity is destroyed
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // saving button state for persistence
        outState.putString("startVis", String.valueOf(btnStart.getVisibility()));
        outState.putString("stopVis", String.valueOf(btnStop.getVisibility()));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // restoring state
            if (savedInstanceState.getString("startVis").equals("0")) {
                btnStart.setVisibility(View.VISIBLE);
            } else {
                btnStart.setVisibility(View.INVISIBLE);
            }

            if (savedInstanceState.getString("stopVis").equals("0")) {
                btnStop.setVisibility(View.VISIBLE);
            } else {
                btnStop.setVisibility(View.INVISIBLE);
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // finds maps view and starts the map using the mark up options defined
        mapView = root.findViewById(R.id.map);
        if (mapView != null)
        {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My Current Location");

        btnStart = root.findViewById(R.id.btnStart);
        btnStop = root.findViewById(R.id.btnStop);
        timer = root.findViewById(R.id.textViewTimer);

        // checks if the permissions are enabled and if they are the location listener and buttons are enabled
        if(!runtimePermissions())
        {
            requestLocation();
            enableButtons();
        }

        return root;
    }

    private void enableButtons() {

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // starts the TrackerService
                btnStart.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                Intent i = new Intent(getActivity(), TrackerService.class);
                getActivity().startService(i);

            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // stops the TrackerService
                btnStart.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.INVISIBLE);
                Intent i = new Intent(getActivity(), TrackerService.class);
                getActivity().stopService(i);

            }
        });

    }

    private boolean runtimePermissions() {
        // this checks if permissions are enabled
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                // enables location listener and buttons if the user allows permissions
                enableButtons();
                requestLocation();
            }else {
                // request permissions again if they do not allow the permissions
                runtimePermissions();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // initialises the map
        MapsInitializer.initialize(getContext());
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        marker = mMap.addMarker(mo);
    }

    @Override
    public void onLocationChanged(Location location) {
        // updates map marker any time the users location is changed
        myCoords = new LatLng(location.getLatitude(), location.getLongitude());
        marker.setPosition(myCoords);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoords, 18));
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

    private void requestLocation() {
        // requests users location
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5, this);
        } catch(SecurityException e) {
            Log.d("g53mdp", e.toString());
        }
    }

}