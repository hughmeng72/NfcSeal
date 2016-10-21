package com.freight_track.android.nfcseal;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;

public class LoginFragment extends Fragment {

	private static final String BACKGROUND_COLOR = "#4682B4";
	public static final String PREF_REMEMBER_ME = "RememberMe";
	public static final String PREF_USERNAME = "UserName";
	public static final String PREF_PASSWORD = "Password";

	private final String TAG = "LoginFragment";

	private EditText mUserNameEditText;
	private EditText mPasswordTextEdit;
	private Button mLoginButton;
	private Button mRegisterButton;
	private ImageButton mSignInImageButton;
	private CheckBox mRememberMeCheckBox;

	Gson gson = new Gson();

	private Callbacks mCallbacks;

	// Required interface for hosting activities
	public interface Callbacks {
		void onLoginValiated();

		void onSignInRequest();
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
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_login, parent, false);

		mRememberMeCheckBox = (CheckBox) v.findViewById(R.id.fragment_login_rememberMeCheckBox);

		mUserNameEditText = (EditText) v.findViewById(R.id.fragment_login_userNameEditText);
		mPasswordTextEdit = (EditText) v.findViewById(R.id.fragment_login_passwordEditText);

		View.OnClickListener notImplementedListten = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), R.string.common_Todo, Toast.LENGTH_SHORT).show();
			}
		};

		mLoginButton = (Button) v.findViewById(R.id.fragment_login_loginButton);
		mLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}
		});

		mRegisterButton = (Button) v.findViewById(R.id.fragment_login_registerButton);
		mRegisterButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), RegisterActivity.class);
				startActivity(i);
			}
		});

		mSignInImageButton = (ImageButton) v.findViewById(R.id.fragment_login_signInImageButton);
		mSignInImageButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mCallbacks.onSignInRequest();
			}
		});

		rememberMe();

		setupActionBar();

		return v;
	}

	@SuppressLint("NewApi")
	private void setupActionBar() {

		ActionBar actionBar = getActivity().getActionBar();

		actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.prompt_login)));
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			actionBar.setIcon(R.drawable.ic_company);
		}
	}

	private void rememberMe() {
		boolean rememberMe = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(PREF_REMEMBER_ME,
						false);

		mRememberMeCheckBox.setChecked(rememberMe);

		if (rememberMe) {
			mUserNameEditText.setText(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
							PREF_USERNAME, ""));

			mPasswordTextEdit.setText(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
							PREF_PASSWORD, ""));
		}
	}

	private void login() {

		String userName = mUserNameEditText.getText().toString();
		String password = mPasswordTextEdit.getText().toString();

		if (Utils.isNetworkConnected(getActivity())) {
			
			getActivity().setProgressBarIndeterminateVisibility(true);
			
			enableControls(false);
			
			LoginTask task = new LoginTask();
			task.execute(userName, password, Utils.getCurrentLanguage());
		}
		else {
			Toast toast = Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}
	
	private void enableControls(boolean enabled) {
		this.mLoginButton.setEnabled(enabled);
		this.mRegisterButton.setEnabled(enabled);
		this.mSignInImageButton.setEnabled(enabled);
	}

	// AsynTask class to handle Login Web Service call as separate UI Thread
	private class LoginTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "doInBackground: " + params.toString());

			// Invoke web service GetTokenByUserNameAndPasswordResult
			return performLoginTask(params[0], params[1], params[2]);
		}

		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "onPostExecute: ");
			
			enableControls(true);
			getActivity().setProgressBarIndeterminateVisibility(false);

			try {
				User my = null;
				
				if (result != null) {
					try {
						my = gson.fromJson(result, User.class);
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					if (my.getTOKEN() == null || my.getTOKEN().isEmpty()) {
						Toast toast = Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					} else {
						User.setUser(my);

						if (mRememberMeCheckBox.isChecked()) {
							String userName = mUserNameEditText.getText().toString();
							String password = mPasswordTextEdit.getText().toString();

							PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
											.putBoolean(PREF_REMEMBER_ME, true).putString(PREF_USERNAME, userName)
											.putString(PREF_PASSWORD, password).commit();
						} else {
							PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
											.putBoolean(PREF_REMEMBER_ME, false).remove(PREF_USERNAME).remove(PREF_PASSWORD)
											.commit();
						}
						
						Log.i(TAG, User.get().getTOKEN());

						mCallbacks.onLoginValiated();
					}
				}
				else {
					Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				Toast.makeText(getActivity(), R.string.prompt_system_error, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			// Log.i(TAG, "onPreExecute");
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

		// Method which invoke web method
		private String performLoginTask(String userName, String password, String language) {

			// Create request
			SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfUserAuthentication());

			request.addProperty(Utils.newPropertyInstance("userName", userName, String.class));
			request.addProperty(Utils.newPropertyInstance("password", password, String.class));
			request.addProperty(Utils.newPropertyInstance("language", language, String.class));

			// Create envelope
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			// Set output SOAP object
			envelope.setOutputSoapObject(request);
			// Create HTTP call object
			HttpTransportSE androidHttpTransport = new HttpTransportSE(Utils.getWsUrl());

			String responseJSON = null;

			try {
				// Invoke web service
				androidHttpTransport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfUserAuthentication(), envelope);

				// Get the response
				SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
				
				responseJSON = response.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseJSON;
		}

	}

}
