package com.tumpaca.tumpaca.viewmodel;

import android.util.Log

class LoginActivityViewModel(mail: String, password: String) {
    final val TAG = "LoginActivityViewModel"

    var mail = mail
    var password = password

    fun login() {
        // TODO: ログイン処理
        Log.i(TAG, "login, mail=$mail, pass=${password.length}")
    }
}
