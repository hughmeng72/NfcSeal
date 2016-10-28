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
	}
}
