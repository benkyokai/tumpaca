package com.tumpaca.tp.fragment.post

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.NativeExpressAdView
import com.tumpaca.tp.BuildConfig
import com.tumpaca.tp.R
import com.tumpaca.tp.util.configureForTest

class AdPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_ad, container, false)

        val adView = view.findViewById<NativeExpressAdView>(R.id.adView)
        val adRequest = AdRequest.Builder()
        if (BuildConfig.ADMOB_TEST) {
            adRequest.configureForTest()
        }
        adView.loadAd(adRequest.build())

        return view
    }
}
