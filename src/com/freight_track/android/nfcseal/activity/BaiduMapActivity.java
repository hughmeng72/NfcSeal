package com.freight_track.android.nfcseal.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Window;

import com.baidu.mapapi.SDKInitializer;
import com.freight_track.android.nfcseal.fragment.BaiduMapFragment;
import com.freight_track.android.nfcseal.fragment.webFragment;
import com.freight_track.android.nfcseal.model.WsResultOperation;

import java.util.ArrayList;

public class BaiduMapActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		SDKInitializer.initialize(getApplicationContext());
		ArrayList<WsResultOperation> operations = getIntent().getParcelableArrayListExtra(BaiduMapFragment.EXTRA_OPERATIONS);
		return BaiduMapFragment.newInstance(operations);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
}