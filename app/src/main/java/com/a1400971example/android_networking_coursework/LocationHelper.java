package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.util.Log;

import java.security.Permission;
import java.security.Permissions;

/**
 * Created by Matthew Owens on 06/03/16.
 */
public class LocationHelper extends Service implements LocationListener {

    private final Context context;
    private static final String TAG = "LocationHelper";
    private static final float MIN_DISTANCE = 5;        // Minimum distance to update in meters
    private static final long MIN_TIME_MS = 1000;       // Minimum time to between updates in ms
    private static final int FINE_ACCESS_REQUEST = 1337;
    private boolean GPSEnabled = false;
    private boolean canGetLocation = false;
    private int fineLocationPermission;

    private Location location;


    // Defaulting the lat & long to -1 so we can see if something's gone wrong later on
    private double latitude = -1;
    private double longtitude = -1;

    protected LocationManager locationManager;

    public LocationHelper(Context context) {
        this.context = context;
        //checkPermission((Activity)context);     // TODO: Fix this quick hack, if needed
        getLocation();
    }

    /*public void checkPermission(Activity currentActivity) {
        int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: check if we should show a notification
            // Requesting permission
            ActivityCompat.requestPermissions(currentActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_ACCESS_REQUEST);

        }
    }*/

    public Location getLocation() {
        // Requesting the location service
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        // Getting device info
        GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        canGetLocation = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //int hasFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        if (!GPSEnabled && !canGetLocation && fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Can't resolve location!");
        }
        // Trying to get location over the internet first
        else if (canGetLocation) {
            Log.i(TAG, "Resolving over the internet");

            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_MS, MIN_DISTANCE, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else Log.i(TAG, "Location manager is null!");

            if (location != null) {
                latitude = location.getLatitude();
                longtitude = location.getLongitude();
            } else Log.i(TAG, "location returned null!");

        }
        // Trying GPS if there's no connection
        else {
            Log.i(TAG, "Resolving with GPS");

            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_MS, MIN_DISTANCE, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else Log.i(TAG, "Location manager is null!");

            if (location != null) {
                latitude = location.getLatitude();
                longtitude = location.getLongitude();
            } else Log.i(TAG, "location returned null!");

        }

        return location;
    }

    public void disableGPS() {
        //int hasFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        if (locationManager != null && fineLocationPermission == PackageManager.PERMISSION_GRANTED)
            locationManager.removeUpdates(LocationHelper.this);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public boolean canGetLocation() {
        return canGetLocation;
    }

    //@Override
    public void onRequestPermissionResult(int requestCode, String permissions[], int grantResults[])
    {
        Log.i(TAG, "permission request result");
        if(requestCode == FINE_ACCESS_REQUEST)
        {
            // Checking to see if the request was cancelled / denied
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                fineLocationPermission = PackageManager.PERMISSION_GRANTED;
            } else fineLocationPermission = PackageManager.PERMISSION_DENIED;
        }

    }

    @Override
    public void onLocationChanged(Location location) {

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
