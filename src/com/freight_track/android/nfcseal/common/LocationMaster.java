package com.freight_track.android.nfcseal.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class LocationMaster {
    private static final String TAG = "LocationMaster";
    private static LocationMaster sLocationMaster;

    private Context mAppContext;

    private LocationManager mLocationManager;
    private LocationClient mLocationClient;

    private String mLastCoordinate;
    private String mAddress;

    public static final String ACTION_LOCATION = "com.freight_track.android.nfcseal.ACTION_LOCATION";

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getLastCoordinate() {
        return mLastCoordinate;
    }

    public void setLastCoordinate(String lastCoordinate) {
        mLastCoordinate = lastCoordinate;
    }

    private LocationMaster(Context appContext) {
        mAppContext = appContext;

        if (Utils.CheckChineseLanguage()) {
            LocationClientOption option = new LocationClientOption();
            option.setIsNeedAddress(true);
            mLocationClient = new LocationClient(mAppContext);
            mLocationClient.setLocOption(option);
        }
        else {
            mLocationManager = (LocationManager) mAppContext.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public static LocationMaster get(Context c) {
        if (sLocationMaster == null) {
            // we use the application context to avoid leaking activities
            sLocationMaster = new LocationMaster(c.getApplicationContext());
        }
        return sLocationMaster;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }

    public void startLocationUpdates(BDLocationListener bdLocationListener) {

        if (Utils.CheckChineseLanguage()) {
            mLocationClient.registerLocationListener(bdLocationListener);
            mLocationClient.start();
        } else {
            String provider = LocationManager.GPS_PROVIDER;

            // get the last known location and broadcast it if we have one
            Location lastKnown = mLocationManager.getLastKnownLocation(provider);

            if (lastKnown == null) {
                Log.i(TAG, "Not got last known location from GPS");

                lastKnown = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (lastKnown == null) {
                    Log.i(TAG, "Not got last known location from Network");
                } else {
                    Log.i(TAG, "Got last known location from Network");
                }
            } else {
                Log.i(TAG, "Got last known location from GPS");
            }

            if (lastKnown != null) {
                setLastCoordinate(String.format("%1$f,%2$f", lastKnown.getLatitude(), lastKnown.getLongitude()));
                broadcastLocation(lastKnown);
            }

            // start updates from the location manager
            PendingIntent pi = getLocationPendingIntent(true);
            mLocationManager.requestLocationUpdates(provider, 60000, 0, pi);
        }
    }

    public void stopLocationUpdates() {
        if (Utils.CheckChineseLanguage()) {
            if (mLocationClient != null) {
                mLocationClient.stop();
                mLocationClient = null;
            }
        }
        else {
            PendingIntent pi = getLocationPendingIntent(false);
            if (pi != null) {
                mLocationManager.removeUpdates(pi);
                pi.cancel();
            }
        }
    }

    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }

    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mAppContext.sendBroadcast(broadcast);
    }
}
