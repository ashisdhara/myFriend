package com.mobile.myfriendsapp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class MyService extends Service {
	protected static AudioManager mAudioManager;
	protected SpeechRecognizer mSpeechRecognizer;
	protected Intent mSpeechRecognizerIntent;
	protected final Messenger mServerMessenger = new Messenger(
			new IncomingHandler(this));

	protected boolean mIsListening;
	protected volatile boolean mIsCountDownOn;
	private static boolean mIsStreamSolo;

	static final int MSG_RECOGNIZER_START_LISTENING = 1;
	static final int MSG_RECOGNIZER_CANCEL = 2;

	@Override
	public void onCreate() {
		Log.d("myfriend", "in oncreate myservice");
		super.onCreate();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizer
				.setRecognitionListener(new SpeechRecognitionListener());
		mSpeechRecognizerIntent = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(
				RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
	}
	
	

	protected static class IncomingHandler extends Handler {
		private WeakReference<MyService> mtarget;

		IncomingHandler(MyService target) {
			mtarget = new WeakReference<MyService>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			final MyService target = mtarget.get();
			//Log.d("myfriend", "inside handle message");
			switch (msg.what) {
			case MSG_RECOGNIZER_START_LISTENING:
				//Log.d("myfriend", "message received");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					// turn off beep sound
					if (!mIsStreamSolo) {
						mAudioManager.setStreamSolo(
								AudioManager.STREAM_VOICE_CALL, true);
						mIsStreamSolo = true;
					}
				}
				if (!target.mIsListening) {
					target.mSpeechRecognizer
							.startListening(target.mSpeechRecognizerIntent);
					target.mIsListening = true;
				
				}
				break;

			case MSG_RECOGNIZER_CANCEL:
				if (mIsStreamSolo) {
					mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL,
							false);
					mIsStreamSolo = false;
				}
				target.mSpeechRecognizer.cancel();
				target.mIsListening = false;
				
				break;
			}
		}
	}

	// Count down timer for Jelly Bean work around
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFinish() {
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
			try {
				mServerMessenger.send(message);
				message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
				mServerMessenger.send(message);
			} catch (RemoteException e) {

			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mIsCountDownOn) {
			mNoSpeechCountDown.cancel();
		}
		if (mSpeechRecognizer != null) {
			mSpeechRecognizer.destroy();
		}
	}

	protected class SpeechRecognitionListener implements RecognitionListener {

		@Override
		public void onBeginningOfSpeech() {
			Log.d("myfriend", "speech recog");
			// speech input will be processed, so there is no need for count
			// down anymore
			if (mIsCountDownOn) {
				mIsCountDownOn = false;
				mNoSpeechCountDown.cancel();
			}
			
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			Log.d("myfriend", "onBuffer");
		}

		@Override
		public void onEndOfSpeech() {
			Log.d("myfriend", "end of speech");
		}

		@Override
		public void onError(int error) {
			if (mIsCountDownOn) {
				mIsCountDownOn = false;
				mNoSpeechCountDown.cancel();
			}
			mIsListening = false;
			Message message = Message.obtain(null,
					MSG_RECOGNIZER_START_LISTENING);
			try {
				mServerMessenger.send(message);
			} catch (RemoteException e) {

			}
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			Log.d("myfriend", "onEvents");
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			Log.d("myfriend", "onPartialresults");
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				mIsCountDownOn = true;
				mNoSpeechCountDown.start();

			}
			Log.d("myfriend", "onReadyForSpeech");
		}

		@Override
		public void onResults(Bundle results) {
			
			Log.d("myfriend", "onResults");
			  ArrayList strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			  for (int i = 0; i < strlist.size();i++ ) {
			   Log.d("myfriend", "result=" + strlist.get(i));
			   
			   mIsListening = false;
				Message message = Message.obtain(null,
						MSG_RECOGNIZER_START_LISTENING);
				try {
					mServerMessenger.send(message);
				} catch (RemoteException e) {

				}
			  }
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			//Log.d("myfriend", "onRmsChanged");
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("myfriend", "onBind");

		return mServerMessenger.getBinder();
	}

}