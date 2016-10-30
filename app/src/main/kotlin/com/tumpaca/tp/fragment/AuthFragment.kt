package com.tumpaca.tp.fragment

import android.os.Bundle
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tumpaca.tp.R
import com.tumpaca.tp.model.TPRuntime
import com.tumpaca.tp.util.isOnline

class AuthFragment : FragmentBase() {
    override fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fr_auth, container, false)
        val auth = view?.findViewById(R.id.authorize)
        auth?.setOnClickListener {
            auth()
        }
        val thumbnail = view?.findViewById(R.id.thumbnail)
        thumbnail?.setOnClickListener {
            auth()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        getActionBar()?.hide()

        // Tumblr へのログインは別 Activity で行うが、ログインが終わるとその Activity は閉じられる。
        // ログインに成功したら、自動的に次の画面に遷移したいので onResume() でログイン状態をチェックする必要がある。
        if (TPRuntime.tumblrService.isLoggedIn) {
            replaceFragment(DashboardFragment(), false, DashboardFragment.TAG)
        }
    }

    private fun auth() {
        if (context.isOnline()) {
            TPRuntime.tumblrService.auth(activity)
        } else {
            Toast.makeText(context, R.string.offline_toast, Toast.LENGTH_SHORT).show()
        }
    }
}
