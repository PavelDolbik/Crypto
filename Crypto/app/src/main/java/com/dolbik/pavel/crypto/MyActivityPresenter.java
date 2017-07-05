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
    private CompositeSubscription compositeSbs;


    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        compositeSbs = new CompositeSubscription();
    }


    void encryptedData(final String text, final String password) {
        if(TextUtils.isEmpty(text) || TextUtils.isEmpty(password)) {
            getViewState().showError(R.string.error_encrypted);
        } else {
            this.password = password;
            compositeSbs.add(
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


    void decryptedData(final String password) {
        if(TextUtils.isEmpty(encryptedString) || TextUtils.isEmpty(password)) {
            getViewState().showError(R.string.error_decrypted);
        } else if(!this.password.equals(password)) {
            getViewState().showError(R.string.error_password);
        } else {
            compositeSbs.add(
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


    void clear() {
        encryptedString = null;
        password        = null;
        compositeSbs.unsubscribe();
        getViewState().clear();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSbs.unsubscribe();
    }


}
