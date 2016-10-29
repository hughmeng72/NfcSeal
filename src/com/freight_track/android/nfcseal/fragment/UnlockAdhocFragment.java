package com.freight_track.android.nfcseal.fragment;

import java.io.File;
import java.io.FileOutputStream;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.freight_track.android.nfcseal.common.PictureUtils;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.model.Seal;
import com.freight_track.android.nfcseal.common.ThumbnailDownloader;
import com.freight_track.android.nfcseal.model.User;
import com.freight_track.android.nfcseal.common.Utils;
import com.freight_track.android.nfcseal.model.WsResult;
import com.freight_track.android.nfcseal.common.LocationMaster;
import com.freight_track.android.nfcseal.common.LocationReceiver;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

public class UnlockAdhocFragment extends Fragment {

    private static String TAG = "UnlockAdhocFragment";
    private static final String DIALOG_IMAGE = "image";
    private static final String BACKGROUND_COLOR = "#84D018";

    private Seal mSeal;
    ThumbnailDownloader<ImageView> mThumbnailThread;

    private String mLastLocation;
    private String mLastAddress;
    private LocationMaster mLocationMaster;
    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {
        @Override
        protected void onLocationReceived(Context context, Location loc) {
            Log.d(TAG, loc.toString());

            mLastLocation = String.format("%1$f,%2%f", loc.getLatitude(), loc.getLongitude());
            mLocationMaster.setLastCoordinate(mLastLocation);

            ReverseGeocodingTask task = new ReverseGeocodingTask();
            task.execute(loc);
        }
    };

    private TextView mLockTimeTextView;
    private TextView mLockPlaceTextView;
    private ImageView mPictureImageView1;
    private ImageView mPictureImageView2;
    private ImageView mPictureImageView3;
    private ImageButton mTakePictureImageButton;

    private RadioButton mSealNotFoundRadioButton;
    private RadioButton mTagNotFoundRadioButton;
    private RadioButton mOthersRadioButton;
    private EditText mOtherEditText;

    private EditText mCarriageNoEditText;
    private Button mConfirmationButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, User.get().getTOKEN());

        mSeal = new Seal(getActivity().getApplicationContext());

        mLocationMaster = LocationMaster.get(getActivity());
        mLastLocation = mLocationMaster.getLastCoordinate();
        mLastAddress = mLocationMaster.getAddress();

//        mLocationMaster.startLocationUpdates(mLocationListener);

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_unlock_adhoc, parent, false);

        mSealNotFoundRadioButton = (RadioButton) v.findViewById(R.id.sealNotFoundRadioButton);
        mTagNotFoundRadioButton = (RadioButton) v.findViewById(R.id.tagNotFoundRadioButton);
        mOthersRadioButton = (RadioButton) v.findViewById(R.id.othersRadioButton);
        mOtherEditText = (EditText) v.findViewById(R.id.otherEditText);

        mSealNotFoundRadioButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mSealNotFoundRadioButton.setChecked(true);
                mTagNotFoundRadioButton.setChecked(false);
                mOthersRadioButton.setChecked(false);
                mOtherEditText.setText("");
                mOtherEditText.setEnabled(false);
            }
        });

        mTagNotFoundRadioButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mSealNotFoundRadioButton.setChecked(false);
                mTagNotFoundRadioButton.setChecked(true);
                mOthersRadioButton.setChecked(false);
                mOtherEditText.setText("");
                mOtherEditText.setEnabled(false);
            }
        });

        mOthersRadioButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mSealNotFoundRadioButton.setChecked(false);
                mTagNotFoundRadioButton.setChecked(false);
                mOthersRadioButton.setChecked(true);
                mOtherEditText.setEnabled(true);
                mOtherEditText.requestFocus();
            }
        });

        mLockTimeTextView = (TextView) v.findViewById(R.id.lockTimeTextView);
        mLockPlaceTextView = (TextView) v.findViewById(R.id.lockPlaceTextView);
        mPictureImageView1 = (ImageView) v.findViewById(R.id.pictureImageView1);
        mPictureImageView2 = (ImageView) v.findViewById(R.id.pictureImageView2);
        mPictureImageView3 = (ImageView) v.findViewById(R.id.pictureImageView3);

        View.OnClickListener showImageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null) {
                    FragmentManager fm = getActivity().getFragmentManager();
                    ImageFragment.newInstance(v.getTag().toString()).show(fm, DIALOG_IMAGE);
                }
            }
        };

        mPictureImageView1.setOnClickListener(showImageListener);
        mPictureImageView2.setOnClickListener(showImageListener);
        mPictureImageView3.setOnClickListener(showImageListener);

        mTakePictureImageButton = (ImageButton) v.findViewById(R.id.takePictureImageButton);
        mTakePictureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSeal == null) return;

                if (mSeal.getUnlockPhotoName() == null || mSeal.getUnlockPhotoName().isEmpty()) {
                    // Intent i = new Intent(getActivity(),
                    // CameraActivity.class);
                    // startActivityForResult(i, 0);

                    dispatchTakePictureIntent();
                } else {
                    FragmentManager fm = getActivity().getFragmentManager();
                    ImageFragment.newInstance(mSeal.getUnlockPhotoFilePath()).show(fm, DIALOG_IMAGE);
                }
            }
        });

        mCarriageNoEditText = (EditText) v.findViewById(R.id.carriageNoEditText);
        mCarriageNoEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    mSeal.setSealNo(mCarriageNoEditText.getText().toString());
                    mSeal.setPlace("");
                    mSeal.setOperationTime(null);
                    mSeal.setPhoto1FilePath("");
                    mSeal.setPhoto2FilePath("");
                    mSeal.setPhoto3FilePath("");

                    getLockInfo();
                }

                return false;
            }
        });

        mConfirmationButton = (Button) v.findViewById(R.id.confirmationButton);
        mConfirmationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUnock();
            }
        });

        setupActionBar();

        return v;
    }

    private void doUnock() {

        if (mOthersRadioButton.isChecked()) {
            if (mOtherEditText.getText().toString().isEmpty()) {
                Toast toast = Toast.makeText(getActivity(), R.string.prompt_other_reason_missed, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                return;
            }
        }

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

        if (mSeal.getSealNo() == null || mSeal.getSealNo().isEmpty()) {
            Toast toast = Toast.makeText(getActivity(), R.string.prompt_no_available_seal_No, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return;
        }

        if (mSeal.getUnlockPhotoName() == null || mSeal.getUnlockPhotoName().isEmpty()) {

            Toast toast = Toast.makeText(getActivity(), R.string.prompt_unlock_need_a_photo, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            return;
        }

        if (Utils.isNetworkConnected(getActivity())) {
            getActivity().setProgressBarIndeterminateVisibility(true);

            mSeal.setOperationTime(new Date());

            if (mSealNotFoundRadioButton.isChecked()) {
                mSeal.setExceptionReason(mSealNotFoundRadioButton.getText().toString());
            } else if (mTagNotFoundRadioButton.isChecked()) {
                mSeal.setExceptionReason(mTagNotFoundRadioButton.getText().toString());
            } else {
                mSeal.setExceptionReason(mOtherEditText.getText().toString());
            }


            UnlockTask task = new UnlockTask();
            task.execute(mSeal);
        } else {
            Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NewApi")
    private void setupActionBar() {

        ActionBar actionBar = getActivity().getActionBar();

        actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.title_adhoc_unlock)));
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actionBar.setIcon(R.drawable.ic_company);
        }

    }

    private void getLockInfo() {

        if (Utils.isNetworkConnected(getActivity())) {
            getActivity().setProgressBarIndeterminateVisibility(true);

            GotOperationTask task = new GotOperationTask();
            task.execute(mSeal);
        } else {
            Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_SHORT).show();
        }

    }

    private String mCurrentPhotoPath;

    private void dispatchTakePictureIntent() {

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

        startActivityForResult(takePictureIntent, 0);
    }

    private File setupPhotoFile() throws IOException {

        File f = PictureUtils.createImageFile(null);
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult");

        if (resultCode != Activity.RESULT_OK)
            return;

        handleCameraPhoto();
    }

    private void handleCameraPhoto() {

        mSeal.setUnlockPhotoFilePath(mCurrentPhotoPath);

        if (mCurrentPhotoPath != null) {
            Log.d(TAG, mCurrentPhotoPath);

            PictureUtils.showPic(mTakePictureImageButton, mCurrentPhotoPath);

            mCurrentPhotoPath = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        getActivity().registerReceiver(mLocationReceiver, new IntentFilter(LocationMaster.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(mLocationReceiver);

        PictureUtils.cleanImageView(mTakePictureImageButton);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailThread.quit();
        mLocationMaster.stopLocationUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mThumbnailThread.clearQueue();
    }

    private class GotOperationTask extends AsyncTask<Seal, Void, String> {

        @Override
        protected String doInBackground(Seal... params) {
            Log.i(TAG, "GotOperationTask doInBackground");

            return performCheckTask(params[0]);
        }

        private String performCheckTask(Seal seal) {

            SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfLockOperationBySealNo());

            request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
            request.addProperty(Utils.newPropertyInstance("SealNumber", seal.getSealNo(), String.class));
            request.addProperty(Utils.newPropertyInstance("language", Utils.getCurrentLanguage(), String.class));

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(Utils.getWsUrl());

            String responseJSON = null;

            try {
                transport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfLockOperationBySealNo(), envelope);

                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

                responseJSON = response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseJSON;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "GotOperationTask onPostExecute");

            getActivity().setProgressBarIndeterminateVisibility(false);

            try {
                if (result != null) {
                    Gson gson = new Gson();
                    WsResult ret = gson.fromJson(result, WsResult.class);

                    if (ret.isOK()) {
                        updateUI(ret);
                    } else {
                        mSeal.setSealNo(null);

                        Toast toast = Toast.makeText(getActivity(), R.string.prompt_tag_not_read_lock_info, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } else {
                    mSeal.setSealNo(null);

                    Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        private void updateUI(WsResult result) {

            String lockTime = getActivity().getString(R.string.lock_time)
                    + result.getOperations().get(0).getOperateTime();
            mLockTimeTextView.setText(lockTime);

            String lockPlace = getActivity().getString(R.string.lock_place) + result.getOperations().get(0).getPlace();
            mLockPlaceTextView.setText(lockPlace);

            String imageNames[] = result.getOperations().get(0).getImgNames().split(";");

            Log.d(TAG, "Image count: " + String.valueOf(imageNames.length));

            for (int i = 0; i < imageNames.length; i++) {
                String imageName = imageNames[i];

                if (imageName != null && !imageName.isEmpty()) {

                    String url = "http://www.freight-track.com/image/" + imageName;
                    Log.d(TAG, url);

                    switch (i) {
                        case 0:
                            mSeal.setPhotoName1(imageName);
                            mThumbnailThread.queueThumbnail(mPictureImageView1, url);
                            break;
                        case 1:
                            mSeal.setPhotoName2(imageName);
                            mThumbnailThread.queueThumbnail(mPictureImageView2, url);
                            break;
                        case 2:
                            mSeal.setPhotoName3(imageName);
                            mThumbnailThread.queueThumbnail(mPictureImageView3, url);
                            break;
                    }
                } else {
                    switch (i) {
                        case 0:
                            mPictureImageView1.setVisibility(View.GONE);
                            break;
                        case 1:
                            mPictureImageView2.setVisibility(View.GONE);
                            break;
                        case 2:
                            mPictureImageView3.setVisibility(View.GONE);
                            break;
                    }
                }
            }

            for (int i = 3; i > imageNames.length; i--) {
                switch (i) {
                    case 3:
                        mPictureImageView3.setVisibility(View.GONE);
                        break;
                    case 2:
                        mPictureImageView2.setVisibility(View.GONE);
                        break;
                    case 1:
                        mPictureImageView1.setVisibility(View.GONE);
                        break;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "GotOperationTask onProgressUpdate");
        }
    }

    private class UnlockTask extends AsyncTask<Seal, Void, String> {

        @Override
        protected String doInBackground(Seal... params) {
            Log.i(TAG, "UnlockTask doInBackground");

            Seal seal = params[0];

            if (!seal.getUnlockPhotoFilePath().isEmpty()) {
                Log.i(TAG, "Uploading photo...");

                PictureUtils.uploadPhoto(seal.getUnlockPhotoName(), seal.getUnlockPhotoFilePath());

                Log.i(TAG, "Uploaded photo...");
            }

            // Invoke web service
            return performUnlockTask(seal);
        }

        private String performUnlockTask(Seal seal) {

            Log.i(TAG, "token: " + User.get().getTOKEN());
            Log.i(TAG, "sealNumber: " + seal.getSealNo());
            Log.i(TAG, "exceptionItem: " + seal.getExceptionReason());
            Log.i(TAG, "coordinate: " + seal.getLocation());
            Log.i(TAG, "place: " + seal.getPlace());
            Log.i(TAG, "operateTime: " + seal.getOperationTime());
            Log.i(TAG, "imageNames: " + seal.getUnlockPhotoName());
            Log.i(TAG, "language: " + Utils.getCurrentLanguage());

            // Create request
            SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfAdhocUnlock());

            request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
            request.addProperty(Utils.newPropertyInstance("sealNumber", seal.getSealNo(), String.class));
            request.addProperty(Utils.newPropertyInstance("exceptionItem", seal.getExceptionReason(), String.class));
            request.addProperty(Utils.newPropertyInstance("coordinate", seal.getLocation(), String.class));
            request.addProperty(Utils.newPropertyInstance("place", seal.getPlace(), String.class));
            request.addProperty(Utils.newPropertyInstance("operateTime",
                    DateFormat.format("yyyy-MM-dd HH:mm:ss", seal.getOperationTime()).toString(), String.class));
            request.addProperty(Utils.newPropertyInstance("imageNames", seal.getUnlockPhotoName(), String.class));
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
                androidHttpTransport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfAdhocUnlock(), envelope);
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
            Log.i(TAG, "UnlockTask onPostExecute");
            // Log.i(TAG, result);

            getActivity().setProgressBarIndeterminateVisibility(false);

            try {
                if (result != null) {
                    WsResult ret = new Gson().fromJson(result, WsResult.class);

                    if (ret.isOK()) {
                        Toast.makeText(getActivity(), R.string.prompt_adhoc_unlock_success, Toast.LENGTH_SHORT).show();

                        getActivity().finish();
                    } else {
                        Toast toast = Toast.makeText(getActivity(), R.string.prompt_unlock_failed, Toast.LENGTH_LONG);
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
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "UnlockTask onProgressUpdate");
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
