package com.freight_track.android.nfcseal;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.SupportMapFragment;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;


public class MapOptionActivity extends FragmentActivity {

	private static final String MAP_FRAGMENT_TAG = "map";
	protected static final String EXTRA_LOCATION_LAT = "com.freight_track.android.nfcseal.location.lat";
	protected static final String EXTRA_LOCATION_LNG = "com.freight_track.android.nfcseal.location.lng";
	private LatLng mLocation;
	
	private AMap aMap;
	private SupportMapFragment aMapFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		AMapOptions aOptions = new AMapOptions();

		aOptions.zoomGesturesEnabled(false);// ��ֹͨ���������ŵ�ͼ
		aOptions.scrollGesturesEnabled(true);// ��ֹͨ�������ƶ���ͼ
		
		double lat = getIntent().getDoubleExtra(EXTRA_LOCATION_LAT, 0);
		double lng = getIntent().getDoubleExtra(EXTRA_LOCATION_LNG, 0);
		mLocation = new LatLng(lat, lng);
		CameraPosition p = new CameraPosition.Builder().target(mLocation).zoom(18).bearing(0).tilt(0).build();
		aOptions.camera(p);
		
		if (aMapFragment == null) {
			aMapFragment = SupportMapFragment.newInstance(aOptions);
			FragmentTransaction fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			fragmentTransaction.add(android.R.id.content, aMapFragment,
					MAP_FRAGMENT_TAG);
			fragmentTransaction.commit();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		initMap();
	}

	private void initMap() {
		if (aMap == null) {
			aMap = aMapFragment.getMap();// amap�����ʼ���ɹ�
			addMarkersToMap();
		}
	}

	private void addMarkersToMap() {

		if (mLocation == null) return;
			
		aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.position(mLocation).title("Shanghai")
				.snippet("Shanghai").draggable(false));
	}

}
