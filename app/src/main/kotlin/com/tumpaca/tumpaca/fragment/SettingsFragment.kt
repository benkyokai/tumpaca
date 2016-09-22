package com.tumpaca.tumpaca.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime

/**
 * 設定画面
 * Created by Yuichi Yabu on 8/22/16.
 */
class SettingsFragment : FragmentBase() {
    companion object {
        private const val TAG = "SettingsFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fr_settings, container, false)

        val reloadButton = view.findViewById(R.id.reload_button)
        reloadButton.setOnClickListener {
            activity.finish()
            activity.startActivity(activity.intent)
        }

        val logoutButton = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            // TODO 本当にログインしたのかダイアログで確認した方がいい
            TPRuntime.tumblrService.logout()
            replaceFragment(AuthFragment(), false)
        }

        val backButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            fragmentManager.popBackStack()
        }

        return view
    }
}