package com.example.freetv.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val streamUrl: String
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val streamUrl: String,
    val lastWatched: Long
)

@Dao
interface UserDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE streamUrl = :url")
    fun removeFavorite(url: String): Int

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHistory(history: HistoryEntity): Long

    @Query("SELECT * FROM history ORDER BY lastWatched DESC LIMIT 20")
    fun getHistory(): Flow<List<HistoryEntity>>
}
