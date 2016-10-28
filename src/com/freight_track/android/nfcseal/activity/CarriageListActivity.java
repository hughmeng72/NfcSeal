package com.freight_track.android.nfcseal.activity;

import android.app.Fragment;

import com.freight_track.android.nfcseal.fragment.CarriageListFragment;

public class CarriageListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CarriageListFragment();
    }

}
