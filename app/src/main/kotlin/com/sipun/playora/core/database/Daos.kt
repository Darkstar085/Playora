package com.sipun.playora.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.sipun.playora.core.database.entities.HiddenVideoEntity
import com.sipun.playora.core.database.entities.MediumStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenVideoDao {
    @Insert
    suspend fun insert(hiddenVideo: HiddenVideoEntity): Long

    @Query("SELECT * FROM hidden_video ORDER BY hidden_at DESC")
    fun getAll(): Flow<List<HiddenVideoEntity>>

    @Query("SELECT * FROM hidden_video WHERE id = :id")
    suspend fun getById(id: Long): HiddenVideoEntity?

    @Query("DELETE FROM hidden_video WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM hidden_video WHERE privacy_path IN (:privacyPaths)")
    suspend fun deleteByPrivacyPaths(privacyPaths: List<String>)
}

@Dao
interface MediumStateDao {
    @Upsert
    suspend fun upsert(mediumState: MediumStateEntity)

    @Upsert
    suspend fun upsertAll(mediaStates: List<MediumStateEntity>)

    @Transaction
    suspend fun update(uri: String, transform: (MediumStateEntity) -> MediumStateEntity) {
        upsert(mediumState = transform(get(uri = uri) ?: MediumStateEntity(uriString = uri)))
    }

    @Query("SELECT * FROM media_state WHERE uri = :uri")
    suspend fun get(uri: String): MediumStateEntity?

    @Query("SELECT * FROM media_state WHERE uri = :uri")
    fun getAsFlow(uri: String): Flow<MediumStateEntity?>

    @Query("SELECT * FROM media_state")
    fun getAll(): Flow<List<MediumStateEntity>>

    @Query("UPDATE media_state SET last_played_time = NULL")
    suspend fun clearPlaybackHistory()

    @Query("UPDATE media_state SET last_played_time = NULL WHERE uri IN (:uris)")
    suspend fun clearLastPlayedTimes(uris: List<String>)

    @Query("DELETE FROM media_state WHERE uri in (:uris)")
    suspend fun delete(uris: List<String>)
}
