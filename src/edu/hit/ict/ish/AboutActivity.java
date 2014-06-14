package edu.hit.ict.ish;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends Activity {

	private TextView tvAppInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);

		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setHomeButtonEnabled(true);

		tvAppInfo = (TextView) findViewById(R.id.about_tv_app_info);

		// set the APP INFO
		tvAppInfo.setText(getResources().getString(R.string.app_name) + " v"
				+ getResources().getString(R.string.app_version));
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	// This is called when the Home (Up) button is pressed
	            // in the Action Bar.
	            
	        	//Intent intent = new Intent(AboutActivity.this, MainActivity.class);
	            //startActivity(intent);
	            finish();
	            break;
	            
	    }
	    
	    return super.onOptionsItemSelected(item);
	}

}
