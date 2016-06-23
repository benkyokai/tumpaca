package com.tumpaca.tumpaca.fragment

import android.os.Bundle
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.TumblerService

class AuthFragment: FragmentBase() {
    override fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fr_auth, container, false)
        val authButton = view?.findViewById(R.id.authorize) as Button
        authButton.setOnClickListener {
            getMainApplication().tumblerService!!.auth(activity);
        }
        return view
    }

    override fun onResume() {
        super.onResume()

        // Tumbler へのログインを別 Activity で行ったあと、その Activity は閉じられる。
        // ログインに成功したら、自動的に次の画面に遷移したいので onResume() でログイン状態をチェックする必要がある。
        val service: TumblerService = getMainApplication().tumblerService!!
        if (service.isLogin) {
            replaceFragment(DashboardFragment(), false)
        }
    }
}
