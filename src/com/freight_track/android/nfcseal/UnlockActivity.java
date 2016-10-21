package com.freight_track.android.nfcseal;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Window;


public class UnlockActivity extends SingleFragmentActivity implements UnlockFragment.Callbacks {

	@Override
	protected Fragment createFragment() {
		return new UnlockFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			UnlockFragment fragment = (UnlockFragment)getFragmentManager().findFragmentById(R.id.fragmentContainer);
			
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			
			fragment.gotTag(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID), tag);
		}
	}

	@Override
	public void onAdhocUnlockRequest() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		Fragment oldFragment = fm.findFragmentById(R.id.fragmentContainer);
		Fragment newFragment = new UnlockAdhocFragment();

		if (oldFragment != null) {
			ft.remove(oldFragment);
		}

		ft.add(R.id.fragmentContainer, newFragment);

		ft.commit();
		
	}

}
