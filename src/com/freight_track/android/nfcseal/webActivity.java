package com.freight_track.android.nfcseal;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Window;

public class webActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		
		String url = getIntent().getStringExtra(webFragment.EXTRA_OPERATION_ID);
		return webFragment.newInstance(url);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
	}

}
