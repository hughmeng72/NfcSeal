package com.freight_track.android.nfcseal;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MoreActivity extends ListActivity {
	
	private static final String BACKGROUND_COLOR = "#4682B4";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String[] values = new String[] {getString(R.string.label_position_diagnose), getString(R.string.label_clear_cache), getString(R.string.label_check_update), getString(R.string.label_about_us), getString(R.string.label_submit_feedbacks)};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
		
		setListAdapter(adapter);
		
		setupActionBar();
	}

	@Override
	  protected void onListItemClick(ListView l, View v, int position, long id) {
	    String item = (String) getListAdapter().getItem(position);
	    
	    if (item.equals(getString(R.string.label_position_diagnose))) {
			Intent i = new Intent(this, LocationDiagnosisActivity.class);
			startActivity(i);
	    }
	    else {
		    Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	    }

	  }


		@SuppressLint("NewApi")
		private void setupActionBar() {

			ActionBar actionBar = getActionBar();

			actionBar.setTitle(Utils.getFormatedTitle(getString(R.string.main_more)));
			actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(BACKGROUND_COLOR)));

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				actionBar.setIcon(R.drawable.ic_company);
			}
		}

}
