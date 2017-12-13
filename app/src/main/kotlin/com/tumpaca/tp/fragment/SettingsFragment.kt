package com.tumpaca.tp.fragment

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
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.tumpaca.tp.BuildConfig
import com.tumpaca.tp.R
import com.tumpaca.tp.model.TPRuntime
import com.tumpaca.tp.util.configureForTest
import com.tumpaca.tp.util.getVersionName

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

        val reloadButton = view.findViewById<View>(R.id.reload)
        reloadButton.setOnClickListener {
            TPRuntime.tumblrService.resetPosts()
            activity.finish()
            activity.startActivity(activity.intent)
        }

        val logoutButton = view.findViewById<View>(R.id.logout)
        logoutButton.setOnClickListener {
            val fragment = LogoutDialogFragment()
            fragment.show(childFragmentManager, null)
        }

        val versionLabel = view.findViewById<TextView>(R.id.versionname)
        versionLabel.text = resources.getString(R.string.version_name, context.getVersionName())

        /**
         * 自分のポストを除外するかどうかの設定
         */
        val excludeMyPosts = view.findViewById<Switch>(R.id.exclude_my_posts)
        excludeMyPosts.isChecked = TPRuntime.settings.excludeMyPosts
        excludeMyPosts.setOnClickListener {
            TPRuntime.settings.excludeMyPosts = excludeMyPosts.isChecked
        }

        /**
         * 写真ポストを除外するかどうかの設定
         * 除外の場合はPHOTO, VIDEO, AUDIOを除外する
         */
        val excludePhoto = view.findViewById<Switch>(R.id.exclude_photo)
        excludePhoto.isChecked = TPRuntime.settings.excludePhoto
        excludePhoto.setOnClickListener {
            TPRuntime.settings.excludePhoto = excludePhoto.isChecked
        }

        /**
         * 写真を高画質で読み込むかどうかの設定
         */
        val highResolutionPhoto = view.findViewById<Switch>(R.id.high_resolution_photo)
        highResolutionPhoto.isChecked = TPRuntime.settings.highResolutionPhoto
        highResolutionPhoto.setOnClickListener {
            TPRuntime.settings.highResolutionPhoto = highResolutionPhoto.isChecked
        }

        val licenseButton = view.findViewById<View>(R.id.viewLicense)
        licenseButton.setOnClickListener {
            val uri = Uri.parse("https://github.com/benkyokai/tumpaca/blob/master/docs/LICENSE.md")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val adView = view.findViewById<AdView>(R.id.adView)
        if (TPRuntime.settings.showSettingsAd) {
            val adRequest = AdRequest.Builder()
            if (BuildConfig.ADMOB_TEST) {
                adRequest.configureForTest()
            }
            adView.loadAd(adRequest.build())
        } else {
            adView.visibility = View.GONE
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
                .setPositiveButton(R.string.yes) { _, _ ->
                    TPRuntime.tumblrService.logout()
                    activity.finish()
                    activity.startActivity(activity.intent)
                }.setNegativeButton(R.string.no, null)
                .create()
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}
