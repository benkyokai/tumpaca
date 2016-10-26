package com.tumpaca.tumpaca.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
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
            TPRuntime.tumblrService.resetPosts()
            activity.finish()
            activity.startActivity(activity.intent)
        }

        val logoutButton = view.findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            // TODO 本当にログインしたのかダイアログで確認した方がいい
            TPRuntime.tumblrService.logout()
            replaceFragment(AuthFragment(), false)
        }

        /**
         * 自分のポストを除外するかどうかの設定
         */
        val excludeMyPosts = view.findViewById(R.id.exclude_my_posts) as Switch
        excludeMyPosts.isChecked = TPRuntime.settings.isExcludeMyPosts()
        excludeMyPosts.setOnClickListener {
            TPRuntime.settings.setExcludeMyPosts(excludeMyPosts.isChecked)
        }

        /**
         * 写真ポストを除外するかどうかの設定
         * 除外の場合はPHOTO, VIDEO, AUDIOを除外する
         */
        val excludePhoto = view.findViewById(R.id.exclude_photo) as Switch
        excludePhoto.isChecked = TPRuntime.settings.isExcludePhoto()
        excludePhoto.setOnClickListener {
            TPRuntime.settings.setExcludePhoto(excludePhoto.isChecked)
        }

        /**
         * 写真を高画質で読み込むかどうかの設定
         */
        val highResolutionPhoto = view.findViewById(R.id.high_resolution_photo) as Switch
        highResolutionPhoto.isChecked = TPRuntime.settings.isHighResolutionPhoto()
        highResolutionPhoto.setOnClickListener {
            TPRuntime.settings.setHighResolutionPhoto(highResolutionPhoto.isChecked)
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