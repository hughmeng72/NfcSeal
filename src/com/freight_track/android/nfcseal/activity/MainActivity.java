package com.freight_track.android.nfcseal.activity;

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

import com.freight_track.android.nfcseal.fragment.LoginFragment;
import com.freight_track.android.nfcseal.fragment.MainFragment;
import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.common.LocationMaster;
import com.freight_track.android.nfcseal.common.LocationReceiver;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

public class MainActivity extends SingleFragmentActivity implements
        LoginFragment.Callbacks, MainFragment.Callbacks {

    private static final String TAG = "MainActivity";
    private static final int LOCK = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

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
}