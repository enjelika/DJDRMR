package edu.uco.sdd.spring15.dj_drmr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class Login extends Activity
{
		@Override
		public void onCreate(Bundle savedInstanceState) 
		{
			super.onCreate(savedInstanceState);
			// Get the view from new_activity.xml
			setContentView(R.layout.login);
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) 
		{
			getMenuInflater().inflate(R.menu.djdrmr_main, menu);
			return true;
		}
}
