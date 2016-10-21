package com.freight_track.android.nfcseal;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class MainFragment extends Fragment {

	private static final String BACKGROUND_COLOR = "#4682B4";

	public enum ActionEnum {
		lock, unlock, inquiry, signIn, aboutMe, more
	}

	public interface Callbacks {
		public void inAction(View v);
	}

	private Callbacks mCallbacks;

	private ImageButton mLockImageButton;
	private ImageButton mUnlockImageButton;
	private ImageButton mInquiryImageButton;
	private ImageButton mSigninImageButton;
	private ImageButton mAboutMeImageButton;
	private ImageButton mMoreImageButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_main, parent, false);

		mLockImageButton = (ImageButton) v
				.findViewById(R.id.fragment_main_lockImageButton);
		mUnlockImageButton = (ImageButton) v
				.findViewById(R.id.fragment_main_unlockImageButton);
		mInquiryImageButton = (ImageButton) v
				.findViewById(R.id.fragment_main_inquiryImageButton);
		mSigninImageButton = (ImageButton) v
				.findViewById(R.id.fragment_main_signinImageButton);
		mAboutMeImageButton = (ImageButton) v
				.findViewById(R.id.fragment_main_aboutMeImageButton);
		mMoreImageButton = (ImageButton) v
				.findViewById(R.id.fragment_main_moreImageButton);

		View.OnClickListener inActionListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallbacks.inAction(v);
			}
		};

		mLockImageButton.setOnClickListener(inActionListener);
		mUnlockImageButton.setOnClickListener(inActionListener);
		mInquiryImageButton.setOnClickListener(inActionListener);
		mSigninImageButton.setOnClickListener(inActionListener);
		mAboutMeImageButton.setOnClickListener(inActionListener);
		mMoreImageButton.setOnClickListener(inActionListener);
		
		setupActionBar();

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallbacks = (Callbacks) activity;
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
		mCallbacks = null;
	}

	@SuppressLint("NewApi")
	private void setupActionBar() {

		ActionBar actionBar = getActivity().getActionBar();

		actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.prompt_main)));
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			actionBar.setIcon(R.drawable.ic_company);
		}

	}


}
