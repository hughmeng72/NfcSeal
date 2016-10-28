package com.freight_track.android.nfcseal.activity;

import android.app.Fragment;

import com.freight_track.android.nfcseal.fragment.OperationListFragment;


public class OperationListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
    	int carriageId = getIntent().getIntExtra(OperationListFragment.EXTRA_CARRIAGE_ID, -1);
    	String carriageNo = getIntent().getStringExtra(OperationListFragment.EXTRA_CARRIAGE_NO);
    	
        return OperationListFragment.newInstance(carriageId, carriageNo);
    }

}
