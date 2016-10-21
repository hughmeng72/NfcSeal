package com.freight_track.android.nfcseal;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ListActivity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class AboutMeActivity extends ListActivity {
	
	private static final String BACKGROUND_COLOR = "#4682B4";

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String[] values = new String[] {getString(R.string.label_personal_info), getString(R.string.label_change_password)};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
		
		setListAdapter(adapter);
		
		setupActionBar();
	}

	@Override
	  protected void onListItemClick(ListView l, View v, int position, long id) {
	    String item = (String) getListAdapter().getItem(position);
	    Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	  }


		@SuppressLint("NewApi")
		private void setupActionBar() {

			ActionBar actionBar = getActionBar();

			actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.main_aboutMe)));
			actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				actionBar.setIcon(R.drawable.ic_company);
			}
		}

}
