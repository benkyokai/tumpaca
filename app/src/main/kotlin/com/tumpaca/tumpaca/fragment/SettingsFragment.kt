package com.tumpaca.tumpaca.fragment

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
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
            val fragment = LogoutDialogFragment()
            fragment.show(childFragmentManager, null)
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

// 直接 Builder から show() すると Activity 破棄時に memory leak するらしいので
// DialogFragment で包んであげる。
// ref: http://qiita.com/suzukihr/items/8973527ebb8bb35f6bb8
class LogoutDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setMessage(R.string.ensure_logout)
                .setPositiveButton(R.string.yes, { dialogInterface, i ->
                    TPRuntime.tumblrService.logout()
                    val transaction = activity.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, AuthFragment(), tag)
                    transaction.commit()
                })
                .setNegativeButton(R.string.no, null)
                .create()
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}
