package com.freight_track.android.nfcseal;

import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;


public class CarriageListFragment extends ListFragment {

	private static final String BACKGROUND_COLOR = "#0FC4D9";

	private static String TAG = "CarriageListFragment";

	private ArrayList<WsResultCarriage> mCarriages;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setupActionBar();
		
		if (Utils.isNetworkConnected(getActivity())) {
			CarriageLoadTask task = new CarriageLoadTask();
			task.execute();
		}
		else {
			Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressLint("NewApi")
	private void setupActionBar() {

		ActionBar actionBar = getActivity().getActionBar();

		actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.prompt_inquiry)));
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			actionBar.setIcon(R.drawable.ic_company);
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		WsResultCarriage carriage = ((CarriageAdapter)getListAdapter()).getItem(position);
		
		Log.d(TAG, String.valueOf(carriage.getProductId()));
		
		Intent i = new Intent(getActivity(), OperationListActivity.class);
		i.putExtra(OperationListFragment.EXTRA_CARRIAGE_ID, carriage.getProductId());
		i.putExtra(OperationListFragment.EXTRA_CARRIAGE_NO, carriage.getProductCode());
		startActivity(i);
	}
	
	private class CarriageAdapter extends ArrayAdapter<WsResultCarriage> {
		
		public CarriageAdapter(ArrayList<WsResultCarriage> carriages) {
			super(getActivity(), 0, carriages);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_carriage, parent, false);
			}
			
			WsResultCarriage carriage = getItem(position);
			
			TextView carriageNoTextView = (TextView)convertView.findViewById(R.id.carriageNoTextView);
			if (carriage.isExceptional()) {
				carriageNoTextView.setText(Html.fromHtml("<font color=\"#FF0000\">" + carriage.getProductCode() + "</font>"));
			}
			else {
				carriageNoTextView.setText(carriage.getProductCode());
			}
			
			TextView carriageStateTextView = (TextView)convertView.findViewById(R.id.carriageStateTextView);
			if (carriage.isExceptional()) {
				carriageStateTextView.setText(Html.fromHtml("<font color=\"#FF0000\">" + carriage.getProductStateDesc(getActivity()) + "</font>"));
			}
			else {
				carriageStateTextView.setText(carriage.getProductStateDesc(getActivity()));
			}
			

			return convertView;
		}
	}

	private class CarriageLoadTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			Log.i(TAG, "CarriageLoadTask doInBackground");

			return performLoadTask();
		}

		private String performLoadTask() {

			SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfCarriage());

			request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
			request.addProperty(Utils.newPropertyInstance("language", Utils.getCurrentLanguage(), String.class));

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(Utils.getWsUrl());

			String responseJSON = null;

			try {
				transport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfCarriage(), envelope);

				SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

				responseJSON = response.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseJSON;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "CarriageLoadTask onPostExecute");
			Log.d(TAG, result);

			try {
				Gson gson = new Gson();
				CarriageLab ret = gson.fromJson(result, CarriageLab.class);

				if (ret.isOK()) {
					mCarriages = ret.getCarriages();
					Log.d(TAG, mCarriages.toArray().toString());

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
			
			CarriageAdapter adapter = new CarriageAdapter(mCarriages);
			setListAdapter(adapter);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i(TAG, "CarriageLoadTask onProgressUpdate");
		}
	}

}
