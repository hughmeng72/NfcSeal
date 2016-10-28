package com.freight_track.android.nfcseal.fragment;

import java.util.Date;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.common.Utils;
import com.freight_track.android.nfcseal.common.LocationMaster;
import com.freight_track.android.nfcseal.common.LocationReceiver;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

public class LocationDiagnosisFragment extends Fragment {

    private static final String TAG = "lDiagnosisFragment";
    private static final String BACKGROUND_COLOR = "#ADD8E6";

    private EditText mCoordinateEditText;
    private EditText mPlaceEditText;
    private EditText mResultEditText;
    private TextView mLogTextView;
    private Button mDiagnoseButton;
    private Button mShowMapButton;

    private String mLastLocation;
    private LocationMaster mLocationMaster;
    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {

        @Override
        protected void onLocationReceived(Context context, Location loc) {
            Log.d(TAG, loc.toString());
            mLastLocation = String.format("%1$f,%2%f", loc.getLatitude(), loc.getLongitude());

            Log.i(TAG, getString(R.string.words_missed_address_prefix));

            Toast.makeText(getActivity(), getString(R.string.words_missed_address_prefix) + mLastLocation  + getString(R.string.words_missed_address_suffix), Toast.LENGTH_SHORT).show();

            ReverseGeocodingTask task = new ReverseGeocodingTask();
            task.execute(mLastLocation);

            processLocation();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationMaster = LocationMaster.get(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_location_diagnose, parent, false);

        mCoordinateEditText = (EditText) v.findViewById(R.id.coordinateEditText);
        mPlaceEditText = (EditText) v.findViewById(R.id.placeEditText);
        mResultEditText = (EditText) v.findViewById(R.id.resultEditText);

        mLogTextView = (TextView) v.findViewById(R.id.logTextView);
        mLogTextView.setMovementMethod(new ScrollingMovementMethod());

        mDiagnoseButton = (Button) v.findViewById(R.id.diagnoseButton);
        mDiagnoseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDiagnoseButton.getText().equals("Start")) {
                    appendTextAndScroll("Start location request service");

                    if (!Utils.CheckChineseLanguage()){
                        mLocationMaster.startLocationUpdates(mLocationListener);
                    }

                    mDiagnoseButton.setText("Stop");
                } else {
                    appendTextAndScroll("Stop location request service");
                    mLocationMaster.stopLocationUpdates();
                    mDiagnoseButton.setText("Start");
                }
            }

        });

        mShowMapButton = (Button) v.findViewById(R.id.showMapButton);
        mShowMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetAdjustedCoordinateTask task = new GetAdjustedCoordinateTask();
                task.execute(mLastLocation);
            }
        });

        // Hide the button of Show Map
        mShowMapButton.setVisibility(View.GONE);

        setupActionBar();

        appendTextAndScroll("Getting hisotry location");
        mLastLocation = mLocationMaster.getLastCoordinate();
        mPlaceEditText.setText(mLocationMaster.getAddress());

        if (mLastLocation != null && (mLocationMaster.getAddress() == null || mLocationMaster.getAddress().isEmpty())) {
            ReverseGeocodingTask task = new ReverseGeocodingTask();
            task.execute(mLastLocation);
        }

        processLocation();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().registerReceiver(mLocationReceiver, new IntentFilter(LocationMaster.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mLocationReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocationMaster.stopLocationUpdates();
    }

    private void appendTextAndScroll(String text) {
        if (mLogTextView != null) {

            mLogTextView.append(DateFormat.format("MM-dd HH:mm:ss", new Date()).toString() + " " + text + "\n");

            final Layout layout = mLogTextView.getLayout();

            if (layout != null) {
                int scrollDelta = layout.getLineBottom(mLogTextView.getLineCount() - 1)
                        - mLogTextView.getScrollY() - mLogTextView.getHeight();

                if (scrollDelta > 0)
                    mLogTextView.scrollBy(0, scrollDelta);
            }
        }
    }

    @SuppressLint("NewApi")
    private void setupActionBar() {

        ActionBar actionBar = getActivity().getActionBar();

        actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.label_position_diagnose)));
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actionBar.setIcon(R.drawable.ic_company);
        }
    }

    private void processLocation() {
        if (mLastLocation == null) {
            mCoordinateEditText.setText("N/A");
            mPlaceEditText.setText("N/A");

            mResultEditText.setText("Location Not Acquired");

            appendTextAndScroll("Not got a location");
        } else {
            mCoordinateEditText.setText(mLastLocation);

            mResultEditText.setText("Location Acquired");

            mLocationMaster.setLastCoordinate(mLastLocation);

            appendTextAndScroll("Got a location");
        }
    }

    private class ReverseGeocodingTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "ReverseGeocodingTask doInBackground");

            return performReverseGeocodingTask(params[0]);
        }

        private String performReverseGeocodingTask(String location) {
            GeocodingResult[] results;

            GeoApiContext contextG = new GeoApiContext().setApiKey("AIzaSyAp2aNol3FhJypghIA2IUZIOkNTwo6YPbY");
            try {
                results = GeocodingApi.geocode(contextG, location).await();

                Log.d(TAG, results[0].formattedAddress);
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }

            return results[0].formattedAddress;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "ReverseGeocodingTask onPostExecute Result: " + result);

            try {
                if (result != null) {
                    mLocationMaster.setAddress(result);
                    mPlaceEditText.setText(result);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute...");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "ReverseGeocodingTask onProgressUpdate");
        }
    }

    private class GetAdjustedCoordinateTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "GetAdjustedCoordinateTask doInBackground");

            return performGetAdjustedCoordinateTask(params[0]);
        }

        private String performGetAdjustedCoordinateTask(String coordinate) {

            SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfGetAdjustedCoordinate());

            request.addProperty(Utils.newPropertyInstance("coordinate", coordinate, String.class));

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(Utils.getWsUrl());

            String responseJSON = null;

            try {
                transport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfGetAdjustedCoordinate(), envelope);

                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

                responseJSON = response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseJSON;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "GetAdjustedCoordinateTask onPostExecute Result: " + result);

            if (result != null) {
                String[] latLng = result.split(",");

                if (latLng.length != 2) {
                    Toast.makeText(getActivity(), "Coordinate Parsing Error: lat/lng missed match.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!Utils.isDouble(latLng[0])) {
                    Toast.makeText(getActivity(), "Coordinate Parsing Error: Latitude type mismatched.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!Utils.isDouble(latLng[1])) {
                    Toast.makeText(getActivity(), "Coordinate Parsing Error: Longitude type mismatched.", Toast.LENGTH_LONG).show();
                    return;
                }

//                Intent i = new Intent(getActivity(), MapOptionActivity.class);
//                i.putExtra(MapOptionActivity.EXTRA_LOCATION_LAT, Double.parseDouble(latLng[0]));
//                i.putExtra(MapOptionActivity.EXTRA_LOCATION_LNG, Double.parseDouble(latLng[1]));

//                startActivity(i);
            } else {
                Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "GetAdjustedCoordinateTask onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "GetAdjustedCoordinateTask onProgressUpdate");
        }
    }

}
