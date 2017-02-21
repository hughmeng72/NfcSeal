package com.freight_track.android.nfcseal.common;

import android.app.Application;

/**
 * Created by wayne on 10/11/2016.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UpdateConfig.initGet(this);
    }
}
