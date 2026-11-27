package com.sipun.playora.feature.player.extensions

import android.app.Activity
import android.provider.Settings
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.toggleSystemBars(showBars: Boolean, @Type.InsetsType types: Int = Type.systemBars()) {
    WindowCompat.getInsetsController(
        window,
        window.decorView,
    ).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (showBars) show(types) else hide(types)
    }
}

val Activity.currentBrightness: Float
    get() = when (val brightness = window.attributes.screenBrightness) {
        in BRIGHTNESS_OVERRIDE_OFF..BRIGHTNESS_OVERRIDE_FULL ->
            brightness

        else -> Settings.System.getFloat(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
        ) / 255
    }

val Activity.brightnessPercentage: Int
    get() = (currentBrightness / BRIGHTNESS_OVERRIDE_FULL * 100).toInt()
