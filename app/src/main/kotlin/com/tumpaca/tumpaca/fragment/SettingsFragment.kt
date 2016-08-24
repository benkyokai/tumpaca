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

    interface SettingsFragmentListener {
        fun hideSettings()
        fun reload()
    }

    var listener: SettingsFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fr_settings, container, false)

        val reloadButton = view.findViewById(R.id.reload_button)
        reloadButton.setOnClickListener {
            doReload()
        }

        val logoutButton = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            doLogout()
        }

        val backButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            listener?.hideSettings()
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context !is SettingsFragmentListener) {
            throw UnsupportedOperationException("SettingsFragmentListener is not implementation.")
        } else {
            listener = context
        }
    }

    private fun doReload() {
        listener?.reload()
    }

    private fun doLogout() {
        // TODO 本当にログインしたのかダイアログで確認した方がいい
        TPRuntime.tumblrService!!.logout()
        replaceFragment(AuthFragment(), false)
    }

}