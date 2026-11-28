package com.sipun.playora.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.sipun.playora.core.datastore.serializer.ApplicationPreferencesSerializer
import com.sipun.playora.core.datastore.serializer.PlayerPreferencesSerializer
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.PlayerPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

object PlayoraDataStores {
    private const val APP_PREFERENCES_DATASTORE_FILE = "app_preferences.json"
    private const val PLAYER_PREFERENCES_DATASTORE_FILE = "player_preferences.json"

    fun createAppPreferencesDataStore(
        context: Context,
        ioDispatcher: CoroutineDispatcher,
        scope: CoroutineScope,
    ): DataStore<ApplicationPreferences> = DataStoreFactory.create(
        serializer = ApplicationPreferencesSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { ApplicationPreferences() },
        scope = CoroutineScope(context = scope.coroutineContext + ioDispatcher),
        produceFile = { context.dataStoreFile(fileName = APP_PREFERENCES_DATASTORE_FILE) },
    )

    fun createPlayerPreferencesDataStore(
        context: Context,
        ioDispatcher: CoroutineDispatcher,
        scope: CoroutineScope,
    ): DataStore<PlayerPreferences> = DataStoreFactory.create(
        serializer = PlayerPreferencesSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { PlayerPreferences() },
        scope = CoroutineScope(context = scope.coroutineContext + ioDispatcher),
        produceFile = { context.dataStoreFile(fileName = PLAYER_PREFERENCES_DATASTORE_FILE) },
    )
}
