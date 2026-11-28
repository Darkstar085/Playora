package com.sipun.playora.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sipun.playora.core.database.dao.HiddenVideoDao
import com.sipun.playora.core.database.dao.MediumStateDao
import com.sipun.playora.core.database.entities.HiddenVideoEntity
import com.sipun.playora.core.database.entities.MediumStateEntity

@Database(
    entities = [
        MediumStateEntity::class,
        HiddenVideoEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun mediumStateDao(): MediumStateDao

    abstract fun hiddenVideoDao(): HiddenVideoDao

    companion object {
        const val DATABASE_NAME = "media_db"

        fun create(context: Context): MediaDatabase = Room.databaseBuilder(
            context = context,
            klass = MediaDatabase::class.java,
            name = DATABASE_NAME,
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}
