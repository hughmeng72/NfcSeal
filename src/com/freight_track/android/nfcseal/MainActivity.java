package com.freight_track.android.nfcseal;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

public class MainActivity extends SingleFragmentActivity implements
        LoginFragment.Callbacks, MainFragment.Callbacks {

    private static final String TAG = "MainActivity";
    private static final int LOCK = 0;

    private LocationMaster mLocationMaster;
    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {

        @Override
        protected void onLocationReceived(Context context, Location loc) {
            Log.d(TAG, loc.toString());

            mLocationMaster.setKeptLocation(loc);

            if (Utils.getCurrentLanguage().equals("en-US")) {
                ReverseGeocodingTask task = new ReverseGeocodingTask();
                task.execute(loc);
            }

            //			// Stop location update once got a location with specified accuracy.
//			if (loc.getAccuracy() <= 50.0f) {
//				Log.i(TAG, TAG + "Got enough accuracy location.");
//				mLocationMaster.stopLocationUpdates();
//			}
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        mLocationMaster = LocationMaster.get(this);
        mLocationMaster.startLocationUpdates();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }

    @Override
    public void onLoginValiated() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment oldFragment = fm.findFragmentById(R.id.fragmentContainer);
        Fragment newFragment = new MainFragment();

        if (oldFragment != null) {
            ft.remove(oldFragment);
        }

        ft.add(R.id.fragmentContainer, newFragment);

        ft.commit();

        // Toast.makeText(this, R.string.common_Todo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSignInRequest() {
        Intent i = new Intent(this, SignInActivity.class);
        startActivity(i);
    }

    @Override
    public void inAction(View v) {

        if (v.getTag() != null) {

            MainFragment.ActionEnum act = MainFragment.ActionEnum.valueOf(v
                    .getTag().toString());

            Intent i;
            switch (act) {
                case lock:
                    i = new Intent(this, LockActivity.class);
                    startActivity(i);
                    break;
                case unlock:
                    i = new Intent(this, UnlockActivity.class);
                    startActivity(i);
                    break;
                case signIn:
                    i = new Intent(this, SignInActivity.class);
                    startActivity(i);
                    break;
                case inquiry:
                    i = new Intent(this, CarriageListActivity.class);
                    startActivity(i);
                    break;
                case aboutMe:
                    i = new Intent(this, AboutMeActivity.class);
                    startActivity(i);
                    break;
                case more:
                    i = new Intent(this, MoreActivity.class);
                    startActivity(i);
                    break;
                default:
                    // Do nothing
                    Toast.makeText(this, R.string.common_Todo, Toast.LENGTH_SHORT)
                            .show();
            }
        }
    }

    private static long backPressed;

    @Override
    public void onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), R.string.prompt_exit_alert, Toast.LENGTH_SHORT).show();

            backPressed = System.currentTimeMillis();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        registerReceiver(mLocationReceiver, new IntentFilter(LocationMaster.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterReceiver(mLocationReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocationMaster.stopLocationUpdates();
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

            try {
                if (result != null) {
                    mLocationMaster.setAddress(result);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
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
