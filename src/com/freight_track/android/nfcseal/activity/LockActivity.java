package com.freight_track.android.nfcseal.activity;

import android.app.Fragment;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Window;

import com.freight_track.android.nfcseal.fragment.LockFragment;
import com.freight_track.android.nfcseal.R;

public class LockActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new LockFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			LockFragment fragment = (LockFragment) getFragmentManager()
					.findFragmentById(R.id.fragmentContainer);

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			
			fragment.gotTag(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID), tag);
}
	}

}
