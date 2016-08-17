package com.dolbik.pavel.crypto;

import android.text.TextUtils;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


@InjectViewState
public class MyActivityPresenter extends MvpPresenter<MyActivityView> {

    private String       encryptedString;
    private String       password;
    private Subscription encryptedDataSbs;
    private Subscription decryptedDataSbs;


    public void encryptedData(final String text, final String password) {
        if(TextUtils.isEmpty(text) || TextUtils.isEmpty(password)) {
            getViewState().showError(R.string.error_encrypted);
        } else {
            this.password = password;
            encryptedDataSbs = Observable
                   .just(Crypto.encrypt(text, password))
                   .subscribeOn(Schedulers.computation())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Subscriber<String>() {
                       @Override
                       public void onCompleted() {}
                       @Override
                       public void onError(Throwable e) { e.printStackTrace(); }
                       @Override
                       public void onNext(String s) {
                           encryptedString = s;
                           getViewState().setEncryptedString(encryptedString);
                       }
                   });
        }
    }


    public void decryptedData(final String password) {
        if(TextUtils.isEmpty(encryptedString) || TextUtils.isEmpty(password)) {
            getViewState().showError(R.string.error_decrypted);
        } else if(!this.password.equals(password)) {
            getViewState().showError(R.string.error_password);
        } else {
            decryptedDataSbs = Observable
                    .just(Crypto.decrypt(encryptedString, password))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {}
                        @Override
                        public void onError(Throwable e) { e.printStackTrace(); }
                        @Override
                        public void onNext(String s) {
                            getViewState().setDecryptedString(s);
                        }
                    });
        }
    }


    public void clear() {
        encryptedString = null;
        password        = null;
        unsubscribe();
        getViewState().clear();
    }


    private void unsubscribe() {
        if(encryptedDataSbs != null) {
            encryptedDataSbs.unsubscribe();
            encryptedDataSbs = null;
        }
        if(decryptedDataSbs != null) {
            decryptedDataSbs.unsubscribe();
            decryptedDataSbs = null;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }
}
