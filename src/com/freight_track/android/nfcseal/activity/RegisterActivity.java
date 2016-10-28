package com.freight_track.android.nfcseal.activity;

import android.app.Fragment;

import com.freight_track.android.nfcseal.fragment.RegisterFragment;

public class RegisterActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new RegisterFragment();
		
	}
	
}
