package com.dolbik.pavel.crypto;

import android.text.TextUtils;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


@InjectViewState
public class MyActivityPresenter extends MvpPresenter<MyActivityView> {

    private String encryptedString;
    private String password;
    private CompositeSubscription compositeSubscription;


    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        compositeSubscription = new CompositeSubscription();
    }


    public void encryptedData(final String text, final String password) {
        if(TextUtils.isEmpty(text) || TextUtils.isEmpty(password)) {
            getViewState().showError(R.string.error_encrypted);
        } else {
            this.password = password;
            compositeSubscription.add(
                    Observable
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
                   }));
        }
    }


    public void decryptedData(final String password) {
        if(TextUtils.isEmpty(encryptedString) || TextUtils.isEmpty(password)) {
            getViewState().showError(R.string.error_decrypted);
        } else if(!this.password.equals(password)) {
            getViewState().showError(R.string.error_password);
        } else {
            compositeSubscription.add(
                    Observable
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
                    }));
        }
    }


    public void clear() {
        encryptedString = null;
        password        = null;
        compositeSubscription.unsubscribe();
        getViewState().clear();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }
}
