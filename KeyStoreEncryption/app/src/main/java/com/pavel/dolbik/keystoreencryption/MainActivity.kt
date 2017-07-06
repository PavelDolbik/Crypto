package com.pavel.dolbik.keystoreencryption

import android.os.Bundle
import android.support.transition.TransitionManager
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : MvpAppCompatActivity(), MainView {

    @InjectPresenter lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        encrypt.setOnClickListener { presenter.encrypt(planText.text.toString().trim())}
        decrypt.setOnClickListener { presenter.decryptText() }
        clear.setOnClickListener   { presenter.clear() }
    }


    override fun setEncryptText(result: String) {
        encryptResult?.text = result
    }


    override fun setDecryptText(result: String) {
        decryptResult?.text = result
    }


    override fun clear() {
        TransitionManager.beginDelayedTransition(container)
        planText.setText("")
        planText?.setText("")
        encryptResult?.text = ""
        decryptResult?.text = ""
    }

}
