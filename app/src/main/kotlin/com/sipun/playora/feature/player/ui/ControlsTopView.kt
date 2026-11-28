package com.sipun.playora.feature.player.ui.controls

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.media.audiofx.AudioEffect
import android.os.BatteryManager
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.PixelCopy
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.ScreenRotationAlt
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.R
import com.sipun.playora.core.ui.extensions.copy
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassContainer
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.feature.player.buttons.LoopButton
import com.sipun.playora.feature.player.buttons.PlayerButton
import com.sipun.playora.feature.player.buttons.PlayerButtonBlackAlpha
import com.sipun.playora.feature.player.buttons.ShuffleButton
import com.sipun.playora.feature.player.model.DecoderMode
import com.sipun.playora.feature.player.model.labelRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

@Stable
data class BatteryAndClockStatus(
    val batteryPercentage: Int,
    val isCharging: Boolean,
    val currentTime: String,
)

@Composable
fun rememberBatteryAndClockStatus(uppercaseAmPm: Boolean = true): BatteryAndClockStatus {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var batteryPercentage by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }

    val timeFormatter = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault())
    }

    DisposableEffect(context, lifecycleOwner, uppercaseAmPm) {
        fun updateFromBatteryIntent(intent: Intent?) {
            intent ?: return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL ||
                plugged > 0
            if (level >= 0 && scale > 0) {
                batteryPercentage = (level * 100) / scale
            }
        }

        fun updateClock() {
            val formatted = timeFormatter.format(Date())
            currentTime = if (uppercaseAmPm) {
                formatted.uppercase(Locale.getDefault())
            } else {
                formatted.lowercase(Locale.getDefault())
            }
        }

        updateFromBatteryIntent(
            ContextCompat.registerReceiver(
                context,
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                ContextCompat.RECEIVER_EXPORTED,
            ),
        )
        updateClock()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_BATTERY_CHANGED -> updateFromBatteryIntent(intent)
                    Intent.ACTION_TIME_TICK,
                    Intent.ACTION_TIME_CHANGED,
                    Intent.ACTION_TIMEZONE_CHANGED,
                        -> updateClock()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updateFromBatteryIntent(
                    ContextCompat.registerReceiver(
                        context,
                        null,
                        IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                        ContextCompat.RECEIVER_EXPORTED,
                    ),
                )
                updateClock()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    return BatteryAndClockStatus(
        batteryPercentage = batteryPercentage,
        isCharging = isCharging,
        currentTime = currentTime,
    )
}

@Composable
fun BatteryAndClockView(modifier: Modifier = Modifier) {
    val status = rememberBatteryAndClockStatus(uppercaseAmPm = true)

    Box(
        modifier = modifier.appGlassSurface(
            shape = CircleShape,
            clear = true,
            dimmed = 0.25f,
            forceDark = true,
            fallbackBackground = PlayerButtonBlackAlpha,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val batteryText = if (status.isCharging) {
                "⚡ ${status.batteryPercentage}%"
            } else {
                "${status.batteryPercentage}%"
            }
            Text(
                text = batteryText,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
            Text(
                text = status.currentTime,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun openEqualizer(context: Context) {
    val activity = context.findActivity()
    try {
        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (activity != null) {
            activity.startActivity(intent)
        } else {
            context.startActivity(intent)
        }
    } catch (_: Exception) {
        try {
            val settingsIntent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (activity != null) {
                activity.startActivity(settingsIntent)
            } else {
                context.startActivity(settingsIntent)
            }
        } catch (_: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.equalizer_not_available),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}

suspend fun captureVideoFrame(context: Context, player: Player?): Boolean {
    val uri = try {
        player?.currentMediaItem?.localConfiguration?.uri
    } catch (_: Throwable) {
        null
    }
    val currentPositionMs = try {
        player?.currentPosition ?: 0L
    } catch (_: Throwable) {
        0L
    }

    return withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        if (uri != null) {
            val retriever = MediaMetadataRetriever()
            try {
                try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        retriever.setDataSource(pfd.fileDescriptor)
                    } ?: run {
                        retriever.setDataSource(context, uri)
                    }
                } catch (_: Throwable) {
                    retriever.setDataSource(context, uri)
                }
                val currentPosUs = (currentPositionMs * 1000L).coerceAtLeast(0L)
                bitmap = retriever.getFrameAtTime(
                    currentPosUs,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                ) ?: retriever.getFrameAtTime(currentPosUs)
            } catch (_: Exception) {
            } finally {
                runCatching { retriever.release() }
            }
        }
        if (bitmap == null) {
            bitmap = withContext(Dispatchers.Main) {
                val activity = context.findActivity() ?: return@withContext null
                val window = activity.window ?: return@withContext null
                val bounds = window.decorView.let { Rect(0, 0, it.width, it.height) }
                if (bounds.width() <= 0 || bounds.height() <= 0) return@withContext null
                val bmp = createBitmap(bounds.width(), bounds.height())
                val captured = withTimeoutOrNull(1_000L) {
                    suspendCancellableCoroutine { continuation ->
                        continuation.invokeOnCancellation { bmp.recycle() }
                        try {
                            PixelCopy.request(
                                window,
                                bmp,
                                { copyResult ->
                                    if (continuation.isActive) {
                                        if (copyResult == PixelCopy.SUCCESS) {
                                            continuation.resume(bmp)
                                        } else {
                                            bmp.recycle()
                                            continuation.resume(null)
                                        }
                                    } else {
                                        bmp.recycle()
                                    }
                                },
                                Handler(Looper.getMainLooper()),
                            )
                        } catch (_: Exception) {
                            bmp.recycle()
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                }
                if (captured == null && !bmp.isRecycled) {
                    bmp.recycle()
                }
                captured
            }
        }

        val capturedBitmap = bitmap ?: return@withContext false
        try {
            saveBitmapToGallery(context, capturedBitmap)
        } finally {
            if (!capturedBitmap.isRecycled) {
                capturedBitmap.recycle()
            }
        }
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val filename = "Playora_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/Playora",
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues,
        )
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
            true
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ControlsTopView(
    modifier: Modifier = Modifier,
    title: String,
    videoDecoderMode: DecoderMode?,
    activeVideoDecoderMode: DecoderMode? = null,
    player: Player? = null,
    onBackClick: () -> Unit = {},
    onDecoderClick: () -> Unit = {},
    onAudioClick: () -> Unit = {},
    onSubtitleClick: () -> Unit = {},
    onPlaybackSpeedClick: () -> Unit = {},
    onPlayInBackgroundClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firstControlFocusRequester = remember { FocusRequester() }
    val systemBarsPadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues()
    val decoderDescription = stringResource(R.string.select_decoders)
    val capturingText = stringResource(R.string.capturing_screenshot)
    val savedText = stringResource(R.string.screenshot_saved)
    val failedText = stringResource(R.string.screenshot_failed)
    val extraTopPadding = if (systemBarsPadding.calculateTopPadding() == 0.dp) 16.dp else 0.dp
    val useGlass = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = if (useGlass) 0.55f else 0.75f),
                        Color.Black.copy(alpha = if (useGlass) 0.28f else 0.45f),
                        Color.Transparent,
                    ),
                ),
            )
            .padding(systemBarsPadding.copy(bottom = 0.dp))
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .padding(top = extraTopPadding)
            .focusProperties { onEnter = { firstControlFocusRequester.requestFocus() } }
            .focusGroup(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onBackClick,
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_up),
                )
            }
            val validTitle = title.takeIf { it.isNotBlank() }
            if (validTitle != null) {
                Text(
                    text = validTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.appGlassContainer(enabled = useGlass),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BatteryAndClockView()

                PlayerButton(
                    modifier = Modifier.size(40.dp),
                    onClick = onAudioClick,
                    containerColor = PlayerButtonBlackAlpha,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Audiotrack,
                        contentDescription = stringResource(R.string.select_audio_track),
                    )
                }
                PlayerButton(
                    modifier = Modifier.size(40.dp),
                    onClick = onSubtitleClick,
                    containerColor = PlayerButtonBlackAlpha,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Subtitles,
                        contentDescription = stringResource(R.string.select_subtitle_track_desc),
                    )
                }
                val selectedMode = videoDecoderMode ?: DecoderMode.AUTO
                val decoderText = if (selectedMode == DecoderMode.AUTO && activeVideoDecoderMode != null) {
                    stringResource(
                        R.string.decoder_mode_auto_active,
                        stringResource(activeVideoDecoderMode.labelRes),
                    )
                } else {
                    stringResource(selectedMode.labelRes)
                }
                PlayerButton(
                    modifier = Modifier
                        .semantics { contentDescription = decoderDescription }
                        .focusRequester(focusRequester = firstControlFocusRequester),
                    onClick = onDecoderClick,
                    containerColor = PlayerButtonBlackAlpha,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = decoderText,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                    )
                }
            }
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState())
                .appGlassContainer(enabled = useGlass),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 12.dp else 8.dp),
        ) {
            val activeAccent = MaterialTheme.colorScheme.primary

            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = { openEqualizer(context) },
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.equalizer),
                    modifier = Modifier.size(20.dp),
                )
            }

            val speed = player?.playbackParameters?.speed ?: 1.0f
            val isSpeedActive = speed != 1.0f
            val speedText =
                if (speed == speed.toInt().toFloat()) "${speed.toInt()}X" else "${speed}X"
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onPlaybackSpeedClick,
                containerColor = if (isSpeedActive) activeAccent.copy(alpha = 0.35f) else PlayerButtonBlackAlpha,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = speedText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (isSpeedActive) activeAccent else Color.White,
                )
            }

            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = {
                    coroutineScope.launch {
                        Toast.makeText(context, capturingText, Toast.LENGTH_SHORT).show()
                        val success = captureVideoFrame(context, player)
                        Toast.makeText(
                            context,
                            if (success) savedText else failedText,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = stringResource(R.string.capture_screenshot),
                    modifier = Modifier.size(20.dp),
                )
            }

            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onPlayInBackgroundClick,
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Headset,
                    contentDescription = stringResource(R.string.play_in_background),
                    modifier = Modifier.size(20.dp),
                )
            }

            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = {
                    val activity = context.findActivity()
                    activity?.let { act ->
                        val isLandscapeMode =
                            act.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                        act.requestedOrientation = if (isLandscapeMode) {
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }
                    }
                },
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ScreenRotationAlt,
                    contentDescription = stringResource(R.string.rotate_screen),
                    modifier = Modifier.size(20.dp),
                )
            }

            ShuffleButton(
                modifier = Modifier.size(40.dp),
                player = player,
            )
            LoopButton(
                modifier = Modifier.size(40.dp),
                player = player,
            )
        }
    }
}
