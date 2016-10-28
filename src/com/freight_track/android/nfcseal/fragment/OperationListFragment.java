package com.freight_track.android.nfcseal.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.freight_track.android.nfcseal.common.PictureUtils;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.common.ThumbnailDownloader;
import com.freight_track.android.nfcseal.model.User;
import com.freight_track.android.nfcseal.common.Utils;
import com.freight_track.android.nfcseal.model.WsResult;
import com.freight_track.android.nfcseal.model.WsResultOperation;
import com.freight_track.android.nfcseal.activity.webActivity;
import com.google.gson.Gson;

public class OperationListFragment extends ListFragment {

	private static final String BACKGROUND_COLOR = "#0FC4D9";

	public static final String EXTRA_CARRIAGE_ID = "com.freight_track.android.nfcseal.carriage_id";

	public static final String EXTRA_CARRIAGE_NO = "com.freight_track.android.nfcseal.carriage_no";

	private static String TAG = "OperationListFragment";
	private ArrayList<WsResultOperation> mOperations;

	ThumbnailDownloader<ImageView> mThumbnailThread;

	public static OperationListFragment newInstance(int carriageId, String carriageNo) {
		Bundle args = new Bundle();
		args.putInt(EXTRA_CARRIAGE_ID, carriageId);
		args.putString(EXTRA_CARRIAGE_NO, carriageNo);

		OperationListFragment fragment = new OperationListFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupActionBar();

		Log.d(TAG, User.get().getTOKEN());

		mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
			public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {

				try {
					File f = PictureUtils.createImageFile(null);
					String photoPath = f.getAbsolutePath();

					FileOutputStream fOut = new FileOutputStream(f);

					thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					StrictMode.setThreadPolicy(policy);

					fOut.flush();
					fOut.close();
					imageView.setTag(photoPath);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (isVisible()) {
					imageView.setScaleType(ScaleType.CENTER);
					imageView.setImageBitmap(thumbnail);
				}
			}
		});
		mThumbnailThread.start();
		mThumbnailThread.getLooper();

		if (Utils.isNetworkConnected(getActivity())) {
			int carriageId = getArguments().getInt(EXTRA_CARRIAGE_ID);
			Log.d(TAG, "carriageId: " + String.valueOf(carriageId));

			OperationLoadTask task = new OperationLoadTask();
			task.execute(carriageId);
		} 
		else {
			Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressLint("NewApi")
	private void setupActionBar() {

		ActionBar actionBar = getActivity().getActionBar();

		String carriageNo = getArguments().getString(EXTRA_CARRIAGE_NO);

		actionBar.setTitle(Utils.getFormatedTitle(carriageNo));
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			actionBar.setIcon(R.drawable.ic_company);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mThumbnailThread != null) {
			mThumbnailThread.quit();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (mThumbnailThread != null) {
			mThumbnailThread.clearQueue();
		}
	}

	private class OperationAdapter extends ArrayAdapter<WsResultOperation> {

		public OperationAdapter(ArrayList<WsResultOperation> operations) {
			super(getActivity(), android.R.layout.simple_list_item_1, operations);
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_operation, null);
			}

			final WsResultOperation operation = getItem(position);

			TextView oprTimeLabelTextView = (TextView) convertView.findViewById(R.id.operationTimeLabelTextView);
			oprTimeLabelTextView.setText(operation.getSealOperateDesc(getActivity()) + getString(R.string.label_time));

			TextView oprTimeTextView = (TextView) convertView.findViewById(R.id.operationTimeTextView);
			oprTimeTextView.setText(operation.getOperateTime());

			TextView oprPlaceLabelTextView = (TextView) convertView.findViewById(R.id.operationPlaceLabelTextView);
			oprPlaceLabelTextView.setText(operation.getSealOperateDesc(getActivity()) + getString(R.string.label_place));

			TextView oprPlaceTextView = (TextView) convertView.findViewById(R.id.operationPlaceTextView);
			oprPlaceTextView.setText(Html.fromHtml("<u>" + operation.getPlace() + "</u>"));

			if (operation.getPlace() != null && !operation.getPlace().isEmpty()) {
				oprPlaceTextView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getActivity(), webActivity.class);
						
						Display display = getActivity().getWindowManager().getDefaultDisplay();
						
						Log.i(TAG, "Mobile device width = " + String.valueOf(display.getWidth() + ", height = " + String.valueOf(display.getHeight())));
						
						String url;
						
						if (Utils.getCurrentLanguage().equals("zh-CN")) {
							if (display.getWidth() == 1080) {
								
								if (display.getHeight() == 1920) {
									url = String.format("http://www.freight-track.com/baidumap/NFCPoint.aspx?Coordinate=%s&mobileWidth=%d&mobileHight=%d&Token=%s", operation.getCoordinate(), 343, 597, User.get().getTOKEN());
								}
								else {
									url = String.format("http://www.freight-track.com/baidumap/NFCPoint.aspx?Coordinate=%s&mobileWidth=%d&mobileHight=%d&Token=%s", operation.getCoordinate(), 343, 550, User.get().getTOKEN());
								}
							}
							else {
								url = String.format("http://www.freight-track.com/baidumap/NFCPoint.aspx?Coordinate=%s&mobileWidth=%d&mobileHight=%d&Token=%s", operation.getCoordinate(), display.getWidth(), display.getHeight(), User.get().getTOKEN());
							}
						}
						else {
							if (display.getWidth() == 1080) {
								
								if (display.getHeight() == 1920) {
									url = String.format("http://dev.freight-track.com/baidumap/NFCPoint_google.aspx?mapCenter=%s&mobileWidth=%d&mobileHight=%d&Token=%s", operation.getCoordinate(), 343, 597, User.get().getTOKEN());
								}
								else {
									url = String.format("http://dev.freight-track.com/baidumap/NFCPoint_google.aspx?mapCenter=%s&mobileWidth=%d&mobileHight=%d&Token=%s", operation.getCoordinate(), 343, 550, User.get().getTOKEN());
								}
							}
							else {
								url = String.format("http://dev.freight-track.com/baidumap/NFCPoint_google.aspx?mapCenter=%s&mobileWidth=%d&mobileHight=%d&Token=%s", operation.getCoordinate(), display.getWidth(), display.getHeight(), User.get().getTOKEN());
							}
						}

						i.putExtra(webFragment.EXTRA_OPERATION_ID, url);
						startActivity(i);
						
//						// Call Android AMap SDK, Not in use yet. 3/26/2015
//						GetAdjustedCoordinateTask task = new GetAdjustedCoordinateTask();
//						task.execute(operation.getCoordinate());
					}
				});
			}
			
			LinearLayout expContainer = (LinearLayout)convertView.findViewById(R.id.exceptionLinearLayout);
			
			if (operation.isExceptional()) {
				TextView exceptionTextView = (TextView)convertView.findViewById(R.id.exceptionReasonTextView);
				exceptionTextView.setText(operation.getExceptionCause());
			}
			else {
				expContainer.setVisibility(View.GONE);
			}

			TextView operatorLabelTextView = (TextView) convertView.findViewById(R.id.operatorLabelTextView);
			operatorLabelTextView.setText(getActivity().getString(R.string.operator));

			TextView operatorTextView = (TextView) convertView.findViewById(R.id.operatorTextView);
			operatorTextView.setText(operation.getOperator());

			ImageView picImageView1 = (ImageView) convertView.findViewById(R.id.pictureImageView1);
			ImageView picImageView2 = (ImageView) convertView.findViewById(R.id.pictureImageView2);
			ImageView picImageView3 = (ImageView) convertView.findViewById(R.id.pictureImageView3);

			View.OnClickListener showImageListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v.getTag() != null) {
						FragmentManager fm = getActivity().getFragmentManager();
						ImageFragment.newInstance(v.getTag().toString()).show(fm, "DIALOG_IMAGE");
					}
				}
			};

			picImageView1.setOnClickListener(showImageListener);
			picImageView2.setOnClickListener(showImageListener);
			picImageView3.setOnClickListener(showImageListener);

			String imageNames[] = operation.getImgNames().split(";");

			Log.d(TAG, "Image count: " + String.valueOf(imageNames.length));

			for (int i = 0; i < imageNames.length; i++) {
				String imageName = imageNames[i];

				if (imageName != null && !imageName.isEmpty()) {

					String url = "http://www.freight-track.com/image/" + imageName;
					Log.d(TAG, url);

					switch (i) {
					case 0:
						mThumbnailThread.queueThumbnail(picImageView1, url);
						break;
					case 1:
						mThumbnailThread.queueThumbnail(picImageView2, url);
						break;
					case 2:
						mThumbnailThread.queueThumbnail(picImageView3, url);
						break;
					}
				}
				else {
					switch (i) {
					case 0:
						picImageView1.setVisibility(View.GONE);
						break;
					case 1:
						picImageView2.setVisibility(View.GONE);
						break;
					case 2:
						picImageView3.setVisibility(View.GONE);
						break;
					}
				}
			}
			
			for (int i = 3; i > imageNames.length; i--) {
				switch (i) {
				case 3:
					picImageView3.setVisibility(View.GONE);
					break;
				case 2:
					picImageView2.setVisibility(View.GONE);
					break;
				case 1:
					picImageView1.setVisibility(View.GONE);
					break;
				}
			}

			return convertView;
		}
	}

	private class OperationLoadTask extends AsyncTask<Integer, Void, String> {

		@Override
		protected String doInBackground(Integer... params) {
			Log.i(TAG, "OperationLoadTask doInBackground");

			return performLoadTask(params[0]);
		}

		private String performLoadTask(int carriageId) {

			SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfOperation());

			request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
			request.addProperty(Utils.newPropertyInstance("productId", carriageId, Integer.class));
			request.addProperty(Utils.newPropertyInstance("language", Utils.getCurrentLanguage(), String.class));

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(Utils.getWsUrl());

			String responseJSON = null;

			try {
				transport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfOperation(), envelope);

				SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

				responseJSON = response.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseJSON;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "OperationLoadTask onPostExecute");
//			Log.d(TAG, result);

			try {
				Gson gson = new Gson();
				WsResult ret = gson.fromJson(result, WsResult.class);
				// Log.d(TAG, "OperationLoadTask onPostExecute result: " +
				// String.valueOf(ret.isOK()));

				if (ret.isOK()) {
					mOperations = ret.getOperations();
					// Log.d(TAG, mOperations.toString());

					updateUI();
				} else {
					Toast.makeText(getActivity(), R.string.prompt_inquiry_failed, Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Toast.makeText(getActivity(), R.string.prompt_inquiry_failed, Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}

		private void updateUI() {

			OperationAdapter adapter = new OperationAdapter(mOperations);
			setListAdapter(adapter);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i(TAG, "OperationLoadTask onProgressUpdate");
		}
	}

//	private class GetAdjustedCoordinateTask extends AsyncTask<String, Void, String> {
//
//		@Override
//		protected String doInBackground(String... params) {
//			Log.i(TAG, "GetAdjustedCoordinateTask doInBackground");
//
//			return performGetAdjustedCoordinateTask(params[0]);
//		}
//
//		private String performGetAdjustedCoordinateTask(String coordinate) {
//
//			SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfGetAdjustedCoordinate());
//
//			request.addProperty(Utils.newPropertyInstance("coordinate", coordinate, String.class));
//
//			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//			envelope.dotNet = true;
//			envelope.setOutputSoapObject(request);
//
//			HttpTransportSE transport = new HttpTransportSE(Utils.getWsUrl());
//
//			String responseJSON = null;
//
//			try {
//				transport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfGetAdjustedCoordinate(), envelope);
//
//				SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
//
//				responseJSON = response.toString();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			return responseJSON;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			Log.i(TAG, "GetAdjustedCoordinateTask onPostExecute Result: " + result);
//
//			if (result != null) {
//				String[] latLng = result.split(",");
//
//				if (latLng.length != 2) {
//					Toast.makeText(getActivity(), "Coordinate Parsing Error: lat/lng missed match.", Toast.LENGTH_LONG).show();
//					return;
//				}
//
//				if (!Utils.isDouble(latLng[0])) {
//					Toast.makeText(getActivity(), "Coordinate Parsing Error: Latitude type mismatched.", Toast.LENGTH_LONG).show();
//					return;
//				}
//
//				if (!Utils.isDouble(latLng[1])) {
//					Toast.makeText(getActivity(), "Coordinate Parsing Error: Longitude type mismatched.", Toast.LENGTH_LONG).show();
//					return;
//				}
//
//				Intent i = new Intent(getActivity(), MapOptionActivity.class);
//				i.putExtra(MapOptionActivity.EXTRA_LOCATION_LAT, Double.parseDouble(latLng[0]));
//				i.putExtra(MapOptionActivity.EXTRA_LOCATION_LNG, Double.parseDouble(latLng[1]));
//				startActivity(i);
//			} else {
//				Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
//			}
//		}
//
//		@Override
//		protected void onPreExecute() {
//			Log.i(TAG, "GetAdjustedCoordinateTask onPreExecute");
//		}
//
//		@Override
//		protected void onProgressUpdate(Void... values) {
//			Log.i(TAG, "GetAdjustedCoordinateTask onProgressUpdate");
//		}
//	}

}
