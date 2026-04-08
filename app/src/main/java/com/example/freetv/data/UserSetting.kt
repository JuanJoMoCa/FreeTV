package com.example.freetv.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_settings")
data class UserSetting(
    @PrimaryKey
    val key: String,
    val value: String
)

@Dao
interface SettingDao {
    @Query("SELECT * FROM user_settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<UserSetting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSetting(setting: UserSetting): Long
    
    @Query("SELECT * FROM user_settings")
    fun getAllSettings(): Flow<List<UserSetting>>
}
