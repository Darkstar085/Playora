package com.sipun.playora.feature.player.state

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.util.Consumer
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.sipun.playora.feature.player.PlayerActivity
import com.sipun.playora.feature.player.extensions.brightnessPercentage
import com.sipun.playora.feature.player.extensions.currentBrightness
import com.sipun.playora.feature.player.service.getIsLoudnessGainSupported
import com.sipun.playora.feature.player.service.getLoudnessGain
import com.sipun.playora.feature.player.service.setLoudnessGain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun rememberBrightnessState(): BrightnessState {
    val activity = LocalActivity.current
    val brightnessState = remember { BrightnessState(activity = activity as PlayerActivity) }
    DisposableEffect(key1 = activity) {
        brightnessState.handleListeners(disposableEffectScope = this)
    }
    return brightnessState
}

@Stable
class BrightnessState(private val activity: PlayerActivity) {
    val maxBrightness: Float = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
    var currentBrightness: Float by mutableFloatStateOf(activity.currentBrightness)
        private set

    var brightnessPercentage: Int by mutableIntStateOf(activity.brightnessPercentage)
        private set

    private var isInPictureInPictureMode = false
    private var brightnessBeforePictureInPicture =
        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

    fun updateBrightnessPercentage(percentage: Int) {
        setBrightness(
            brightness =
                percentage.coerceIn(
                    minimumValue = 0,
                    maximumValue = 100,
                ) * maxBrightness / 100,
        )
    }

    fun setBrightness(brightness: Float) {
        val playerBrightness = brightness.coerceIn(
            minimumValue = 0f,
            maximumValue = maxBrightness,
        )
        if (isInPictureInPictureMode) {
            brightnessBeforePictureInPicture = playerBrightness
            currentBrightness = playerBrightness
            brightnessPercentage = (playerBrightness / maxBrightness * 100).toInt()
        } else {
            setWindowBrightness(brightness = playerBrightness)
        }
    }

    private fun setWindowBrightness(brightness: Float) {
        val windowAttributes = activity.window.attributes
        windowAttributes.screenBrightness = brightness
        activity.window.attributes = windowAttributes
    }

    private fun updatePictureInPictureMode(isInPictureInPictureMode: Boolean) {
        if (this.isInPictureInPictureMode == isInPictureInPictureMode) return
        if (isInPictureInPictureMode) {
            brightnessBeforePictureInPicture = activity.window.attributes.screenBrightness
        }
        this.isInPictureInPictureMode = isInPictureInPictureMode

        setWindowBrightness(
            brightness = if (isInPictureInPictureMode) {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            } else {
                brightnessBeforePictureInPicture
            },
        )
    }

    fun handleListeners(disposableEffectScope: DisposableEffectScope): DisposableEffectResult =
        with(receiver = disposableEffectScope) {
            val windowAttributesChangedListener: Consumer<WindowManager.LayoutParams?> = Consumer {

                if (!isInPictureInPictureMode) {
                    currentBrightness = activity.currentBrightness
                    brightnessPercentage = activity.brightnessPercentage
                }
            }
            val pictureInPictureModeChangedListener = Consumer<PictureInPictureModeChangedInfo> {
                updatePictureInPictureMode(it.isInPictureInPictureMode)
            }
            activity.addOnWindowAttributesChangedListener(
                listener = windowAttributesChangedListener,
            )
            activity.addOnPictureInPictureModeChangedListener(pictureInPictureModeChangedListener)
            updatePictureInPictureMode(activity.isInPictureInPictureMode)

            onDispose {
                activity.removeOnWindowAttributesChangedListener(
                    listener = windowAttributesChangedListener,
                )
                activity.removeOnPictureInPictureModeChangedListener(
                    pictureInPictureModeChangedListener,
                )
            }
        }
}

@OptIn(UnstableApi::class)
@Composable
fun rememberVolumeState(player: Player?, showVolumePanelIfHeadsetIsOn: Boolean): VolumeState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val volumeState = remember(player) {
        VolumeState(
            player = player,
            context = context,
            showVolumePanelIfHeadsetIsOn = showVolumePanelIfHeadsetIsOn,
            scope = scope,
        )
    }
    LaunchedEffect(key1 = player) { volumeState.initialize() }
    DisposableEffect(key1 = context) { volumeState.handleLifecycle(disposableEffectScope = this) }
    return volumeState
}

@Stable
class VolumeState(
    private val player: Player?,
    private val context: Context,
    private val showVolumePanelIfHeadsetIsOn: Boolean,
    private val scope: CoroutineScope,
) {
    private val audioManager = getSystemService(
        context,
        AudioManager::class.java,
    )!!

    private val systemMaxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    private var isLoudnessGainSupported: Boolean by mutableStateOf(value = false)

    val maxVolume: Int
        get() = if (isLoudnessGainSupported) systemMaxVolume * 2 else systemMaxVolume

    val maxVolumePercentage: Int
        get() = if (isLoudnessGainSupported) {
            MAX_VOLUME_PERCENTAGE_BOOST
        } else {
            MAX_VOLUME_PERCENTAGE_NORMAL
        }

    var currentVolume: Int by mutableIntStateOf(audioManager.currentStreamVolume)
        private set

    var volumePercentage: Int by mutableIntStateOf(value = calculateVolumePercentage())
        private set

    fun syncWithSystem() {
        if (currentVolume > systemMaxVolume) return
        val systemVolume = audioManager.currentStreamVolume
        if (systemVolume != currentVolume) {
            currentVolume = systemVolume
            volumePercentage = calculateVolumePercentage()
        }
    }

    fun updateVolumePercentage(percentage: Int) {
        val maxPercentage = maxVolumePercentage
        val clampedPercentage = percentage.coerceIn(
            minimumValue = 0,
            maximumValue = maxPercentage,
        )
        val targetVolume = (clampedPercentage * systemMaxVolume) / MAX_VOLUME_PERCENTAGE_NORMAL

        setVolume(volume = targetVolume)
    }

    fun increaseVolume(showVolumePanel: Boolean = false) {
        setVolume(
            volume = currentVolume + 1,
            showVolumePanel = showVolumePanel,
        )
    }

    fun decreaseVolume(showVolumePanel: Boolean = false) {
        setVolume(
            volume = currentVolume - 1,
            showVolumePanel = showVolumePanel,
        )
    }

    fun handleLifecycle(disposableEffectScope: DisposableEffectScope): DisposableEffectResult =
        with(receiver = disposableEffectScope) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == VOLUME_CHANGED_ACTION) {
                        if (currentVolume <= systemMaxVolume) {
                            currentVolume = audioManager.currentStreamVolume
                            volumePercentage = calculateVolumePercentage()
                        }
                    }
                }
            }

            context.registerReceiver(
                receiver,
                IntentFilter(VOLUME_CHANGED_ACTION),
            )

            onDispose { context.unregisterReceiver(receiver) }
        }

    suspend fun initialize() {
        val player = player as? MediaController ?: return
        isLoudnessGainSupported = player.getIsLoudnessGainSupported()
        val loudnessGain = player.getLoudnessGain()
        val systemVolume = audioManager.currentStreamVolume

        if (loudnessGain > 0 && isLoudnessGainSupported) {
            if (systemVolume < systemMaxVolume) {
                currentVolume = systemVolume
                volumePercentage = calculateVolumePercentage()
                player.setLoudnessGain(gain = 0)
            } else {
                val boostVolume = (loudnessGain * systemMaxVolume) / MAX_BOOST_GAIN_MB
                currentVolume = systemMaxVolume + boostVolume
                volumePercentage = calculateVolumePercentage()
            }
        }

        player.listen { events ->
            if (events.contains(Player.EVENT_AUDIO_SESSION_ID)) {
                scope.launch { isLoudnessGainSupported = player.getIsLoudnessGainSupported() }
            }
        }
    }

    private fun setVolume(volume: Int, showVolumePanel: Boolean = false) {
        val clampedVolume = volume.coerceIn(
            minimumValue = 0,
            maximumValue = maxVolume,
        )
        currentVolume = clampedVolume
        volumePercentage = calculateVolumePercentage()

        if (clampedVolume <= systemMaxVolume) {
            setSystemVolume(
                volume = clampedVolume,
                showVolumePanel = showVolumePanel,
            )
            applyVolumeBoost(volume = 0)
        } else {
            setSystemVolume(
                volume = systemMaxVolume,
                showVolumePanel = showVolumePanel,
            )
            applyVolumeBoost(volume = clampedVolume - systemMaxVolume)
        }
    }

    private fun setSystemVolume(volume: Int, showVolumePanel: Boolean) {
        val shouldShowUi =
            showVolumePanel || (showVolumePanelIfHeadsetIsOn && audioManager.isHeadsetOn)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume.coerceIn(
                minimumValue = 0,
                maximumValue = systemMaxVolume,
            ),
            if (shouldShowUi) AudioManager.FLAG_SHOW_UI else 0,
        )
    }

    private fun applyVolumeBoost(volume: Int) {
        val player = player as? MediaController ?: return
        val gainMillibels = (volume.toFloat() / systemMaxVolume * MAX_BOOST_GAIN_MB).toInt()

        try {
            player.setLoudnessGain(gain = gainMillibels)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateVolumePercentage(): Int =
        (currentVolume.toFloat() / systemMaxVolume * MAX_VOLUME_PERCENTAGE_NORMAL).toInt()

    private val AudioManager.currentStreamVolume: Int
        get() = getStreamVolume(AudioManager.STREAM_MUSIC)

    private val AudioManager.isHeadsetOn: Boolean
        get() = getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { device ->
            device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                    device.type == AudioDeviceInfo.TYPE_USB_HEADSET
                            )
        }

    companion object {
        private const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
        private const val MAX_VOLUME_PERCENTAGE_NORMAL = 100
        private const val MAX_VOLUME_PERCENTAGE_BOOST = 200
        private const val MAX_BOOST_GAIN_MB = 2000
    }
}

@Composable
fun rememberVolumeAndBrightnessGestureState(
    volumeState: VolumeState,
    brightnessState: BrightnessState,
    enableVolumeGesture: Boolean,
    enableBrightnessGesture: Boolean,
    volumeGestureSensitivity: Float,
    brightnessGestureSensitivity: Float,
): VolumeAndBrightnessGestureState {
    val coroutineScope = rememberCoroutineScope()
    val volumeAndBrightnessGestureState = remember(
        volumeState,
        brightnessState,
    ) {
        VolumeAndBrightnessGestureState(
            volumeState = volumeState,
            brightnessState = brightnessState,
            enableVolumeGesture = enableVolumeGesture,
            enableBrightnessGesture = enableBrightnessGesture,
            volumeGestureSensitivity = volumeGestureSensitivity,
            brightnessGestureSensitivity = brightnessGestureSensitivity,
            coroutineScope = coroutineScope,
        )
    }
    return volumeAndBrightnessGestureState
}

@Stable
class VolumeAndBrightnessGestureState(
    private val volumeState: VolumeState,
    private val brightnessState: BrightnessState,
    private val enableVolumeGesture: Boolean = true,
    private val enableBrightnessGesture: Boolean = true,
    private val volumeGestureSensitivity: Float,
    private val brightnessGestureSensitivity: Float,
    private val coroutineScope: CoroutineScope,
) {
    var activeGesture: VerticalGesture? by mutableStateOf(value = null)
        private set

    var volumeChangePercentage: Int by mutableIntStateOf(value = 0)
        private set

    var brightnessChangePercentage: Int by mutableIntStateOf(value = 0)
        private set

    private var startingY = 0f
    private var startVolumePercentage = 0
    private var startBrightnessPercentage = 0
    private var job: Job? = null

    fun onDragStart(offset: Offset, size: IntSize) {
        val viewCenterX = size.width / 2
        job?.cancel()
        activeGesture = when {
            offset.x < viewCenterX -> VerticalGesture.BRIGHTNESS.takeIf { enableBrightnessGesture }
            else -> VerticalGesture.VOLUME.takeIf { enableVolumeGesture }
        }
        startingY = offset.y
        volumeState.syncWithSystem()
        startVolumePercentage = volumeState.volumePercentage
        startBrightnessPercentage = brightnessState.brightnessPercentage
    }

    fun onDrag(change: PointerInputChange, dragAmount: Float) {
        val activeGesture = activeGesture ?: return
        if (change.isConsumed) return

        when (activeGesture) {
            VerticalGesture.VOLUME -> {
                val maxVolumePercentage = volumeState.maxVolumePercentage
                val volumeChange = (startingY - change.position.y) * (volumeGestureSensitivity / 10)
                val newVolume = startVolumePercentage + volumeChange.toInt()
                volumeChangePercentage = (newVolume - startVolumePercentage).coerceIn(
                    minimumValue = 0 - startVolumePercentage,
                    maximumValue = maxVolumePercentage - startVolumePercentage,
                )
                brightnessChangePercentage = 0
                volumeState.updateVolumePercentage(percentage = newVolume)
            }

            VerticalGesture.BRIGHTNESS -> {
                val brightnessChange =
                    (startingY - change.position.y) * (brightnessGestureSensitivity / 10)
                val newBrightness = startBrightnessPercentage + brightnessChange.toInt()
                brightnessChangePercentage = (newBrightness - startBrightnessPercentage).coerceIn(
                    minimumValue = 0 - startBrightnessPercentage,
                    maximumValue = MAX_BRIGHTNESS_PERCENTAGE - startBrightnessPercentage,
                )
                volumeChangePercentage = 0
                brightnessState.updateBrightnessPercentage(percentage = newBrightness)
            }
        }
    }

    fun onDragEnd() {
        startingY = 0f
        startVolumePercentage = 0
        startBrightnessPercentage = 0

        job?.cancel()
        job = coroutineScope.launch {
            delay(1.seconds)
            activeGesture = null
            volumeChangePercentage = 0
            brightnessChangePercentage = 0
        }
    }

    companion object {
        private const val MAX_BRIGHTNESS_PERCENTAGE = 100
    }
}

enum class VerticalGesture {
    VOLUME,
    BRIGHTNESS,
}
