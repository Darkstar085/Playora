package com.sipun.playora.core.data.repository

import com.sipun.playora.core.common.di.DiQualifiers
import com.sipun.playora.core.datastore.datasource.AppPreferencesDataSource
import com.sipun.playora.core.datastore.datasource.PlayerPreferencesDataSource
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.PlayerPreferences
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface PreferencesRepository {

    val applicationPreferencesFlow: Flow<ApplicationPreferences>

    val applicationPreferences: StateFlow<ApplicationPreferences>

    val playerPreferences: StateFlow<PlayerPreferences>

    suspend fun updateApplicationPreferences(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    )

    suspend fun updatePlayerPreferences(transform: suspend (PlayerPreferences) -> PlayerPreferences)

    suspend fun resetPreferences()
}

@Single
class LocalPreferencesRepository(
    private val appPreferencesDataSource: AppPreferencesDataSource,
    private val playerPreferencesDataSource: PlayerPreferencesDataSource,
    @Named(DiQualifiers.APPLICATION_SCOPE) private val applicationScope: CoroutineScope,
) : PreferencesRepository {

    override val applicationPreferencesFlow: Flow<ApplicationPreferences> =
        appPreferencesDataSource.preferences.catch { exception ->
            if (exception is IOException) emit(ApplicationPreferences()) else throw exception
        }

    override val applicationPreferences: StateFlow<ApplicationPreferences> =
        applicationPreferencesFlow.stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = ApplicationPreferences(),
        )

    override val playerPreferences: StateFlow<PlayerPreferences> =
        playerPreferencesDataSource.preferences.stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = PlayerPreferences(),
        )

    override suspend fun updateApplicationPreferences(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    ) {
        appPreferencesDataSource.update(transform = transform)
    }

    override suspend fun updatePlayerPreferences(
        transform: suspend (PlayerPreferences) -> PlayerPreferences,
    ) {
        playerPreferencesDataSource.update(transform = transform)
    }

    override suspend fun resetPreferences() {
        appPreferencesDataSource.update { current ->
            ApplicationPreferences(
                hasCompletedOnboarding = current.hasCompletedOnboarding,
                hasSeenVaultHint = current.hasSeenVaultHint,
            )
        }
        playerPreferencesDataSource.update { PlayerPreferences() }
    }
}
