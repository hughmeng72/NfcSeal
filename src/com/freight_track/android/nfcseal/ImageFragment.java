package com.freight_track.android.nfcseal;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageFragment extends DialogFragment {
	public static final String EXTRA_IMAGE_PATH = "path";

	public static ImageFragment newInstance(String imagePath) {
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_IMAGE_PATH, imagePath);

		ImageFragment fragment = new ImageFragment();
		fragment.setArguments(args);
		fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);

		return fragment;
	}

	private ImageView mImageView;

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

		mImageView = new ImageView(getActivity());

		String path = (String) getArguments().getSerializable(EXTRA_IMAGE_PATH);

		Display display = getActivity().getWindowManager().getDefaultDisplay();

		PictureUtils.showPic(mImageView, path, display.getWidth(), display.getHeight());
			
		return mImageView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		PictureUtils.cleanImageView(mImageView);
	}
}
