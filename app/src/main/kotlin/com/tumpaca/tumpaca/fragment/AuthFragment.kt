package com.tumpaca.tumpaca.fragment

import android.os.Bundle
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.TumblrService

class AuthFragment: FragmentBase() {
    override fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fr_auth, container, false)
        val auth = view?.findViewById(R.id.authorize) as TextView
        auth.setOnClickListener {
            getMainApplication().tumblrService!!.auth(activity)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        getActionBar()?.hide()

        // Tumblr へのログインは別 Activity で行うが、ログインが終わるとその Activity は閉じられる。
        // ログインに成功したら、自動的に次の画面に遷移したいので onResume() でログイン状態をチェックする必要がある。
        val service: TumblrService = getMainApplication().tumblrService!!
        if (service.isLoggedIn) {
            replaceFragment(DashboardFragment(), false)
        }
    }
}
