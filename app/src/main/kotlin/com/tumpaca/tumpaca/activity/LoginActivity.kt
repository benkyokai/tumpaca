package com.tumpaca.tumpaca.activity

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.SimpleTextWatcher
import com.tumpaca.tumpaca.viewmodel.LoginActivityViewModel

class LoginActivity : AppCompatActivity() {

    var vm: LoginActivityViewModel? = null

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
        vm = LoginActivityViewModel(mail ?: "", password ?: "")

        //
        // ビューとバインド
        // ただし、これだと UI => ViewModel しかバインドできてない
        //
        val mailView = this.findViewById(R.id.mail) as EditText?
        mailView?.addTextChangedListener(object: SimpleTextWatcher() {
            override fun onTextChanged(a: CharSequence, start : Int, before : Int, count : Int) {
                vm?.mail = a.toString()
            }
        })
        val passwordView = this.findViewById(R.id.password) as EditText?
        passwordView?.addTextChangedListener(object: SimpleTextWatcher() {
            override fun onTextChanged(a: CharSequence, start : Int, before : Int, count : Int) {
                vm?.password = a.toString()
            }
        });

        val loginButton = this.findViewById(R.id.login) as Button?
        loginButton?.setOnClickListener { v ->
            vm?.login()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // ViewModel の保存
        outState.putString("mail", vm?.mail)
        outState.putString("password", vm?.password)
    }


    override fun onDestroy() {
        // View が破棄されるので ViewModel も破棄
        vm = null;
        super.onDestroy()
    }
}
