package com.freight_track.android.nfcseal.fragment;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.common.NotiftLocationListener;
import com.freight_track.android.nfcseal.model.Seal;
import com.freight_track.android.nfcseal.model.User;
import com.freight_track.android.nfcseal.common.Utils;
import com.freight_track.android.nfcseal.model.WsResult;
import com.freight_track.android.nfcseal.activity.SignInActivity;
import com.freight_track.android.nfcseal.common.LocationMaster;
import com.freight_track.android.nfcseal.common.LocationReceiver;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

public class SignInFragment extends Fragment {
    private static final String BACKGROUND_COLOR = "#FF6600";
    private static String TAG = "SignInFragment";
    private Seal mSeal;

    private LocationMaster mLocationMaster;
    private String mLastLocation;
    private String mLastAddress;

    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {
        @Override
        protected void onLocationReceived(Context context, Location loc) {
            mLastLocation = String.format("%1$f,%2$f", loc.getLatitude(), loc.getLongitude());
            mLocationMaster.setLastCoordinate(mLastLocation);

            Log.d(TAG, "Got location from GPS: " + mLastLocation);

            ReverseGeocodingTask task = new ReverseGeocodingTask();
            task.execute(mLastLocation);
        }
    };

    private NotiftLocationListener mBDLocationListener = new NotiftLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation loc) {
            super.onReceiveLocation(loc);

            mLastLocation = String.format("%1$f,%2$f", loc.getLatitude(), loc.getLongitude());

            Log.d(TAG, "Got location from Baidu: " + mLastLocation);

            mLocationMaster.setLastCoordinate(mLastLocation);
            mLocationMaster.setAddress(loc.getAddress().address);
        }
    };

    private TextView mTaggingPromptTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSeal = new Seal(getActivity().getApplicationContext());

        mLocationMaster = LocationMaster.get(getActivity());
        mLastLocation = mLocationMaster.getLastCoordinate();
        mLastAddress = mLocationMaster.getAddress();

        Log.d(TAG, User.get().getTOKEN());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_signin, parent, false);

        mTaggingPromptTextView = (TextView) v.findViewById(R.id.tagPromptTextView);

        setupActionBar();

        return v;
    }

    @SuppressLint("NewApi")
    private void setupActionBar() {

        ActionBar actionBar = getActivity().getActionBar();

        actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.prompt_signIn)));
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actionBar.setIcon(R.drawable.ic_company);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().registerReceiver(mLocationReceiver, new IntentFilter(LocationMaster.ACTION_LOCATION));
        mLocationMaster.startLocationUpdates(mBDLocationListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), getActivity()
                .getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        // filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        // filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(getActivity());
        adapter.enableForegroundDispatch(getActivity(), pi, new IntentFilter[]{filter}, Utils.getNfcTechList());
    }

    @Override
    public void onPause() {
        super.onPause();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(getActivity());
        adapter.disableForegroundDispatch(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mLocationReceiver);
        mLocationMaster.stopLocationUpdates(mBDLocationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        mLocationMaster.stopLocationUpdates(bdLocationListener);
    }

    public void gotTag(byte[] bs, Tag tag) {

        String tagId = Utils.ByteArrayToHexString(bs);
        Log.d(TAG, "NFC Tag UID: " + tagId);

        if (tagId == null || tagId.isEmpty()) {
            mTaggingPromptTextView.setText(this.getString(R.string.lock_tag_prompt));
        } else {
            mSeal.setTagId(tagId);

            ReadTagTask task = new ReadTagTask();
            task.execute(tag);
        }
    }

    private void doSignIn() {
        if (mLastLocation == null) {
            Toast toast = Toast.makeText(getActivity(), R.string.prompt_location_missed, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            mSeal.setLocation(mLastLocation);
        }

        if (mLastAddress == null || mLastAddress.isEmpty()) {
            mSeal.setPlace(getString(R.string.words_missed_address_prefix) + mSeal.getLocation() + getString(R.string.words_missed_address_suffix));
        } else {
            mSeal.setPlace(mLastAddress);
        }

        if (Utils.isNetworkConnected(getActivity())) {
            getActivity().setProgressBarIndeterminateVisibility(true);

            SignInTask task = new SignInTask();
            task.execute(mSeal);
        } else {
            Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_SHORT).show();
        }
    }

    private class SignInTask extends AsyncTask<Seal, Void, String> {
        @Override
        protected String doInBackground(Seal... params) {

            Log.i(TAG, "SignInTask doInBackground");

            Seal seal = params[0];

            return performSignInTask(seal);
        }

        private String performSignInTask(Seal seal) {
            Log.d(TAG, "token: " + User.get().getTOKEN());
            Log.d(TAG, "uid: " + seal.getTagId());
            Log.d(TAG, "coordinate: " + seal.getLocation());
            Log.d(TAG, "operateTime: " + seal.getOperationTime());
            Log.d(TAG, "language: " + Utils.getCurrentLanguage());

            // Create request
            SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfSignin());

            request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
            request.addProperty(Utils.newPropertyInstance("uid", seal.getTagId(), String.class));
            request.addProperty(Utils.newPropertyInstance("sealId", seal.getSealId(), String.class));
            request.addProperty(Utils.newPropertyInstance("coordinate", seal.getLocation(), String.class));
            request.addProperty(Utils.newPropertyInstance("place", seal.getPlace(), String.class));
            request.addProperty(Utils.newPropertyInstance("operateTime",
                    DateFormat.format("yyyy-MM-dd HH:mm:ss", seal.getOperationTime()).toString(), String.class));
            request.addProperty(Utils.newPropertyInstance("language", Utils.getCurrentLanguage(), String.class));

            // Create envelope
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            // Set output SOAP object
            envelope.setOutputSoapObject(request);
            // Create HTTP call object
            HttpTransportSE androidHttpTransport = new HttpTransportSE(Utils.getWsUrl());

            String responseJSON = null;

            try {
                // new MarshalDate().register(envelope);

                // Invoke web service
                androidHttpTransport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfSignin(), envelope);
                // Get the response
                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                // Assign it to static variable
                responseJSON = response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseJSON;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "SignInTask onPostExecute");

            if (getActivity() != null && getActivity() instanceof SignInActivity) {
                getActivity().setProgressBarIndeterminateVisibility(false);
            }

            getActivity().setProgressBarIndeterminateVisibility(false);

            try {
                if (result != null) {
                    Log.d(TAG, result);

                    WsResult ret = new Gson().fromJson(result, WsResult.class);

                    if (ret.isOK()) {
                        mTaggingPromptTextView.setText(R.string.prompt_signin_complete);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        getActivity().finish();
                    } else {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.prompt_signin_failed, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private class ReadTagTask extends AsyncTask<Tag, Void, String[]> {
        @Override
        protected String[] doInBackground(Tag... params) {

            Tag tag = params[0];

            return performReadTagTask(tag);
        }

        private String[] performReadTagTask(Tag tag) {

            String ret[] = new String[2];

            MifareUltralight mifare = MifareUltralight.get(tag);

            try {
                mifare.connect();

                // Read Seal ID
                byte[] payload = mifare.readPages(8);
                int sealId = Utils.bytestoInt(payload, 0, 4);
                ret[0] = String.valueOf(sealId);
                Log.d(TAG, "Seal ID: " + sealId);

                // Read Seal No
                payload = mifare.readPages(9);
                int len = (int) payload[0] & 0xFF;
                ret[1] = Utils.bytesToAscii(payload, 1, len);
                Log.d(TAG, "Seal No: " + ret[1]);

                return ret;
            } catch (IOException e) {
                Log.e(TAG, "IOException while reading tag message...", e);
            } finally {
                if (mifare != null) {
                    try {
                        mifare.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing tag...", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            if (getActivity() != null && getActivity() instanceof SignInActivity) {
                getActivity().setProgressBarIndeterminateVisibility(false);
            }

            mSeal.setSealId(result[0]);
            mSeal.setSealNo(result[1]);

            doSignIn();
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

            if (getActivity() != null && getActivity() instanceof SignInActivity) {
                getActivity().setProgressBarIndeterminateVisibility(false);
            }

            try {
                if (getActivity() instanceof SignInActivity) {
                    getActivity().setProgressBarIndeterminateVisibility(false);
                }

                if (result != null) {
                    mLocationMaster.setAddress(result);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "ReverseGeocodingTask onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "ReverseGeocodingTask onProgressUpdate");
        }
    }

}
