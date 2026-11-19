package com.sipun.playora.feature.player.ui.controls

import android.app.Activity
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
import android.os.Build
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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.R
import com.sipun.playora.core.ui.extensions.copy
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.feature.player.buttons.LoopButton
import com.sipun.playora.feature.player.buttons.PlayerButton
import com.sipun.playora.feature.player.buttons.PlayerButtonBlackAlpha
import com.sipun.playora.feature.player.buttons.ShuffleButton
import com.sipun.playora.feature.player.model.labelRes
import com.sipun.nextlib.media3ext.ffdecoder.DecoderMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Composable
fun BatteryAndClockView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var batteryPercentage by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }

    val timeFormatter = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault())
    }

    LaunchedEffect(Unit) {
        while (true) {
            val batteryIntent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            )
            batteryIntent?.let { intent ->
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
            currentTime = timeFormatter.format(Date()).uppercase(Locale.getDefault())
            delay(10_000)
        }
    }

    Surface(
        color = PlayerButtonBlackAlpha,
        shape = CircleShape,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val batteryText = if (isCharging) "⚡ $batteryPercentage%" else "$batteryPercentage%"
            Text(
                text = batteryText,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = currentTime,
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

fun openEqualizer(context: Context, player: Player?) {
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
            Toast.makeText(context, "Equalizer not available", Toast.LENGTH_SHORT).show()
        }
    }
}

suspend fun captureVideoFrame(context: Context, player: Player?): Boolean {
    // Read player parameters on main thread before coroutine switch
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
            try {
                val retriever = MediaMetadataRetriever()
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
                bitmap = retriever.getFrameAtTime(currentPosUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(currentPosUs)
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (bitmap == null) {
            withContext(Dispatchers.Main) {
                val activity = context.findActivity() ?: return@withContext
                val window = activity.window ?: return@withContext
                val bounds = window.decorView.let { Rect(0, 0, it.width, it.height) }
                if (bounds.width() > 0 && bounds.height() > 0) {
                    val bmp = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
                    val latch = CountDownLatch(1)
                    var success = false
                    PixelCopy.request(window, bmp, { copyResult ->
                        success = (copyResult == PixelCopy.SUCCESS)
                        latch.countDown()
                    }, Handler(Looper.getMainLooper()))
                    latch.await(1, TimeUnit.SECONDS)
                    if (success) bitmap = bmp
                }
            }
        }

        if (bitmap != null) {
            saveBitmapToGallery(context, bitmap!!)
        } else {
            false
        }
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val filename = "Playora_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Playora")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ControlsTopView(
    modifier: Modifier = Modifier,
    title: String,
    videoDecoderMode: DecoderMode?,
    player: Player? = null,
    onBackClick: () -> Unit = {},
    onDecoderClick: () -> Unit = {},
    onAudioClick: () -> Unit = {},
    onSubtitleClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
    onChaptersClick: () -> Unit = {},
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
    val extraTopPadding = if (systemBarsPadding.calculateTopPadding() == 0.dp) 16.dp else 0.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.75f),
                        Color.Black.copy(alpha = 0.45f),
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
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onBackClick,
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                )
            }
            val validTitle = title.takeIf { it.isNotBlank() }
            if (validTitle != null) {
                Text(
                    text = validTitle,
                    style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            BatteryAndClockView()

            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onAudioClick,
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Audiotrack,
                    contentDescription = null,
                )
            }
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onSubtitleClick,
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Subtitles,
                    contentDescription = null,
                )
            }
            PlayerButton(
                modifier = Modifier
                    .semantics { contentDescription = decoderDescription }
                    .focusRequester(focusRequester = firstControlFocusRequester),
                onClick = onDecoderClick,
                containerColor = PlayerButtonBlackAlpha,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = stringResource((videoDecoderMode ?: DecoderMode.HARDWARE).labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
        }

        // Secondary Quick Actions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val activeAccent = MaterialTheme.colorScheme.primary

            // Equalizer
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = { openEqualizer(context, player) },
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Playback speed
            val speed = player?.playbackParameters?.speed ?: 1.0f
            val isSpeedActive = speed != 1.0f
            val speedText = if (speed == speed.toInt().toFloat()) "${speed.toInt()}X" else "${speed}X"
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onPlaybackSpeedClick,
                containerColor = if (isSpeedActive) activeAccent.copy(alpha = 0.35f) else PlayerButtonBlackAlpha,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = speedText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    ),
                    color = if (isSpeedActive) activeAccent else Color.White,
                )
            }

            // Screenshot / Frame capture
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = {
                    coroutineScope.launch {
                        Toast.makeText(context, "Capturing screenshot...", Toast.LENGTH_SHORT).show()
                        val success = captureVideoFrame(context, player)
                        if (success) {
                            Toast.makeText(context, "Screenshot saved to Pictures/Playora", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Background play
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onPlayInBackgroundClick,
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Headset,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Screen Rotation toggle
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = {
                    val activity = context.findActivity()
                    activity?.let { act ->
                        val isLandscape = act.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                        act.requestedOrientation = if (isLandscape) {
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
                    contentDescription = null,
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

@Preview
@Composable
private fun ControlsTopViewPreview() {
    PlayoraTheme(darkTheme = true) {
        Surface {
            ControlsTopView(
                title = "Title",
                videoDecoderMode = DecoderMode.HARDWARE,
                onBackClick = {},
            )
        }
    }
}
