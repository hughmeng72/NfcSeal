package com.freight_track.android.nfcseal.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationMaster {
    private static final String TAG = "LocationMaster";

    private static LocationMaster sLocationMaster;

    private Context mAppContext;
    private LocationManager mLocationManager;
    private Location mKeptLocation;
    private String mAddress;

    public static final String ACTION_LOCATION = "com.freight_track.android.nfcseal.ACTION_LOCATION";

    public String getAddress() {
		return mAddress;
	}

	public void setAddress(String address) {
		mAddress = address;
	}

	public Location getKeptLocation() {
		return mKeptLocation;
	}

	public void setKeptLocation(Location keptLocation) {
		mKeptLocation = keptLocation;
	}

	private LocationMaster(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
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

    public void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;

        // get the last known location and broadcast it if we have one
        Location lastKnown = mLocationManager.getLastKnownLocation(provider);

        if (lastKnown == null) {
            Log.i(TAG, "Not got last known location from GPS");
        	
        	lastKnown = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        	
        	if (lastKnown == null) {
                Log.i(TAG, "Not got last known location from Network");
        	}
        	else {
                Log.i(TAG, "Got last known location from Network");
        	}
        }
    	else {
            Log.i(TAG, "Got last known location from GPS");
    	}
        
        if (lastKnown != null) {
        	setKeptLocation(lastKnown);
            broadcastLocation(lastKnown);
        }

        // start updates from the location manager
        PendingIntent pi = getLocationPendingIntent(true);
        mLocationManager.requestLocationUpdates(provider, 60000, 0, pi);
    }
    
    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            mLocationManager.removeUpdates(pi);
            pi.cancel();
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
