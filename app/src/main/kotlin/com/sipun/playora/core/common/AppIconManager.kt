package com.sipun.playora.core.common

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

enum class AppIconPreset(
    val aliasSuffix: String,
    val color: Int,
) {
    DYNAMIC("", 0),
    BLUE(".MainActivityBlue", 0xFF2196F3.toInt()),
    RED(".MainActivityRed", 0xFFE92B2B.toInt()),
    CORAL(".MainActivityCoral", 0xFFE57373.toInt()),
    TEAL(".MainActivityTeal", 0xFF26A69A.toInt()),
    GREEN(".MainActivityGreen", 0xFF66BB6A.toInt()),
    LIGHT_GREEN(".MainActivityLightGreen", 0xFF9CCC65.toInt()),
    AMBER(".MainActivityAmber", 0xFFFFB74D.toInt()),
    DEEP_ORANGE(".MainActivityDeepOrange", 0xFFFF7043.toInt()),
    PURPLE(".MainActivityPurple", 0xFFAB47BC.toInt()),
    DEEP_PURPLE(".MainActivityDeepPurple", 0xFF7E57C2.toInt()),
    INDIGO(".MainActivityIndigo", 0xFF5C6BC0.toInt()),
    CYAN(".MainActivityCyan", 0xFF26C6DA.toInt()),
    LIME(".MainActivityLime", 0xFF8BC34A.toInt()),
    ORANGE(".MainActivityOrange", 0xFFFFA726.toInt());

    fun getComponentName(packageName: String): ComponentName {
        val className = if (aliasSuffix.isEmpty()) {
            "$packageName.MainActivity"
        } else {
            packageName + aliasSuffix
        }
        return ComponentName(packageName, className)
    }

    companion object {
        fun findClosest(colorInt: Int): AppIconPreset {
            val colorPresets = entries.filter { it != DYNAMIC }
            val exact = colorPresets.firstOrNull { it.color == colorInt }
            if (exact != null) return exact

            val r = (colorInt shr 16) and 0xFF
            val g = (colorInt shr 8) and 0xFF
            val b = colorInt and 0xFF

            var closest = BLUE
            var minDistance = Long.MAX_VALUE

            for (preset in colorPresets) {
                val pr = (preset.color shr 16) and 0xFF
                val pg = (preset.color shr 8) and 0xFF
                val pb = preset.color and 0xFF

                val dr = (r - pr).toLong()
                val dg = (g - pg).toLong()
                val db = (b - pb).toLong()
                val dist = dr * dr + dg * dg + db * db

                if (dist < minDistance) {
                    minDistance = dist
                    closest = preset
                }
            }
            return closest
        }
    }
}

object AppIconManager {
    @Volatile
    private var pendingCustomColor: Int? = null
    @Volatile
    private var pendingUseDynamicColors: Boolean? = null

    fun setPending(customColor: Int, useDynamicColors: Boolean) {
        pendingCustomColor = customColor
        pendingUseDynamicColors = useDynamicColors
    }

    fun applyPendingIfAny(context: Context) {
        val customColor = pendingCustomColor
        val useDynamicColors = pendingUseDynamicColors ?: false
        if (customColor == null) return

        pendingCustomColor = null
        pendingUseDynamicColors = null

        val targetPreset = if (useDynamicColors) {
            AppIconPreset.DYNAMIC
        } else {
            AppIconPreset.findClosest(customColor)
        }

        val packageManager = context.packageManager
        val packageName = context.packageName
        val targetComponent = targetPreset.getComponentName(packageName)

        val targetCurrentState = try {
            packageManager.getComponentEnabledSetting(targetComponent)
        } catch (_: Exception) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        }

        val isTargetEnabled = if (targetPreset == AppIconPreset.DYNAMIC) {
            targetCurrentState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                targetCurrentState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        } else {
            targetCurrentState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        if (isTargetEnabled) {
            return
        }

        try {
            packageManager.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        } catch (_: Exception) {
            return
        }

        AppIconPreset.entries.forEach { preset ->
            if (preset != targetPreset) {
                val component = preset.getComponentName(packageName)
                try {
                    val currentState = packageManager.getComponentEnabledSetting(component)
                    if (currentState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                        packageManager.setComponentEnabledSetting(
                            component,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP,
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }
}
