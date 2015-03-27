package com.mobile.myfriendsapp;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class WordDetected extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("myFriend" , "detected hello");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_word_detected);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_word_detected, menu);
		return true;
	}

}
