package com.freight_track.android.nfcseal.activity;

import android.app.Fragment;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.common.NotiftLocationListener;
import com.freight_track.android.nfcseal.common.Utils;
import com.freight_track.android.nfcseal.fragment.SignInFragment;

public class SignInActivity extends SingleFragmentActivity {

    private LocationClient mLocationClient;
    private NotiftLocationListener mLocationListener;

    @Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new SignInFragment();
	}

    @Override
    public void onNewIntent(Intent intent) {
        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            SignInFragment fragment = (SignInFragment)getFragmentManager().findFragmentById(R.id.fragmentContainer);

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            fragment.gotTag(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID), tag);
        }
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

        initLocationClient();
	}

    // Initialize Baidu Location Client
    private void initLocationClient() {
        mLocationListener = new NotiftLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                super.onReceiveLocation(location);

                Toast.makeText(getApplicationContext(), String.format("Location: %1$s (%2$f, %3$f)", location.getAddress().address, location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
            }
        };

        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setLocOption(option);
    }

	@Override
	protected void onStart() {
		super.onStart();

        if (Utils.CheckChineseLanguage()) {
            mLocationClient.registerLocationListener(mLocationListener);
            mLocationClient.start();
        }
	}

    @Override
    protected void onStop() {
        super.onStop();

        if (mLocationClient != null) {
            mLocationClient.stop();
            mLocationClient = null;
        }

        mLocationListener = null;
    }
}
