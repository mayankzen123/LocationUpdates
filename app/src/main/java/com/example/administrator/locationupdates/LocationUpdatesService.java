package com.example.administrator.locationupdates;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Administrator on 4/11/2018.
 */

public class LocationUpdatesService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest;
    private final long INTERVAL = 5 * 1000;  //5 Sec.
    private final long FASTEST_INTERVAL = 2 * 1000; //3 Sec.
    private final long LOCATION_UPDATES = 20 * 1000; //20 Sec.
    Context mContext = this;
    private final int SCHEDULE_UPDATE = 111;
    private final int ENABLE_LOCATION_UPDATES = 222;
    Location location;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //1.Get Latitude and Longitude
            //2.Check If internet is On
            //3.Check permission is Granted
            //4.Check Google Play Services Installed
            //5.Then Send Location to Firebase or Server
            switch (msg.what) {
                case SCHEDULE_UPDATE:
                    if (checkPreConditions()) {
                        Log.d("Location Updates ", location.getLatitude() + " " + location.getLongitude());
                        updateLocationToServer();
                        handler.sendEmptyMessageDelayed(ENABLE_LOCATION_UPDATES, LOCATION_UPDATES);
                    } else {
                        shutDownService();
                        Log.d("Location Updates ", "Something Went wrong");
                    }
                    break;
                case ENABLE_LOCATION_UPDATES:
                    if (checkPreConditions()) {
                        enableLocationUpdates();
                    } else {
                        shutDownService();
                        Log.d("Location Updates ", "Something Went wrong");
                    }
                    break;
            }
        }
    };

    private void shutDownService() {
        //1.Disconnect Google API Client
        //2.Remove Handler
        //3.Remove Location Updates
        //4.Stop Service
        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
        handler.removeMessages(ENABLE_LOCATION_UPDATES);
        handler.removeMessages(SCHEDULE_UPDATE);
        mLocationClient.disconnect();
        stopSelf();
    }

    private void updateLocationToServer() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeGoogleApiClient();
        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }

    //Build and Connect to Google API Client
    //Listen for updates inside OnConnected and for failure inside onConnectionSuspended and onConnectionFailed
    private void initializeGoogleApiClient() {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationClient.connect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initializeLocationRequest();
    }

    private void initializeLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        enableLocationUpdates();
    }

    private void enableLocationUpdates() {
        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopSelf();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
        handler.sendEmptyMessage(SCHEDULE_UPDATE);
    }

    private boolean checkPreConditions() {
        if (Utility.checkInternetConnected(mContext) && Utility.checkGpsEnabled(mContext) && Utility.checkPermissionGranted(mContext) && Utility.checkGooglePlayServicesEnabled(mContext)) {
            return true;
        }
        return false;
    }
}
