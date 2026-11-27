package com.sipun.playora

import android.content.Context
import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.media3.common.util.UnstableApi
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.sipun.playora.core.common.di.DiQualifiers
import com.sipun.playora.core.common.service.system.LocalSystemService
import com.sipun.playora.core.common.service.system.SystemService
import com.sipun.playora.core.data.repository.LocalMediaRepository
import com.sipun.playora.core.data.repository.LocalPreferencesRepository
import com.sipun.playora.core.data.repository.LocalPrivacyPinRepository
import com.sipun.playora.core.data.repository.LocalPrivacyRepository
import com.sipun.playora.core.data.repository.MediaRepository
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.data.repository.PrivacyPinRepository
import com.sipun.playora.core.data.repository.PrivacyRepository
import com.sipun.playora.core.database.MediaDatabase
import com.sipun.playora.core.datastore.PlayoraDataStores
import com.sipun.playora.core.datastore.datasource.AppPreferencesDataSource
import com.sipun.playora.core.datastore.datasource.PlayerPreferencesDataSource
import com.sipun.playora.core.domain.GetFolderTreeMediaUseCase
import com.sipun.playora.core.domain.GetHiddenVideosUseCase
import com.sipun.playora.core.domain.GetPopularFoldersUseCase
import com.sipun.playora.core.domain.GetRecentlyPlayedVideoUseCase
import com.sipun.playora.core.domain.GetSortedFoldersUseCase
import com.sipun.playora.core.domain.GetSortedMediaUseCase
import com.sipun.playora.core.domain.GetSortedVideosUseCase
import com.sipun.playora.core.domain.SearchMediaUseCase
import com.sipun.playora.core.media.datasource.LocalDataSourceFactory
import com.sipun.playora.core.media.services.LocalMediaOperationsService
import com.sipun.playora.core.media.services.MediaOperationsService
import com.sipun.playora.core.media.services.MediaService
import com.sipun.playora.core.media.services.MediaStoreMediaService
import com.sipun.playora.core.media.sync.LocalMediaSynchronizer
import com.sipun.playora.core.media.sync.MediaSynchronizer
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.PlayerPreferences
import com.sipun.playora.feature.player.PlayerViewModel
import com.sipun.playora.feature.videopicker.privacy.PrivacyViewModel
import com.sipun.playora.feature.videopicker.screens.mediapicker.MediaPickerViewModel
import com.sipun.playora.feature.videopicker.screens.search.SearchViewModel
import com.sipun.playora.settings.screens.appearance.AppearancePreferencesViewModel
import com.sipun.playora.settings.screens.general.GeneralPreferencesViewModel
import com.sipun.playora.settings.screens.gesture.GesturePreferencesViewModel
import com.sipun.playora.settings.screens.medialibrary.FolderPreferencesViewModel
import com.sipun.playora.settings.screens.player.PlayerPreferencesViewModel
import com.sipun.playora.settings.screens.subtitle.SubtitlePreferencesViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

@OptIn(UnstableApi::class)
val appModule = module {

    single<CoroutineDispatcher>(named(DiQualifiers.IO_DISPATCHER)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(DiQualifiers.DEFAULT_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher> { get(named(DiQualifiers.DEFAULT_DISPATCHER)) }
    single<CoroutineScope>(named(DiQualifiers.APPLICATION_SCOPE)) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(named(DiQualifiers.DEFAULT_DISPATCHER)))
    }
    single<CoroutineScope> { get(named(DiQualifiers.APPLICATION_SCOPE)) }

    single { MediaDatabase.create(get()) }
    single { get<MediaDatabase>().mediumStateDao() }
    single { get<MediaDatabase>().hiddenVideoDao() }

    single<DataStore<ApplicationPreferences>>(named(DiQualifiers.APP_PREFERENCES)) {
        PlayoraDataStores.createAppPreferencesDataStore(
            context = get(),
            ioDispatcher = get(named(DiQualifiers.IO_DISPATCHER)),
            scope = get(named(DiQualifiers.APPLICATION_SCOPE)),
        )
    }
    single<DataStore<PlayerPreferences>>(named(DiQualifiers.PLAYER_PREFERENCES)) {
        PlayoraDataStores.createPlayerPreferencesDataStore(
            context = get(),
            ioDispatcher = get(named(DiQualifiers.IO_DISPATCHER)),
            scope = get(named(DiQualifiers.APPLICATION_SCOPE)),
        )
    }
    single { AppPreferencesDataSource(get(named(DiQualifiers.APP_PREFERENCES))) }
    single { PlayerPreferencesDataSource(get(named(DiQualifiers.PLAYER_PREFERENCES))) }

    singleOf(::LocalPreferencesRepository) bind PreferencesRepository::class
    singleOf(::LocalMediaRepository) bind MediaRepository::class
    singleOf(::LocalPrivacyRepository) bind PrivacyRepository::class
    singleOf(::LocalPrivacyPinRepository) bind PrivacyPinRepository::class
    singleOf(::MediaStoreMediaService) bind MediaService::class
    singleOf(::LocalMediaOperationsService) bind MediaOperationsService::class
    singleOf(::LocalMediaSynchronizer) bind MediaSynchronizer::class
    singleOf(::LocalSystemService) bind SystemService::class
    single { provideLocalDataSourceFactory(get()) }

    singleOf(::GetSortedMediaUseCase)
    singleOf(::GetFolderTreeMediaUseCase)
    singleOf(::GetSortedVideosUseCase)
    singleOf(::GetSortedFoldersUseCase)
    singleOf(::GetRecentlyPlayedVideoUseCase)
    singleOf(::SearchMediaUseCase)
    singleOf(::GetPopularFoldersUseCase)
    singleOf(::GetHiddenVideosUseCase)

    viewModelOf(::MainViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::MediaPickerViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::PrivacyViewModel)
    viewModelOf(::AppearancePreferencesViewModel)
    viewModelOf(::GeneralPreferencesViewModel)
    viewModelOf(::GesturePreferencesViewModel)
    viewModelOf(::FolderPreferencesViewModel)
    viewModelOf(::PlayerPreferencesViewModel)
    viewModelOf(::SubtitlePreferencesViewModel)

    single {
        ImageLoader.Builder(context = get())
            .components {
                add(
                    VideoThumbnailDecoder.Factory(
                        thumbnailStrategy = {
                            ThumbnailStrategy.Hybrid()
                        },
                    ),
                )
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache(
                DiskCache.Builder()
                    .directory(get<Context>().filesDir.resolve("thumbnails").toOkioPath())
                    .maxSizePercent(1.0)
                    .build(),
            )
            .crossfade(true)
            .build()
    }
}

@OptIn(UnstableApi::class)
private fun provideLocalDataSourceFactory(context: Context): LocalDataSourceFactory =
    LocalDataSourceFactory(context)
