package com.dolbik.pavel.crypto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.arellomobile.mvp.MvpDelegate;


public class MvpAppCompatActivity extends AppCompatActivity {

	private MvpDelegate<? extends MvpAppCompatActivity> mMvpDelegate;


	public MvpDelegate getMvpDelegate() {
		if (mMvpDelegate == null) {
			mMvpDelegate = new MvpDelegate<>(this);
		}
		return mMvpDelegate;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getMvpDelegate().onCreate(savedInstanceState);
	}


	@Override
	protected void onStart() {
		super.onStart();
		getMvpDelegate().onAttach();
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getMvpDelegate().onSaveInstanceState(outState);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		getMvpDelegate().onDetach();
		if (isFinishing()) {
			getMvpDelegate().onDestroy();
		}
	}

}
