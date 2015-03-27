package com.mobile.myfriendsapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MainActivity extends Activity {

	private int mBindFlag;
	private Messenger mServiceMessenger;
	private static Context activityContext;
	private Messenger mServerMessenger;
	private static MyService myService ;  
	//private static VoiceCommandService voiceCommandService;
	private static Intent service;

	//Oncreate function executed during first initialization 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		Log.d("myfriend" , "inoncreate mainactivity");
		//Starting service MyService service  
	    Intent service = new Intent(this, MyService.class);
	    this.startService(service);
	    mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
		//Loading main activity
	    setContentView(R.layout.activity_main);		
	}
	
	//onStart executed when program execution is started
	@Override
	protected void onStart() {
		super.onStart();
		Log.d("akd", "inside onstart");
		//super.onStart();
		//binding service with this activity 
	    bindService(new Intent(this, MyService.class), mServiceConnection, mBindFlag);
	}

	//onStop executed when the program execution is stopped (back button) 
	@Override
	protected void onStop() {
		super.onStop();

		if (mServiceMessenger != null) {
			//service is unbound from the activity 
			unbindService(mServiceConnection);
			mServiceMessenger = null;
		}
	}

	//init function is idle as of now 
	public static void init(Context context) {
		MyService myService = new MyService();
		activityContext = context;
	}

	//Defining a service connection 
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("myfriend" , "service connected");
			//new messenger object created 
			mServiceMessenger = new Messenger(service);
			Message msg = new Message();
			//message is passed to the service 
			msg.what = MyService.MSG_RECOGNIZER_START_LISTENING;

			try {
				Log.d("myfriend", "tried sending message");
				mServiceMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//if (DEBUG) {Log.d(TAG, "onServiceDisconnected");} //$NON-NLS-1$
			
			mServiceMessenger = null;
		}

	}; // mServiceConnection

	
}