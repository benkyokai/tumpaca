package com.tumpaca.tumpaca.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.SimpleTextWatcher
import com.tumpaca.tumpaca.viewmodel.HasLoginActivityViewModel
import com.tumpaca.tumpaca.viewmodel.LoginActivityViewModel
import com.tumpaca.tumpaca.viewmodel.MixInLoginActivityViewModel

class LoginActivity : AppCompatActivity(), HasLoginActivityViewModel by MixInLoginActivityViewModel {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // layout ファイルから View を生成してルートビューにセット。
        setContentView(R.layout.activity_login)

        //
        // ViewModel を生成
        // もし、保存されていた情報があれば復元
        //
        val mail = savedInstanceState?.getString("mail")
        val password = savedInstanceState?.getString("password")
        loginActivityViewModel.set(mail ?: "", password ?: "")

        //
        // ビューとバインド
        // ただし、これだと UI => ViewModel しかバインドできてない
        //
        val mailView = this.findViewById(R.id.mail) as EditText?
        mailView?.addTextChangedListener(object: SimpleTextWatcher() {
            override fun onTextChanged(a: CharSequence, start : Int, before : Int, count : Int) {
                loginActivityViewModel.mail = a.toString()
            }
        })
        val passwordView = this.findViewById(R.id.password) as EditText?
        passwordView?.addTextChangedListener(object: SimpleTextWatcher() {
            override fun onTextChanged(a: CharSequence, start : Int, before : Int, count : Int) {
                loginActivityViewModel.password = a.toString()
            }
        });

        val loginButton = this.findViewById(R.id.login) as Button?
        loginButton?.setOnClickListener { v ->
            loginActivityViewModel.login()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // ViewModel の保存
        outState.putString("mail", loginActivityViewModel.mail)
        outState.putString("password", loginActivityViewModel.password)
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}