package com.tumpaca.tumpaca.fragment

import android.content.Intent
import android.net.Uri
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

        val reloadButton = view.findViewById(R.id.reload)
        reloadButton.setOnClickListener {
            activity.finish()
            activity.startActivity(activity.intent)
        }

        val logoutButton = view.findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            // TODO 本当にログインしたのかダイアログで確認した方がいい
            TPRuntime.tumblrService.logout()
            replaceFragment(AuthFragment(), false)
        }

        val licenseButton = view.findViewById(R.id.viewLicense)
        licenseButton.setOnClickListener {
            val uri = Uri.parse("https://github.com/benkyokai/tumpaca/blob/master/docs/LICENSE.md")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }


        return view
    }
}