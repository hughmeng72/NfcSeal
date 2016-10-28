package com.freight_track.android.nfcseal.fragment;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalDate;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.freight_track.android.nfcseal.common.PictureUtils;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.model.Seal;
import com.freight_track.android.nfcseal.model.Seal.StateEnum;
import com.freight_track.android.nfcseal.model.User;
import com.freight_track.android.nfcseal.common.Utils;
import com.freight_track.android.nfcseal.model.WsResult;
import com.freight_track.android.nfcseal.common.LocationMaster;
import com.freight_track.android.nfcseal.common.LocationReceiver;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

public class LockFragment extends Fragment {

    private static final String BACKGROUND_COLOR = "#7679EA";
    private static final String TAG = "LockFragment";
    private static final String DIALOG_IMAGE = "image";
    private static final int ACTION_TAKE_PHOTO_1 = 0;
    private static final int ACTION_TAKE_PHOTO_2 = 1;
    private static final int ACTION_TAKE_PHOTO_3 = 2;

    private TextView mTaggingPromptTextView;
    private ImageButton mLock_takePicture1_ImageButton;
    private ImageButton mLock_takePicture2_ImageButton;
    private ImageButton mLock_takePicture3_ImageButton;
    private EditText mCarriageNoEditText;
    private Button mConfirmationButton;

    private Seal mSeal;
    private LocationMaster mLocationMaster;
    private String mLastLocation;
    private String mLastAddress;

    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {

        @Override
        protected void onLocationReceived(Context context, Location loc) {
            Log.d(TAG, loc.toString());

            mLastLocation = String.format("%1$f,%2$f",loc.getLatitude(), loc.getLongitude());
            mLocationMaster.setLastCoordinate(mLastLocation);

            if (Utils.getCurrentLanguage().equals("en-US")) {
                ReverseGeocodingTask task = new ReverseGeocodingTask();
                task.execute(loc);
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSeal = new Seal(getActivity().getApplicationContext());

        mLocationMaster = LocationMaster.get(getActivity());
        mLastLocation = mLocationMaster.getLastCoordinate();
        mLastAddress = mLocationMaster.getAddress();

        mLocationMaster.startLocationUpdates(mLocationListener);

        Log.d(TAG, User.get().getTOKEN());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceSate) {

        View v = inflater.inflate(R.layout.fragment_lock, parent, false);

        mTaggingPromptTextView = (TextView) v.findViewById(R.id.tagPromptTextView);
        mCarriageNoEditText = (EditText) v.findViewById(R.id.carriageNoEditText);

        mLock_takePicture1_ImageButton = (ImageButton) v.findViewById(R.id.takePicture1_ImageButton);
        mLock_takePicture1_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSeal.getPhotoName1() == null || mSeal.getPhotoName1().isEmpty()) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_1);
                } else {
                    FragmentManager fm = getActivity().getFragmentManager();
                    ImageFragment.newInstance(mSeal.getPhoto1FilePath()).show(fm, DIALOG_IMAGE);
                }
            }
        });

        mLock_takePicture2_ImageButton = (ImageButton) v.findViewById(R.id.takePicture2_ImageButton);
        mLock_takePicture2_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSeal.getPhotoName2() == null || mSeal.getPhotoName2().isEmpty()) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_2);
                } else {
                    FragmentManager fm = getActivity().getFragmentManager();
                    ImageFragment.newInstance(mSeal.getPhoto2FilePath()).show(fm, DIALOG_IMAGE);
                }
            }
        });

        mLock_takePicture3_ImageButton = (ImageButton) v.findViewById(R.id.takePicture3_ImageButton);
        mLock_takePicture3_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSeal.getPhotoName3() == null || mSeal.getPhotoName3().isEmpty()) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_3);
                } else {
                    FragmentManager fm = getActivity().getFragmentManager();
                    ImageFragment.newInstance(mSeal.getPhoto3FilePath()).show(fm, DIALOG_IMAGE);
                }
            }
        });

        mConfirmationButton = (Button) v.findViewById(R.id.confirmationButton);
        mConfirmationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLock();
            }
        });

        setupActionBar();

        return v;
    }

    @SuppressLint("NewApi")
    private void setupActionBar() {

        ActionBar actionBar = getActivity().getActionBar();

        actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.prompt_lock)));
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actionBar.setIcon(R.drawable.ic_company);
        }

    }

    private String mCurrentPhotoPath;

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = null;

        try {
            f = setupPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, actionCode);
    }

    private File setupPhotoFile() throws IOException {

        File f = PictureUtils.createImageFile(null);
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void handleCameraPhoto() {

        mSeal.setUnlockPhotoFilePath(mCurrentPhotoPath);

        if (mCurrentPhotoPath != null) {
            Log.d(TAG, mCurrentPhotoPath);

            // PictureUtils.setPic(mTakePictureImageButton, mCurrentPhotoPath);

            mCurrentPhotoPath = null;
        }
    }

    private void showPhoto2s() {
        // (Re)set the image button's image based on our photo

        if (mSeal.getPhotoName1() == null || mSeal.getPhotoName1().isEmpty()) {
            mLock_takePicture1_ImageButton.setImageResource(android.R.drawable.ic_menu_camera);
        } else {
            BitmapDrawable b = null;

            b = PictureUtils.getScaledDrawable(getActivity(), mSeal.getPhoto1FilePath(), false);

            // PictureUtils.rotateImageWith90(mLock_takePicture1_ImageButton);
            mLock_takePicture1_ImageButton.setImageDrawable(b);
        }

        if (mSeal.getPhotoName2() == null || mSeal.getPhotoName2().isEmpty()) {
            mLock_takePicture2_ImageButton.setImageResource(android.R.drawable.ic_menu_camera);
        } else {
            BitmapDrawable b = null;

            b = PictureUtils.getScaledDrawable(getActivity(), mSeal.getPhoto2FilePath(), false);

            // PictureUtils.rotateImageWith90(mLock_takePicture2_ImageButton);
            mLock_takePicture2_ImageButton.setImageDrawable(b);
        }

        if (mSeal.getPhotoName3() == null || mSeal.getPhotoName3().isEmpty()) {
            mLock_takePicture3_ImageButton.setImageResource(android.R.drawable.ic_menu_camera);
        } else {
            BitmapDrawable b = null;

            b = PictureUtils.getScaledDrawable(getActivity(), mSeal.getPhoto3FilePath(), false);

            // PictureUtils.rotateImageWith90(mLock_takePicture3_ImageButton);
            mLock_takePicture3_ImageButton.setImageDrawable(b);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mLocationReceiver, new IntentFilter(LocationMaster.ACTION_LOCATION));

        // showPhotos();
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mLocationReceiver);

        PictureUtils.cleanImageView(mLock_takePicture1_ImageButton);
        PictureUtils.cleanImageView(mLock_takePicture2_ImageButton);
        PictureUtils.cleanImageView(mLock_takePicture3_ImageButton);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocationMaster.stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();

        PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), getActivity()
                .getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        nfcAdapter.enableForegroundDispatch(getActivity(), pi, new IntentFilter[]{filter}, Utils.getNfcTechList());
    }

    @Override
    public void onPause() {
        super.onPause();

        NfcAdapter ncfAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        ncfAdapter.disableForegroundDispatch(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == ACTION_TAKE_PHOTO_1) {
            mSeal.setPhoto1FilePath(mCurrentPhotoPath);

            if (mCurrentPhotoPath != null) {
                Log.d(TAG, mCurrentPhotoPath);

                PictureUtils.showPic(mLock_takePicture1_ImageButton, mCurrentPhotoPath);
                mCurrentPhotoPath = null;
            }
        } else if (requestCode == ACTION_TAKE_PHOTO_2) {
            mSeal.setPhoto2FilePath(mCurrentPhotoPath);

            if (mCurrentPhotoPath != null) {
                Log.d(TAG, mCurrentPhotoPath);

                PictureUtils.showPic(mLock_takePicture2_ImageButton, mCurrentPhotoPath);
                mCurrentPhotoPath = null;
            }
        } else if (requestCode == ACTION_TAKE_PHOTO_3) {
            mSeal.setPhoto3FilePath(mCurrentPhotoPath);

            if (mCurrentPhotoPath != null) {
                Log.d(TAG, mCurrentPhotoPath);

                PictureUtils.showPic(mLock_takePicture3_ImageButton, mCurrentPhotoPath);
                mCurrentPhotoPath = null;
            }
        }
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

    private void updateUI() {

        String out = null;

        if (mSeal.getTagId() == null || mSeal.getTagId().isEmpty()) {
            out = this.getString(R.string.lock_tag_prompt);
        } else {
            out = getActivity().getString(R.string.prompt_tagging_success) + mSeal.getSealNo() + "\n";

            if (Utils.isNetworkConnected(getActivity())) {
                getActivity().setProgressBarIndeterminateVisibility(true);

                CheckTask task = new CheckTask();
                task.execute(mSeal);
            } else {
                Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_SHORT).show();
            }
        }

        mTaggingPromptTextView.setText(out);
    }

    private void doLock() {

        if (mLastLocation == null) {
            Toast toast = Toast.makeText(getActivity(), R.string.prompt_location_missed, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return;
        } else {
            mSeal.setLocation(mLastLocation);
        }

        if (Utils.getCurrentLanguage().equals("en-US") && (mLastAddress == null || mLastAddress.isEmpty())) {
            mSeal.setPlace(getString(R.string.words_missed_address_prefix) + mSeal.getLocation() + getString(R.string.words_missed_address_suffix));
        } else {
            mSeal.setPlace(mLastAddress);
        }

        mSeal.setCarriageNo(mCarriageNoEditText.getText().toString());

        if (mSeal.getTagId() == null || mSeal.getTagId().isEmpty()) {
            Toast toast = Toast.makeText(getActivity(), R.string.prompt_no_available_tag, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return;
        }

        if (mSeal.getCarriageNo() == null || mSeal.getCarriageNo().isEmpty()) {
            Toast toast = Toast.makeText(getActivity(), R.string.prompt_no_carriage_id, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return;
        }

        if ((mSeal.getPhotoName1() == null || mSeal.getPhotoName1().isEmpty())
                && (mSeal.getPhotoName2() == null || mSeal.getPhotoName2().isEmpty())
                && (mSeal.getPhotoName3() == null || mSeal.getPhotoName3().isEmpty())) {

            Toast toast = Toast.makeText(getActivity(), R.string.prompt_at_least_one_photo, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return;
        }

        if (Utils.isNetworkConnected(getActivity())) {
            getActivity().setProgressBarIndeterminateVisibility(true);

            mSeal.setOperationTime(new Date());

            LockTask task = new LockTask();
            task.execute(mSeal);
        } else {
            Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_SHORT).show();
        }
    }

    // AsynTask class to handle Login Web Service call as separate UI Thread
    private class LockTask extends AsyncTask<Seal, Void, String> {

        @Override
        protected String doInBackground(Seal... params) {
            Log.i(TAG, "LockTask doInBackground: ");

            Seal seal = params[0];

            if (seal.getPhotoName1() != null && !seal.getPhotoName1().isEmpty()) {
                Log.i(TAG, "Uploading photo 1...");

                PictureUtils.uploadPhoto(seal.getPhotoName1(), seal.getPhoto1FilePath());

                Log.i(TAG, "Uploaded photo 1...");
            }

            if (seal.getPhotoName2() != null && !seal.getPhotoName2().isEmpty()) {
                Log.i(TAG, "Uploading photo 2...");

                PictureUtils.uploadPhoto(seal.getPhotoName2(), seal.getPhoto2FilePath());

                Log.i(TAG, "Uploaded photo 2...");
            }

            if (seal.getPhotoName3() != null && !seal.getPhotoName3().isEmpty()) {
                Log.i(TAG, "Uploading photo 3...");

                PictureUtils.uploadPhoto(seal.getPhotoName3(), seal.getPhoto3FilePath());

                Log.i(TAG, "Uploaded photo 3...");
            }

            // Invoke web service GetTokenByUserNameAndPasswordResult
            return performLockTask(seal);
        }

        private String performLockTask(Seal seal) {

            // Create request
            SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfLock());

            request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
            request.addProperty(Utils.newPropertyInstance("uid", seal.getTagId(), String.class));
            request.addProperty(Utils.newPropertyInstance("sealId", seal.getSealId(), String.class));
            request.addProperty(Utils.newPropertyInstance("productCode", seal.getCarriageNo(), String.class));
            request.addProperty(Utils.newPropertyInstance("coordinate", seal.getLocation(), String.class));
            request.addProperty(Utils.newPropertyInstance("place", seal.getPlace(), String.class));
            request.addProperty(Utils.newPropertyInstance("operateTime",
                    DateFormat.format("yyyy-MM-dd HH:mm:ss", seal.getOperationTime()).toString(), String.class));
            request.addProperty(Utils.newPropertyInstance("imageNames", seal.getPhotoNames(), String.class));
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
                new MarshalDate().register(envelope);

                // Invoke web service
                androidHttpTransport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfLock(), envelope);
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
            Log.i(TAG, "LockTask onPostExecute");

            getActivity().setProgressBarIndeterminateVisibility(false);

            try {
                if (result != null) {
                    WsResult ret = new Gson().fromJson(result, WsResult.class);

                    if (ret.isOK()) {
                        Toast.makeText(getActivity(), R.string.prompt_lock_success, Toast.LENGTH_SHORT).show();

                        getActivity().finish();
                    } else {
                        Toast toast = Toast.makeText(getActivity(), R.string.prompt_lock_failed, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "LockTask onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "LockTask onProgressUpdate");
        }
    }

    private class CheckTask extends AsyncTask<Seal, Void, String> {

        @Override
        protected String doInBackground(Seal... params) {
            Log.i(TAG, "CheckTask doInBackground");

            return performCheckTask(params[0]);
        }

        private String performCheckTask(Seal seal) {

            SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfSealStateCheck());

            request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
            request.addProperty(Utils.newPropertyInstance("uid", seal.getTagId(), String.class));
            request.addProperty(Utils.newPropertyInstance("language", Utils.getCurrentLanguage(), String.class));

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(Utils.getWsUrl());

            String responseJSON = null;

            try {
                transport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfSealStateCheck(), envelope);

                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

                responseJSON = response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseJSON;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "CheckTask onPostExecute");

            getActivity().setProgressBarIndeterminateVisibility(false);

            try {
                if (result != null) {
                    WsResult ret = new Gson().fromJson(result, WsResult.class);

                    StateEnum state = StateEnum.values()[ret.getResultOfFirstRecord()];
                    mSeal.setState(state);

                    ValidateSeal();
                } else {
                    Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        private void ValidateSeal() {
            if (mSeal.getState() != StateEnum.lockable) {
                mSeal.setTagId(null);

                Toast toast = Toast.makeText(getActivity(), mSeal.getStateWarningDescription(mSeal.getState()),
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "CheckTask onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "CheckTask onProgressUpdate");
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
            mSeal.setSealId(result[0]);
            mSeal.setSealNo(result[1]);

            updateUI();
        }
    }

    private class ReverseGeocodingTask extends AsyncTask<Location, Void, String> {

        @Override
        protected String doInBackground(Location... params) {
            Log.i(TAG, "ReverseGeocodingTask doInBackground");

            return performReverseGeocodingTask(params[0]);
        }

//		private String performReverseGeocodingTask(Location location) {
//
//			SoapObject request = new SoapObject(Utils.getWsNamespace2(), Utils.getWsMethodOfGetGooglePosition());
//
//			request.addProperty(Utils.newPropertyInstance("googleCoordinate", String.format("%1$s,%2$s", String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())), String.class));
//			request.addProperty(Utils.newPropertyInstance("language", Utils.getCurrentLanguage(), String.class));
//
//			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//			envelope.dotNet = true;
//			envelope.setOutputSoapObject(request);
//
//			HttpTransportSE transport = new HttpTransportSE(Utils.getWsUrl2());
//
//			String responseJSON = null;
//
//			try {
//				transport.call(Utils.getWsSoapAction2() + Utils.getWsMethodOfGetGooglePosition(), envelope);
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

        private String performReverseGeocodingTask(Location location) {
            GeocodingResult[] results;

            GeoApiContext contextG = new GeoApiContext().setApiKey("AIzaSyAp2aNol3FhJypghIA2IUZIOkNTwo6YPbY");
            try {
                results = GeocodingApi.geocode(contextG, String.format("%1$f,%2$f", location.getLatitude(), location.getLongitude())).await();

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

            getActivity().setProgressBarIndeterminateVisibility(false);

            try {
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
