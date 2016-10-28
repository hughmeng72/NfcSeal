package com.freight_track.android.nfcseal.common;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

public class NotiftLocationListener implements BDLocationListener {
    private static final String TAG = "NotiftLocationListener";

    @Override
    public void onReceiveLocation(BDLocation location) {
        String address = location.getAddress().address;
    }
}
