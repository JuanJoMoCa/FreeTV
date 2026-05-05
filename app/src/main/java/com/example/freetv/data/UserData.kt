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

@Entity(tableName = "custom_lists")
data class CustomListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "custom_list_channels",
    primaryKeys = ["listId", "streamUrl"]
)
data class CustomListChannel(
    val listId: Long,
    val streamUrl: String
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


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomList(customList: CustomListEntity): Long


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannelsToCustomList(channels: List<CustomListChannel>): List<Long>

    @Query("SELECT * FROM custom_lists ORDER BY name ASC")
    fun getAllCustomLists(): Flow<List<CustomListEntity>>

    @Query("SELECT streamUrl FROM custom_list_channels WHERE listId = :listId")
    fun getChannelsForList(listId: Long): Flow<List<String>>

    @Query("DELETE FROM history")
    fun clearHistory()

}