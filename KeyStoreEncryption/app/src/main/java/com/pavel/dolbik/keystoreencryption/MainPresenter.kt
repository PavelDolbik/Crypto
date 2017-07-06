package com.pavel.dolbik.keystoreencryption

import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter

@InjectViewState
class MainPresenter : MvpPresenter<MainView>() {

    private var encryptText: String = ""


    fun encrypt(planText: String) {
        if (!TextUtils.isEmpty(planText)) {
            KeyStoreHelper.createKeys()
            val key = KeyStoreHelper.getSigningKey()
            if (key != null) {
                encryptText = KeyStoreHelper.encrypt(planText)
                viewState.setEncryptText(encryptText)
            } else {
                // Если KeyStore не был найден, скорее всего API < 18. Можно использовать Crypto.
                // If KeyStore was not found, most likely API <18. We can use Crypto.
            }
        }
    }


    fun decryptText() {
        if (!TextUtils.isEmpty(encryptText)) {
            KeyStoreHelper.createKeys()
            val key = KeyStoreHelper.getSigningKey()
            if (key != null) {
                val decryptText = KeyStoreHelper.decrypt(encryptText)
                viewState.setDecryptText(decryptText)
            } else {
                // Если KeyStore не был найден, скорее всего API < 18. Можно использовать Crypto.
                // If KeyStore was not found, most likely API <18. We can use Crypto.
            }
        }
    }


    fun clear() {
        encryptText = ""
        viewState.clear()
    }

}