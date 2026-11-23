package com.sipun.playora.feature.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.util.Consumer
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.sipun.playora.core.common.extensions.getInitialDirectoryUri
import com.sipun.playora.core.common.extensions.getMediaContentUri
import com.sipun.playora.core.common.service.registerForSuspendActivityResult
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.feature.player.extensions.OpenDocumentAtInitialUri
import com.sipun.playora.feature.player.extensions.uriToSubtitleConfiguration
import com.sipun.playora.feature.player.model.DecoderMode
import com.sipun.playora.feature.player.service.CustomCommands
import com.sipun.playora.feature.player.service.PlayerService
import com.sipun.playora.feature.player.service.addAudioTrack
import com.sipun.playora.feature.player.service.addSubtitleTrack
import com.sipun.playora.feature.player.service.decoderServiceState
import com.sipun.playora.feature.player.service.retainPlaybackUriPermissions
import com.sipun.playora.feature.player.service.setAudioDecoderMode
import com.sipun.playora.feature.player.service.setVideoDecoderMode
import com.sipun.playora.feature.player.service.startBackgroundPlayback
import com.sipun.playora.feature.player.service.stopPlayerSession
import com.sipun.playora.feature.player.service.tryDecoderFallback
import com.sipun.playora.feature.player.state.rememberMediaController
import com.sipun.playora.feature.player.utils.PlayerApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.concurrent.CopyOnWriteArrayList
import com.sipun.playora.R as coreUiR

internal fun shouldResumeExistingPlayback(
    returningFromBackground: Boolean,
    isRequestedUriCurrent: Boolean,
    hasExplicitPlaylist: Boolean,
    hasNextMediaItem: Boolean,
): Boolean = returningFromBackground ||
        (isRequestedUriCurrent && !hasExplicitPlaylist && hasNextMediaItem)

@SuppressLint("UnsafeOptInUsageError")
class PlayerActivity : ComponentActivity() {

    private val preferencesRepository: PreferencesRepository by inject()

    private val viewModel: PlayerViewModel by viewModel { parametersOf(playerOutput()) }
    val playerPreferences get() = viewModel.state.value.playerPreferences

    private val onWindowAttributesChangedListener =
        CopyOnWriteArrayList<Consumer<WindowManager.LayoutParams?>>()

    private var isPlaybackFinished = false
    private var hasPlaybackStarted = false
    private var playbackFailed = false
    private var lastDuration = C.TIME_UNSET
    private var lastPosition = C.TIME_UNSET
    private var lastMediaUri: Uri? = null
    private var lastDecoder: DecoderMode? = null
    private var playInBackground: Boolean = false
    private var isIntentNew: Boolean = true
    private var backgroundLaunchAccepted = false
    private var activityCreated = false
    private var backgroundControllerFuture: ListenableFuture<MediaController>? = null

    private var mediaController by mutableStateOf<MediaController?>(null)
    private var playerApi by mutableStateOf(PlayerApi(Intent()))
    private var playbackRequestJob: Job? = null
    private var pendingGrantItems: List<MediaItem> = emptyList()
    private val playbackStateListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            intent.data = mediaItem?.localConfiguration?.uri
            isPlaybackFinished = false
            playbackFailed = false
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateKeepScreenOnFlag()
        }

        override fun onPlayerError(error: PlaybackException) {
            playbackFailed = true
            if (playerApi.suppressErrorMessage) {
                val controller = mediaController ?: return
                if (controller.hasNextMediaItem()) {
                    controller.seekToNextMediaItem()
                    controller.prepare()
                } else {
                    finishAndStopPlayerSession()
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                hasPlaybackStarted = true
                if (pendingGrantItems.isNotEmpty()) {
                    runCatching {
                        retainPlaybackUriPermissions(
                            intent,
                            pendingGrantItems,
                        )
                    }
                        .onSuccess { pendingGrantItems = emptyList() }
                        .onFailure {
                            Log.w(
                                "PlayerActivity",
                                "Unable to retain playback URI permissions",
                                it,
                            )
                        }
                }
            }
            if (playbackState == Player.STATE_ENDED) {
                isPlaybackFinished = mediaController?.playbackState == Player.STATE_ENDED
                finishAndStopPlayerSession()
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM &&
                mediaController?.repeatMode == Player.REPEAT_MODE_OFF
            ) {
                isPlaybackFinished = true
                finishAndStopPlayerSession()
            }
        }
    }

    private val audioFileSuspendLauncher =
        registerForSuspendActivityResult(contract = OpenDocumentAtInitialUri())

    private val subtitleFileSuspendLauncher =
        registerForSuspendActivityResult(contract = OpenDocumentAtInitialUri())

    private val cancelPipReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (isInPictureInPictureMode) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Dismiss any existing PiP window
        runCatching {
            sendBroadcast(Intent(ACTION_CANCEL_PIP).setPackage(packageName))
        }
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            cancelPipReceiver,
            android.content.IntentFilter(ACTION_CANCEL_PIP),
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        val request = PlayerApi(intent)
        if (request.launchInBackground) {
            setTheme(coreUiR.style.Playora_Background)
        } else {
            setTheme(coreUiR.style.Playora)
        }
        super.onCreate(savedInstanceState)
        activityCreated = true
        playerApi = request
        if (request.launchInBackground && intent.data == null) {
            playbackFailed = true
            finish()
            return
        }
        isIntentNew = savedInstanceState?.getBoolean(
            "isIntentNew",
            true,
        ) ?: true
        hasPlaybackStarted = savedInstanceState?.getBoolean("hasPlaybackStarted") ?: false
        playbackFailed = savedInstanceState?.getBoolean("playbackFailed") ?: false
        lastDuration = savedInstanceState?.getLong(
            "lastDuration",
            C.TIME_UNSET,
        ) ?: C.TIME_UNSET
        lastPosition = savedInstanceState?.getLong(
            "lastPosition",
            C.TIME_UNSET,
        ) ?: C.TIME_UNSET
        lastMediaUri = savedInstanceState?.getString("lastMediaUri")?.let(Uri::parse)
        lastDecoder = savedInstanceState?.getString("lastDecoder")?.let { name ->
            DecoderMode.entries.find { it.name == name }
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        hideSystemBars()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
        }

        viewModel.output = playerOutput()
        playerApi = PlayerApi(intent)
        if (playerApi.launchInBackground) {
            connectBackgroundController()
            return
        }
        setContent {
            val player = rememberMediaController(
                onBeforeRelease = ::onControllerStopped,
                onExtrasChanged = { extras ->
                    viewModel.onAction(
                        PlayerAction.UpdateDecoderServiceState(extras.decoderServiceState()),
                    )
                },
            )
            LaunchedEffect(key1 = player) {
                if (player == null || !player.isConnected) return@LaunchedEffect
                mediaController = player
                viewModel.onAction(
                    PlayerAction.UpdateDecoderServiceState(
                        player.sessionExtras.decoderServiceState(),
                    ),
                )
                if (!playerApi.launchInBackground) player.addListener(playbackStateListener)
                updateKeepScreenOnFlag()
                startPlayback()
            }

            if (!playerApi.launchInBackground) {
                val preferences by preferencesRepository.applicationPreferences
                    .collectAsStateWithLifecycle()
                PlayoraTheme(
                    preferences = preferences,
                    darkTheme = true,
                    dynamicColor = preferences.useDynamicColors,
                ) {
                    MediaPlayerScreen(
                        viewModel = viewModel,
                        player = player,
                        api = playerApi,
                    )
                }
            }
        }
    }

    private fun connectBackgroundController() {
        val future = MediaController.Builder(
            applicationContext,
            SessionToken(
                applicationContext,
                ComponentName(
                    applicationContext,
                    PlayerService::class.java,
                ),
            ),
        ).buildAsync()
        backgroundControllerFuture = future
        lifecycleScope.launch {
            try {
                mediaController = future.await()
                startPlayback()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                playbackFailed = true
                finish()
            }
        }
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(cancelPipReceiver) }
        backgroundControllerFuture?.let {
            MediaController.releaseFuture(it)
            mediaController = null
        }
        backgroundControllerFuture = null
        super.onDestroy()
    }

    private fun playerOutput() = PlayerViewModel.Output(
        navigateUp = ::finishAndStopPlayerSession,
        selectSubtitle = ::selectSubtitle,
        selectAudio = ::selectAudio,
        playInBackground = {
            playInBackground = true
            finish()
        },
        setVideoDecoderMode = { mode ->
            lifecycleScope.launch { mediaController?.setVideoDecoderMode(mode = mode) }
        },
        setAudioDecoderMode = { mode ->
            lifecycleScope.launch { mediaController?.setAudioDecoderMode(mode = mode) }
        },
        tryDecoderFallback = { lifecycleScope.launch { mediaController?.tryDecoderFallback() } },
    )

    private suspend fun currentMediaDirectory(): Uri? {
        val uri = mediaController?.currentMediaItem?.localConfiguration?.uri ?: return null
        return withContext(Dispatchers.IO) { getInitialDirectoryUri(fileUri = uri) }
    }

    private fun selectSubtitle() {
        lifecycleScope.launch {
            val uri = subtitleFileSuspendLauncher.launch(
                OpenDocumentAtInitialUri.Input(
                    mimeTypes = arrayOf(
                        MimeTypes.APPLICATION_SUBRIP,
                        MimeTypes.APPLICATION_TTML,
                        MimeTypes.TEXT_VTT,
                        MimeTypes.TEXT_SSA,
                        MimeTypes.BASE_TYPE_APPLICATION + "/octet-stream",
                        MimeTypes.BASE_TYPE_TEXT + "/*",
                    ),
                    initialUri = currentMediaDirectory(),
                ),
            ) ?: return@launch
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            snapshotFlow { mediaController }.filterNotNull().first().addSubtitleTrack(uri = uri)
        }
    }

    private fun selectAudio() {
        lifecycleScope.launch {
            val uri = audioFileSuspendLauncher.launch(
                OpenDocumentAtInitialUri.Input(
                    mimeTypes = arrayOf(
                        "audio/*",
                        "application/ogg",
                    ),
                    initialUri = currentMediaDirectory(),
                ),
            ) ?: return@launch
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                val controller = snapshotFlow { mediaController }.filterNotNull().first()
                if (!controller.addAudioTrack(uri = uri)) {
                    Toast.makeText(
                        this@PlayerActivity,
                        coreUiR.string.error_opening_audio,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            } catch (_: SecurityException) {
                Toast.makeText(
                    this@PlayerActivity,
                    coreUiR.string.error_opening_audio,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun onControllerStopped() {
        playbackRequestJob?.cancel()
        mediaController?.run {
            lastDuration = duration
            lastPosition = currentPosition
            lastMediaUri = currentMediaItem?.localConfiguration?.uri
            lastDecoder = sessionExtras.decoderServiceState().videoMode
            viewModel.onAction(PlayerAction.UpdatePlayWhenReady(playWhenReady))
            removeListener(playbackStateListener)
            val shouldPlayInBackground =
                playInBackground || (playerApi.sticky ?: playerPreferences.autoBackgroundPlay)
            if (subtitleFileSuspendLauncher.isAwaitingResult ||
                audioFileSuspendLauncher.isAwaitingResult ||
                !shouldPlayInBackground
            ) {
                pause()
            }
            if (isInPictureInPictureMode) {
                finish()
                if (!shouldPlayInBackground) stopPlayerSession()
            }
        }
        mediaController = null
        updateKeepScreenOnFlag()
    }

    private fun startPlayback() {
        val controller = mediaController ?: return
        if (!isIntentNew &&
            (controller.currentMediaItem == null || controller.playbackState == Player.STATE_ENDED)
        ) {

            isPlaybackFinished = true
            finishAndStopPlayerSession()
            return
        }
        val uri = intent.data ?: return

        val returningFromBackground = !isIntentNew
        val currentUri = controller.currentMediaItem?.localConfiguration?.uri
        val hasExplicitPlaylist =
            playerApi.isApiAccess || intent.hasExtra(PlayerApi.API_PLAYLIST) || false
        isIntentNew = false

        if (shouldResumeExistingPlayback(
                returningFromBackground = returningFromBackground,
                isRequestedUriCurrent = currentUri.toString() == uri.toString(),
                hasExplicitPlaylist = hasExplicitPlaylist,
                hasNextMediaItem = controller.hasNextMediaItem(),
            )
        ) {
            controller.prepare()
            controller.playWhenReady = viewModel.state.value.playWhenReady
            return
        }

        playbackRequestJob?.cancel()
        playbackRequestJob = lifecycleScope.launch {
            playVideo(uri = uri)
        }
    }

    private suspend fun playVideo(uri: Uri) = withContext(Dispatchers.Default) {
        val request = playerApi
        val mediaContentUri = if (request.isApiAccess) null else getMediaContentUri(uri)
        val explicitPlaylist = request.getPlaylist()
        val playlist = explicitPlaylist.takeIf { it.isNotEmpty() }
            ?: (mediaContentUri ?: uri).let { mediaUri ->
                viewModel.getPlaylistFromUri(uri = mediaUri)
                    .map { it.uriString }
                    .toMutableList()
                    .apply {
                        if (!contains(mediaUri.toString())) {
                            add(
                                index = 0,
                                element = mediaUri.toString(),
                            )
                        }
                    }
            }.takeIf { it.isNotEmpty() } ?: listOf(uri.toString())

        val mediaItemIndexToPlay = playlist.indexOfFirst {
            it == (mediaContentUri ?: uri).toString() ||
                    it == uri.toString() ||
                    Uri.decode(it) == Uri.decode(uri.toString()) ||
                    (mediaContentUri != null && Uri.decode(it) == Uri.decode(mediaContentUri.toString()))
        }.takeIf { it >= 0 } ?: 0

        val apiSubs = request.getSubs().map { subtitle ->
            uriToSubtitleConfiguration(
                uri = subtitle.uri,
                subtitleEncoding = playerPreferences.subtitleTextEncoding,
                isSelected = subtitle.isSelected,
                label = subtitle.name,
                filename = subtitle.filename,
            )
        }
        val mediaItems = request.createMediaItems(
            playlist,
            mediaItemIndexToPlay,
            apiSubs,
        )
        ensureActive()

        withContext(Dispatchers.Main) {
            startQueue(
                request,
                mediaItems,
                mediaItemIndexToPlay,
                request.position?.toLong() ?: C.TIME_UNSET,
            )
        }
    }

    private suspend fun startQueue(
        request: PlayerApi,
        items: List<MediaItem>,
        index: Int,
        position: Long,
    ) {
        val controller = mediaController ?: return
        if (!request.launchInBackground) {

            pendingGrantItems = items
            controller.setMediaItems(
                items,
                index,
                position,
            )
            request.applyPlaybackSpeed(controller)
            controller.playWhenReady = viewModel.state.value.playWhenReady
            controller.prepare()
            return
        }

        playInBackground = true
        try {
            val result = controller.startBackgroundPlayback(
                items,
                index,
                position,
                request.playbackSpeed,
            )
            currentCoroutineContext().ensureActive()
            if (request !== playerApi) return
            if (result.resultCode == SessionResult.RESULT_SUCCESS) {
                try {
                    retainPlaybackUriPermissions(
                        intent,
                        items,
                    )
                } catch (error: Exception) {
                    controller.stopPlayerSession()
                    throw error
                }
                backgroundLaunchAccepted = true
                playbackFailed = false
                lastMediaUri = result.extras.getString(
                    CustomCommands.BACKGROUND_MEDIA_URI_KEY,
                )?.let(Uri::parse)
                lastPosition = result.extras.getLong(
                    CustomCommands.BACKGROUND_POSITION_KEY,
                    C.TIME_UNSET,
                )
                lastDuration = result.extras.getLong(
                    CustomCommands.BACKGROUND_DURATION_KEY,
                    C.TIME_UNSET,
                )
            } else {
                playbackFailed = true
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            playbackFailed = true
        }
        finish()
    }

    override fun finish() {
        if (playerApi.shouldReturnResult) {
            val result = playerApi.getResult(
                isPlaybackFinished = isPlaybackFinished,
                duration = if (playerApi.launchInBackground) {
                    lastDuration
                } else {
                    mediaController?.duration
                        ?: lastDuration
                },
                position = if (playerApi.launchInBackground) {
                    lastPosition
                } else {
                    mediaController?.currentPosition
                        ?: lastPosition
                },
                uri = if (playerApi.launchInBackground) {
                    lastMediaUri ?: intent.data
                } else {
                    mediaController?.currentMediaItem?.localConfiguration?.uri ?: lastMediaUri
                    ?: intent.data
                },
                usedDecoder =
                    if (playerApi.launchInBackground) {
                        null
                    } else {
                        mediaController?.sessionExtras?.decoderServiceState()?.videoMode
                            ?: lastDecoder
                    },
            )
            setResult(
                when {
                    playbackFailed -> RESULT_FIRST_USER
                    backgroundLaunchAccepted -> RESULT_OK
                    mediaController?.playerError != null -> RESULT_FIRST_USER
                    hasPlaybackStarted || isPlaybackFinished -> RESULT_OK
                    else -> RESULT_CANCELED
                },
                result,
            )
        }
        super.finish()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        if (playerApi.launchInBackground || isInPictureInPictureMode) return

        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.data != null) {
            if (isInPictureInPictureMode) {
                // Cancel PiP: finish current PiP instance and start new video in fullscreen
                finish()
                val fullScreenIntent = Intent(intent).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(fullScreenIntent)
                return
            }
            val previousBackgroundMode = playerApi.launchInBackground
            playbackRequestJob?.cancel()
            setIntent(intent)
            playerApi = PlayerApi(intent)
            if (playerApi.launchInBackground) mediaController?.removeListener(playbackStateListener)
            isPlaybackFinished = false
            hasPlaybackStarted = false
            playbackFailed = false
            lastDuration = C.TIME_UNSET
            lastPosition = C.TIME_UNSET
            lastMediaUri = null
            lastDecoder = null
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            playInBackground = false
            backgroundLaunchAccepted = false
            isIntentNew = true
            if (activityCreated && previousBackgroundMode != playerApi.launchInBackground) {
                recreate()
            } else {
                startPlayback()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(
            "isIntentNew",
            isIntentNew,
        )
        outState.putBoolean(
            "hasPlaybackStarted",
            hasPlaybackStarted,
        )
        outState.putBoolean(
            "playbackFailed",
            playbackFailed,
        )
        outState.putLong(
            "lastDuration",
            mediaController?.duration ?: lastDuration,
        )
        outState.putLong(
            "lastPosition",
            mediaController?.currentPosition ?: lastPosition,
        )
        outState.putString(
            "lastMediaUri",
            (
                    mediaController?.currentMediaItem?.localConfiguration?.uri
                        ?: lastMediaUri
                    )?.toString(),
        )
        outState.putString(
            "lastDecoder",
            (
                    mediaController?.sessionExtras?.decoderServiceState()?.videoMode
                        ?: lastDecoder
                    )?.name,
        )
        super.onSaveInstanceState(outState)
    }

    private fun updateKeepScreenOnFlag() {
        if (!playerApi.launchInBackground && mediaController?.isPlaying == true) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun finishAndStopPlayerSession() {
        finish()
        mediaController?.stopPlayerSession()
    }

    override fun onWindowAttributesChanged(params: WindowManager.LayoutParams?) {
        super.onWindowAttributesChanged(params)
        for (listener in onWindowAttributesChangedListener) {
            listener.accept(value = params)
        }
    }

    fun addOnWindowAttributesChangedListener(listener: Consumer<WindowManager.LayoutParams?>) {
        onWindowAttributesChangedListener.add(listener)
    }

    fun removeOnWindowAttributesChangedListener(listener: Consumer<WindowManager.LayoutParams?>) {
        onWindowAttributesChangedListener.remove(listener)
    }

    companion object {
        const val ACTION_CANCEL_PIP = "com.sipun.playora.ACTION_CANCEL_PIP"
    }
}
