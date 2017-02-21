package com.freight_track.android.nfcseal.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.common.LocationMaster;
import com.freight_track.android.nfcseal.common.NotiftLocationListener;
import com.freight_track.android.nfcseal.update.UpdateHelper;
import com.freight_track.android.nfcseal.update.listener.ForceListener;

public abstract class SingleFragmentActivity extends Activity {
    protected abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .commit();
        }

        checkUpdate();
    }

    private void checkUpdate() {
        UpdateHelper.getInstance().setForceListener(new ForceListener() {
            @Override
            public void onUserCancel(boolean force) {
                if (force) {
                    finish();
                }
            }
        }).check(this);
    }}
