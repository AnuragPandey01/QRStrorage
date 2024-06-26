package com.glitchcraftlabs.qrstorage.app

import android.app.Application
import com.facebook.ads.AudienceNetworkAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QRApp: Application(){
    override fun onCreate() {
        super.onCreate()
        AudienceNetworkAds.initialize(this);
    }
}