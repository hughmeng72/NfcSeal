package com.freight_track.android.nfcseal;

import android.app.Fragment;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Window;


public class SignInActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new SignInFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			SignInFragment fragment = (SignInFragment)getFragmentManager().findFragmentById(R.id.fragmentContainer);
			
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			
			fragment.gotTag(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID), tag);
		}
	}

}
