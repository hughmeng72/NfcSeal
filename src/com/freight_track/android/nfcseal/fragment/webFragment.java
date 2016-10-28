package com.freight_track.android.nfcseal.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.freight_track.android.nfcseal.R;

public class webFragment extends Fragment {

	public static final String EXTRA_OPERATION_ID = "com.freight_track.android.nfcseal.operation_id";

	public static webFragment newInstance(String url) {
		
		Bundle args = new Bundle();
		args.putString(EXTRA_OPERATION_ID, url);
		
		webFragment fragment = new webFragment();
		fragment.setArguments(args);
		
		return fragment;
	}
	
//	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceStatte) {
		
		View v = inflater.inflate(R.layout.fragment_web, parent, false);
		
		String url = getArguments().getString(EXTRA_OPERATION_ID);
		
		WebView webView = (WebView)v.findViewById(R.id.webView);
		
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);

//		settings.setUseWideViewPort(true);
//		settings.setLoadWithOverviewMode(true);
		
//		settings.setPluginState(WebSettings.PluginState.ON);
//		settings.setSupportZoom(true);
//		settings.setBuiltInZoomControls(true);
		
		webView.loadUrl(url);
		
		
		return v;
	}

}
