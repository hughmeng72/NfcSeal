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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.freight_track.android.nfcseal.common.PictureUtils;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.model.Seal;
import com.freight_track.android.nfcseal.model.Seal.StateEnum;
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

public class UnlockFragment extends Fragment {

    private static String TAG = "UnlockFragment";
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

            if (Utils.getCurrentLanguage().equals("en-US")) {
                ReverseGeocodingTask task = new ReverseGeocodingTask();
                task.execute(loc);
            }
        }
    };

    private TextView mTaggingPromptTextView;
    private TextView mLockTimeTextView;
    private TextView mLockPlaceTextView;
    private ImageView mPictureImageView1;
    private ImageView mPictureImageView2;
    private ImageView mPictureImageView3;
    private ImageButton mTakePictureImageButton;
    private Button mConfirmationButton;
    private TextView mTagNotFoundTextView;

    private Callbacks mCallbacks;

    // Required interface for hosting activities
    public interface Callbacks {
        void onAdhocUnlockRequest();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


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

        View v = inflater.inflate(R.layout.fragment_unlock, parent, false);

        mTaggingPromptTextView = (TextView) v.findViewById(R.id.tagPromptTextView);
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

        mConfirmationButton = (Button) v.findViewById(R.id.confirmationButton);
        mConfirmationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUnock();
            }
        });

        mTagNotFoundTextView = (TextView) v.findViewById(R.id.tagNotFoundTextView);
        mTagNotFoundTextView.setText(Html.fromHtml("<u><em>" + getString(R.string.tag_not_found_prompt) + "</em></u>"));
//		SpannableString spanString = new SpannableString(getString(R.string.tag_not_found_prompt));
//		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
//		spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
//		mTagNotFoundTextView.setText(spanString);

        this.mTagNotFoundTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Not found");

//				Intent i = new Intent(getActivity(), UnlockAdhocActivity.class);
//				startActivity(i);

                mCallbacks.onAdhocUnlockRequest();
            }
        });

        setupActionBar();

        return v;
    }

    @SuppressLint("NewApi")
    private void setupActionBar() {

        ActionBar actionBar = getActivity().getActionBar();

        actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.prompt_unlock)));
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actionBar.setIcon(R.drawable.ic_company);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        getActivity().registerReceiver(mLocationReceiver, new IntentFilter(LocationMaster.ACTION_LOCATION));

        // showPhotos();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), getActivity()
                .getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);

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

        PictureUtils.cleanImageView(mTakePictureImageButton);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailThread.quit();
//        mLocationMaster.stopLocationUpdates(bdLocationListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mThumbnailThread.clearQueue();
    }

    public void gotTag(byte[] bs, Tag tag) {

        String tagId = Utils.ByteArrayToHexString(bs);
        Log.d(TAG, "NFC Tag UID: " + tagId);

        if (tagId == null || tagId.isEmpty()) {
            mTaggingPromptTextView.setText(this.getString(R.string.lock_tag_prompt));
        } else {
            mSeal.setTagId(tagId);
            mSeal.setPlace("");
            mSeal.setOperationTime(null);
            mSeal.setPhoto1FilePath("");
            mSeal.setPhoto2FilePath("");
            mSeal.setPhoto3FilePath("");

            ReadTagTask task = new ReadTagTask();
            task.execute(tag);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult");

        if (resultCode != Activity.RESULT_OK)
            return;

        handleCameraPhoto();
    }

    private void getLockInfo() {

        String out = null;

        if (mSeal.getTagId() == null || mSeal.getTagId().isEmpty()) {
            out = this.getString(R.string.lock_tag_prompt);
        } else {
            out = getString(R.string.prompt_tagging_success) + mSeal.getSealNo() + "\n";

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

    private void doUnock() {

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

        if (mSeal.getTagId() == null || mSeal.getTagId().isEmpty()) {
            Toast toast = Toast.makeText(getActivity(), R.string.prompt_no_available_tag, Toast.LENGTH_SHORT);
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

            UnlockTask task = new UnlockTask();
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

    private void handleCameraPhoto() {

        mSeal.setUnlockPhotoFilePath(mCurrentPhotoPath);

        if (mCurrentPhotoPath != null) {
            Log.d(TAG, mCurrentPhotoPath);

            PictureUtils.showPic(mTakePictureImageButton, mCurrentPhotoPath);

            mCurrentPhotoPath = null;
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

            if (Utils.isNetworkConnected(getActivity())) {
                if (mSeal.getState() == StateEnum.locked) {
                    getActivity().setProgressBarIndeterminateVisibility(true);

                    GotOperationTask task = new GotOperationTask();
                    task.execute(mSeal);
                } else {
                    mSeal.setTagId(null);

                    Toast toast = Toast.makeText(getActivity(), mSeal.getStateWarningDescription(mSeal.getState()),
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "CheckTask onProgressUpdate");
        }
    }

    private class GotOperationTask extends AsyncTask<Seal, Void, String> {

        @Override
        protected String doInBackground(Seal... params) {
            Log.i(TAG, "GotOperationTask doInBackground");

            return performCheckTask(params[0]);
        }

        private String performCheckTask(Seal seal) {

            SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfLockOperation());

            request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
            request.addProperty(Utils.newPropertyInstance("TagUId", seal.getTagId(), String.class));
            request.addProperty(Utils.newPropertyInstance("language", Utils.getCurrentLanguage(), String.class));

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(Utils.getWsUrl());

            String responseJSON = null;

            try {
                // new MarshalDate().register(envelope);

                transport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfLockOperation(), envelope);

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
                        mSeal.setTagId(null);

                        Toast toast = Toast.makeText(getActivity(), R.string.prompt_tag_not_read_lock_info, Toast.LENGTH_LONG);
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
            Log.i(TAG, "uid: " + seal.getTagId());
            Log.i(TAG, "coordinate: " + seal.getLocation());
            Log.i(TAG, "place: " + seal.getPlace());
            Log.i(TAG, "operateTime: " + seal.getOperationTime());
            Log.i(TAG, "imageNames: " + seal.getUnlockPhotoName());
            Log.i(TAG, "language: " + Utils.getCurrentLanguage());

            // Create request
            SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfUnlock());

            request.addProperty(Utils.newPropertyInstance("token", User.get().getTOKEN(), String.class));
            request.addProperty(Utils.newPropertyInstance("uid", seal.getTagId(), String.class));
            request.addProperty(Utils.newPropertyInstance("sealId", seal.getSealId(), String.class));
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
                androidHttpTransport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfUnlock(), envelope);
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
                        Toast.makeText(getActivity(), R.string.prompt_unlock_success, Toast.LENGTH_SHORT).show();

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

            getLockInfo();
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
