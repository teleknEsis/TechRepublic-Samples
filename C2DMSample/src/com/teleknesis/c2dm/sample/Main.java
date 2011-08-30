package com.teleknesis.c2dm.sample;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//*************************************************************
//*************************************************************
// Main
//*************************************************************
//*************************************************************
public class Main extends Activity {
    
	private ProgressDialog mDialog = null;

	//**********************************************
	// onCreate
	//**********************************************
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        SharedPreferences prefs = getSharedPreferences("Main", Context.MODE_PRIVATE);
	        if( prefs.contains("RegID") ) {
	        	((TextView)findViewById(R.id.RegID)).setText(prefs.getString("RegID", ""));
	        	}
    		}
    	catch( Exception error ) {
    		Log.e("C2DMSample", "Main_onCreate: " + error.toString() );
    		}
    	}
    
    
    //**********************************************
	// onCreate
	//**********************************************
    public void GetRegKeyClick(View v) {
    	try {
    		mDialog = ProgressDialog.show(this, "Registering","Please Wait...", true, true);
    		
    		// Register for the broadcast message C2DMReceiver will send after it is done registering
    		IntentFilter registrationFilter = new IntentFilter( "com.teleknesis.c2dm.sample.RegistrationComplete" );
			this.registerReceiver( mRegistrationCompleteReceiver, registrationFilter );
			
			// Start the registration service
    		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
			registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0)); // boilerplate
			registrationIntent.putExtra("sender", "<YOUR_EMAIL>");
			startService(registrationIntent);
    		}
    	catch( Exception error ) {
    		Log.e("C2DMSample", "GetRegKeyClick: " + error.toString() );
    		}
    	}
    
    
    //-----------------------------------
	// mRegistrationCompleteReceiver
	//-----------------------------------
	private BroadcastReceiver mRegistrationCompleteReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive( Context context, Intent i ) {
			mDialog.dismiss();
			((TextView)findViewById(R.id.RegID)).setText(i.getStringExtra("RegID"));
			SharedPreferences.Editor editor = context.getSharedPreferences("Main", Context.MODE_PRIVATE).edit();
			editor.putString("RegID", i.getStringExtra("RegID"));
			editor.commit();
			unregisterReceiver(this);
			}
		};
    
	}