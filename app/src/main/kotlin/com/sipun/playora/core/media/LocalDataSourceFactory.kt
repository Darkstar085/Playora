package com.sipun.playora.core.media.datasource

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import org.koin.core.annotation.Single

@UnstableApi
@Single
class LocalDataSourceFactory(context: Context) : DataSource.Factory {
    private val delegate = DefaultDataSource.Factory(
        context,
        FileDataSource.Factory(),
    )

    override fun createDataSource(): DataSource = delegate.createDataSource()
}
