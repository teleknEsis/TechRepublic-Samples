package com.teleknesis.android.surveillance;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import com.teleknesis.android.peepshow.utils.Logger;

public class Home extends Activity {
	
	private boolean mRecording = false;
	private MediaRecorder mRecorder;
	private Camera mCamera;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);
			mRecorder = new MediaRecorder();
			} 
        catch (Exception error) {
			Logger.Error( "Home_onCreate", error );
			}
    	}
    
    
    public void LaunchCameraActivity( View v ) {
    	startActivityForResult( new Intent(Home.this, VideoRecorder.class), 1000 );
    	}
    
    
    
    
    
}