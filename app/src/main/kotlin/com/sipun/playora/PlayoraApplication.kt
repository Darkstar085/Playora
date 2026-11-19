package com.sipun.playora

import android.app.Activity
import android.app.Application
import android.os.Bundle
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.sipun.playora.core.common.AppIconManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PlayoraApplication :
    Application(),
    SingletonImageLoader.Factory {

    private var activeActivityCount = 0

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(androidContext = this@PlayoraApplication)
            modules(appModule)
        }

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                activeActivityCount++
            }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                activeActivityCount--
                if (activeActivityCount <= 0) {
                    activeActivityCount = 0
                    AppIconManager.applyPendingIfAny(this@PlayoraApplication)
                }
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        org.koin.core.context.GlobalContext.get().get()
}
