package com.teleknesis.c2dm.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//**************************************************************
//**************************************************************
// C2DMReceiver
//**************************************************************
//**************************************************************
public class C2DMReceiver extends BroadcastReceiver {
	
	private String mRegistrationId = "";
	
	//******************************************
	// onReceive
	//******************************************
    @Override
    public void onReceive(Context context, Intent intent) {
    	try {
    		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
    	        HandleRegistration(context, intent);
    	    	} 
    		else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
    	        HandleMessage(context, intent);
    	    	}
    		}
    	catch(Exception error) {
    		Log.e("C2DMSample", "onReceive: " + error.toString());
    		}
    	}
    
    
    //------------------------------------------
	// HandleRegistration
    //------------------------------------------
    private void HandleRegistration(Context context, Intent intent) {
    	try {
	        mRegistrationId = intent.getStringExtra("registration_id"); 
	        if (intent.getStringExtra("error") != null) {
	            Log.d("C2DMSample", "C2DMReceiver: RegistrationFailed");
	        	} 
	        else if (intent.getStringExtra("unregistered") != null) {
	        	Log.d("C2DMSample", "C2DMReceiver: Unregistered");
	        	} 
	        else if (mRegistrationId != null) {
	        	Log.d("C2DMSample", "C2DMReceiver: Key=" + mRegistrationId);
	        	
	        	// Send mRegistrationID to your server here
	        	
	        	// Then, if we want to let the Main activity know we are done, let's send
	        	// a broadcast message letting it know that
	        	Intent i = new Intent( "com.teleknesis.c2dm.sample.RegistrationComplete");
	        	i.putExtra("RegID", mRegistrationId);
	        	context.sendBroadcast(i);
	        	}
    		}
    	catch( Exception error ) {
    		Log.e( "C2DMReceiver", "HandleRegistration: ", error );
    		}
    	}
    
    
    //------------------------------------------
	// HandleMessage
    //------------------------------------------
    private void HandleMessage(Context context, Intent intent) {
    	try {
    		// pull the message sent from the server out of the intent.
    		// The key will be data.<key>, sent from the server
    		}
    	catch( Exception error ) {
    		Log.e( "C2DMSample", "HandleMessage: " + error.toString() );
    		}
    	}
     
	}
