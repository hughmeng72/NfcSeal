package com.freight_track.android.nfcseal;

import android.app.Fragment;


public class OperationListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
    	int carriageId = getIntent().getIntExtra(OperationListFragment.EXTRA_CARRIAGE_ID, -1);
    	String carriageNo = getIntent().getStringExtra(OperationListFragment.EXTRA_CARRIAGE_NO);
    	
        return OperationListFragment.newInstance(carriageId, carriageNo);
    }

}
