package com.tumpaca.tumpaca.viewmodel;

import android.util.Log

interface HasLoginActivityViewModel {
    val loginActivityViewModel: LoginActivityViewModel
}

interface LoginActivityViewModel {
    var mail: String?
    var password: String?
    fun set(mail: String, password: String)
    fun login()
}

object MixInLoginActivityViewModel: HasLoginActivityViewModel {
    override val loginActivityViewModel: LoginActivityViewModel
        get() = LoginActivityViewModelImpl
}

object LoginActivityViewModelImpl : LoginActivityViewModel {
    final val TAG = "LoginActivityViewModel"

    override var mail: String? = null
    override var password: String? = null

    override fun set(mail: String, password: String) {
        this.mail = mail
        this.password = password
    }

    override fun login() {
        Log.i(TAG, "login, mail=$mail, pass=${password?.length}")
    }
}