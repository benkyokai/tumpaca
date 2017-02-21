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

class AdPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.post_ad, container, false)

        val adView = view.findViewById(R.id.adView) as NativeExpressAdView
        val adRequest = AdRequest.Builder()
        if (BuildConfig.ADMOB_TEST) {
            adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        }
        adView.loadAd(adRequest.build())

        return view
    }
}
