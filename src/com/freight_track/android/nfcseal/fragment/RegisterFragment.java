package com.freight_track.android.nfcseal.fragment;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.common.Utils;

public class RegisterFragment extends Fragment {
	
	private static final String TAG = "RegisterFragment";
	private static final String BACKGROUND_COLOR = "#ADD8E6";
	
	private EditText mFragment_register_companyNameEditText;
	private EditText mFragment_register_contactEditText;
	private EditText mFragment_register_phoneEditText;
	private EditText mFragment_register_emailEditText;
	private EditText mFragment_register_userNameEditText;
	private EditText mFragment_register_passwordEditText;
	private Button mFragment_login_registerButton;
	private EditText mFragment_register_confirmPasswordEditText;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_register, parent, false);
		
		mFragment_register_companyNameEditText = (EditText)v.findViewById(R.id.fragment_register_companyNameEditText);
		mFragment_register_contactEditText = (EditText)v.findViewById(R.id.fragment_register_contactEditText);
		mFragment_register_phoneEditText = (EditText)v.findViewById(R.id.fragment_register_phoneEditText);
		mFragment_register_emailEditText = (EditText)v.findViewById(R.id.fragment_register_emailEditText);
		mFragment_register_userNameEditText = (EditText)v.findViewById(R.id.fragment_register_userNameEditText);
		mFragment_register_passwordEditText = (EditText)v.findViewById(R.id.fragment_register_passwordEditText);
		mFragment_register_confirmPasswordEditText = (EditText)v.findViewById(R.id.fragment_register_confirmPasswordEditText);
		
		mFragment_login_registerButton = (Button)v.findViewById(R.id.fragment_login_registerButton);
		mFragment_login_registerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				register();
			}
		});
		
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

	private void register() {
		
		if (!validated()) return;
		
		if (Utils.isNetworkConnected(getActivity())) {
			
			getActivity().setProgressBarIndeterminateVisibility(true);
			
			RegisterTask task = new RegisterTask();
			task.execute(mFragment_register_userNameEditText.getText().toString(), mFragment_register_passwordEditText.getText().toString(), mFragment_register_emailEditText.getText().toString(), mFragment_register_companyNameEditText.getText().toString(), mFragment_register_contactEditText.getText().toString(), mFragment_register_phoneEditText.getText().toString(), Utils.getCurrentLanguage());
		}
		else {
			Toast toast = Toast.makeText(getActivity(), R.string.prompt_internet_connection_broken, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	private boolean validated() {

		if (mFragment_register_companyNameEditText.getText().toString().isEmpty()) {
			String waring = getString(R.string.prompt_no_enter) + getString(R.string.label_company_name);
			
			Toast toast = Toast.makeText(getActivity(), waring, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return false;
		}

		if (mFragment_register_contactEditText.getText().toString().isEmpty()) {
			String waring = getString(R.string.prompt_no_enter) + getString(R.string.label_contact);
			
			Toast toast = Toast.makeText(getActivity(), waring, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return false;
		}

		if (mFragment_register_phoneEditText.getText().toString().isEmpty()) {
			String waring = getString(R.string.prompt_no_enter) + getString(R.string.label_contact_phoneNumber);
			
			Toast toast = Toast.makeText(getActivity(), waring, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return false;
		}

		if (mFragment_register_emailEditText.getText().toString().isEmpty()) {
			String waring = getString(R.string.prompt_no_enter) + getString(R.string.label_email_address);
			
			Toast toast = Toast.makeText(getActivity(), waring, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return false;
		}

		if (mFragment_register_userNameEditText.getText().toString().isEmpty()) {
			String waring = getString(R.string.prompt_no_enter) + getString(R.string.login_user_name);
			
			Toast toast = Toast.makeText(getActivity(), waring, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return false;
		}

		if (mFragment_register_passwordEditText.getText().toString().isEmpty()) {
			String waring = getString(R.string.prompt_no_enter) + getString(R.string.hint_password);
			
			Toast toast = Toast.makeText(getActivity(), waring, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return false;
		}

		if (mFragment_register_confirmPasswordEditText.getText().toString().isEmpty()) {
			String waring = getString(R.string.prompt_no_enter) + getString(R.string.label_confirmed_password);
			
			Toast toast = Toast.makeText(getActivity(), waring, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return false;
		}

		if (!mFragment_register_passwordEditText.getText().toString().equals(mFragment_register_confirmPasswordEditText.getText().toString())) {
			Toast toast = Toast.makeText(getActivity(), R.string.prompt_password_not_match, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return false;
		}

		return true;
	}

	// AsynTask class to handle Login Web Service call as separate UI Thread
	private class RegisterTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "doInBackground: " + params.toString());

			// Invoke web service GetTokenByUserNameAndPasswordResult
			return performRegisterTask(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
		}

		@Override
		protected void onPreExecute() {
			// Log.i(TAG, "onPreExecute");
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

		// Method which invoke web method
		private String performRegisterTask(String userName, String password, String email, String companyName, String contactName, String phoneNumber, String language) {

			// Create request
			SoapObject request = new SoapObject(Utils.getWsNamespace(), Utils.getWsMethodOfCreateEnterpriseManager());

			request.addProperty(Utils.newPropertyInstance("userName", userName, String.class));
			request.addProperty(Utils.newPropertyInstance("password", password, String.class));
			request.addProperty(Utils.newPropertyInstance("email", email, String.class));
			request.addProperty(Utils.newPropertyInstance("enterprise", companyName, String.class));
			request.addProperty(Utils.newPropertyInstance("realName", contactName, String.class));
			request.addProperty(Utils.newPropertyInstance("mobile", "", String.class));
			request.addProperty(Utils.newPropertyInstance("phone", phoneNumber, String.class));
			request.addProperty(Utils.newPropertyInstance("isAcceptEmail", 0, int.class));
			request.addProperty(Utils.newPropertyInstance("isAcceptSMS", 0, int.class));
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
				androidHttpTransport.call(Utils.getWsSoapAction() + Utils.getWsMethodOfCreateEnterpriseManager(), envelope);

				// Get the response
				SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
				
				responseJSON = response.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseJSON;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "onPostExecute: " + result);
			
			getActivity().setProgressBarIndeterminateVisibility(false);

			getActivity().finish();
		}

	}
	
}
