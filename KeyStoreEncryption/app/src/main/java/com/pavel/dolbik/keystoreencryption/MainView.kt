package com.pavel.dolbik.keystoreencryption

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType


interface MainView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setEncryptText(result: String)


    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setDecryptText(result: String)


    @StateStrategyType(AddToEndSingleStrategy::class)
    fun clear()

}