package com.example.freetv.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY nombre ASC")
    fun getAllChannels(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1")
    fun getFavoriteChannels(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE lastWatched > 0 ORDER BY lastWatched DESC LIMIT 10")
    fun getRecentChannels(): Flow<List<Channel>>

    @Query("SELECT DISTINCT categoria FROM channels ORDER BY categoria ASC")
    fun getCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChannels(channels: List<Channel>): List<Long>

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :id")
    fun updateFavorite(id: Long, isFavorite: Boolean): Int

    @Query("UPDATE channels SET lastWatched = :timestamp WHERE id = :id")
    fun updateLastWatched(id: Long, timestamp: Long): Int

    @Query("DELETE FROM channels")
    fun clearAllChannels(): Int

    @Query("SELECT COUNT(*) FROM channels")
    fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannel(channel: Channel): Long
}
