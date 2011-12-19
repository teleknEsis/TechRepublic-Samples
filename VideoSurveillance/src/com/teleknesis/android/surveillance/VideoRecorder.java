package com.teleknesis.android.surveillance;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;
import com.teleknesis.android.surveillance.utils.Logger;

//*******************************************************
//*******************************************************
// CamaraView
//*******************************************************
//*******************************************************
public class VideoRecorder extends Activity implements OnClickListener, SurfaceHolder.Callback {
    MediaRecorder mRecorder;
    SurfaceHolder mHolder;
    boolean mRecording = false;
    boolean mStop = false;
    boolean mPrepared = false;
    boolean mLoggedIn = false;
	private PowerManager.WakeLock mWakeLock;
	private ArrayList<String> mFiles = new ArrayList<String>();
	DropboxAPI<AndroidAuthSession> mApi;
	private static final String APP_KEY = "YOUR_APP_KEY";
	private static final String APP_SECRET = "YOUR_APP_SECRET";
	final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    private Timer mStopTimer;
    
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

    mRecorder = new MediaRecorder();
    initRecorder();
    setContentView(R.layout.camera);

    SurfaceView cameraView = (SurfaceView) findViewById(R.id.surface_camera);
    mHolder = cameraView.getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    cameraView.setClickable(true);
    cameraView.setOnClickListener(this);

	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Ping");
	mWakeLock.acquire();
	
	try {
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		checkAppKeySetup();
		mApi.getSession().startAuthentication(this);
		} 
	catch (Exception e) {
		Logger.Error( "onCreate", e);
		}
}


@Override
protected void onDestroy() {
    try {
    	super.onDestroy();
        Logger.Debug("onStop");
		mWakeLock.release();
		logOut();
		} 
    catch (Exception e) {
		Logger.Error( "VideoRecorderTest_onStop", e );
		}
	}


@Override
public void onResume() {
	super.onResume();
	AndroidAuthSession session = mApi.getSession();
	if (session.authenticationSuccessful()) {
        try {
            // Mandatory call to complete the auth
            session.finishAuthentication();

            // Store it locally in our app for later use
            TokenPair tokens = session.getAccessTokenPair();
            storeKeys(tokens.key, tokens.secret);
            setLoggedIn(true);
        } catch (IllegalStateException e) {
            Toast.makeText(this,"Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Logger.Error( "onCreate", e);
        }
    }
	}

private AndroidAuthSession buildSession() {
    AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
    AndroidAuthSession session;

    String[] stored = getKeys();
    if (stored != null) {
        AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
        session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
    } else {
        session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
    }

    return session;
}


private String[] getKeys() {
    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
    String key = prefs.getString(ACCESS_KEY_NAME, null);
    String secret = prefs.getString(ACCESS_SECRET_NAME, null);
    if (key != null && secret != null) {
    	String[] ret = new String[2];
    	ret[0] = key;
    	ret[1] = secret;
    	return ret;
    } else {
    	return null;
    }
}

/**
 * Shows keeping the access keys returned from Trusted Authenticator in a local
 * store, rather than storing user name & password, and re-authenticating each
 * time (which is not to be done, ever).
 */
private void storeKeys(String key, String secret) {
    // Save the access key for later
    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
    Editor edit = prefs.edit();
    edit.putString(ACCESS_KEY_NAME, key);
    edit.putString(ACCESS_SECRET_NAME, secret);
    edit.commit();
}

private void clearKeys() {
    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
    Editor edit = prefs.edit();
    edit.clear();
    edit.commit();
}


private void checkAppKeySetup() {
    

    // Check if the app has set up its manifest properly.
    Intent testIntent = new Intent(Intent.ACTION_VIEW);
    String scheme = "db-" + APP_KEY;
    String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
    testIntent.setData(Uri.parse(uri));
    PackageManager pm = getPackageManager();
    if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
        Toast.makeText(this, "URL scheme in your app's " +
                "manifest is not set up correctly. You should have a " +
                "com.dropbox.client2.android.AuthActivity with the " +
                "scheme: " + scheme, Toast.LENGTH_SHORT).show();
        finish();
    }
}

private void logOut() {
    // Remove credentials from the session
    mApi.getSession().unlink();

    // Clear our stored keys
    clearKeys();
    // Change UI state to display logged out version
    setLoggedIn(false);
}

private void setLoggedIn(boolean loggedIn) {
	mLoggedIn = loggedIn;
}

private void initRecorder() {
    mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
	mRecorder.setMaxDuration((int) 60000);
	mRecorder.setVideoSize(320, 240); 
	mRecorder.setVideoFrameRate(15);
	
    File mediaStorageDir = new File("/sdcard/Surveillance/");
    if ( !mediaStorageDir.exists() ) {
        if ( !mediaStorageDir.mkdirs() ){
            Logger.Debug("failed to create directory");
        	}
    	}
    String filePath = "/sdcard/Surveillance/" + "VID_"+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
    mFiles.add(filePath);
    mRecorder.setOutputFile(filePath);
}

private void prepareRecorder() {
	Logger.Debug( "About to prepare" );
	mRecorder.setPreviewDisplay(mHolder.getSurface());
	
    try {
        mRecorder.prepare();
        mPrepared = true;
        Logger.Debug( "right after prepare");
    } catch (IllegalStateException e) {
        Logger.Error("prepareRecorder_IllegalStateException", e);
        //finish();
    } catch (IOException e) {
    	Logger.Error("prepareRecorder_IOException", e);
        //finish();
    } catch( Exception e) {
    	Logger.Error("prepareRecorder", e);
    	}
    Logger.Debug("after prepare");
    
}

public void onClick(View v) {
    try {
		if (mRecording) {
			Logger.Debug("Stopping");
			mRecorder.reset();
			mPrepared = false;
			mRecording = false;
			this.initRecorder();
			this.prepareRecorder();
			mStopTimer.cancel();
			} 
		else {
			//prepareRecorder();
			mRecorder.start();
			mRecording = true;
			mStopTimer = new Timer();
			mStopTimer.schedule( new TimerTask() {
				@Override
				public void run() {
					Looper.prepare();
					restartRecorder();
					}
				}, 60 * 1000 );
			}
		} 
    catch (Exception e) {
		Logger.Error( "onClick", e );
		}
	}

public void restartRecorder() {
	Logger.Debug("'bout to restart recorder");
	mRecorder.reset();
	mPrepared = false;
	mRecording = false;
	initRecorder();
	prepareRecorder();
	//Start uploading the last file
	new UploadPicture(VideoRecorder.this, mApi, "/", new File(mFiles.get(mFiles.size()-2)) ).execute();
	mRecorder.start();
	mRecording = true;
	mStopTimer = new Timer();
	mStopTimer.schedule( new TimerTask() {
		@Override
		public void run() {
			Looper.prepare();
			restartRecorder();
			}
		}, 60 * 1000 );
	}

public void surfaceCreated(SurfaceHolder holder) {
	if( !mPrepared ) {
		prepareRecorder();
		}
	}

public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

public void surfaceDestroyed(SurfaceHolder holder) {
    if (mRecording) {
        mRecorder.stop();
        mRecording = false;
        //mRecorder.release();
    	}
    mRecorder.release();
	}
}