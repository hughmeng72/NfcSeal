package com.freight_track.android.nfcseal.activity;

import android.app.Fragment;

import com.freight_track.android.nfcseal.fragment.LocationDiagnosisFragment;

public class LocationDiagnosisActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new LocationDiagnosisFragment();
		
	}
	
}
