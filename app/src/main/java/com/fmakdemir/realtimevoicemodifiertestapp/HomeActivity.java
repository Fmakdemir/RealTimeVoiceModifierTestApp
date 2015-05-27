package com.fmakdemir.realtimevoicemodifiertestapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fmakdemir.realtimevoicemodifier.RealTimeVoiceModifier;

public class HomeActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		realTimeVoiceModifier = new RealTimeVoiceModifier();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	RealTimeVoiceModifier realTimeVoiceModifier;
	int fsize = 1;
	double[] filter = new double[fsize];

	public void btnClicked(View v) {
		switch (v.getId()) {
			case R.id.btn_start:
				// reset filter
				fsize = 1; // default size
				filter = RealTimeVoiceModifier.DEFAULT_FILTER;
				realTimeVoiceModifier.setAudioFilter(filter);
				realTimeVoiceModifier.start();
				break;
			case R.id.btn_stop:
				realTimeVoiceModifier.stop();
				break;
			case R.id.btn_increase: // find next average filter with size of n^2
				// get next square
				fsize = (int)Math.sqrt(fsize)+1;
				fsize *= fsize;
				filter = new double[fsize];
				// generate filter of square size
				for (int i=0; i<fsize; ++i) {
					filter[i] = 1.d/fsize;
				}
				if (realTimeVoiceModifier != null) {
					realTimeVoiceModifier.setAudioFilter(filter);
				}
				break;
		}
	}
}
