package com.example.freetv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChannelRepository(private val channelDao: ChannelDao) {

    private val m3uUrl = "https://iptv-org.github.io/iptv/countries/mx.m3u"
    private val m3uParser = M3uParser()

    fun getAllChannels(): Flow<List<Channel>> = channelDao.getAllChannels()
    fun getFavoriteChannels(): Flow<List<Channel>> = channelDao.getFavoriteChannels()
    fun getRecentChannels(): Flow<List<Channel>> = channelDao.getRecentChannels()
    fun getCategories(): Flow<List<String>> = channelDao.getCategories()

    suspend fun syncChannels() = withContext(Dispatchers.IO) {
        val currentChannels = channelDao.getAllChannelsSync()

        val pinnedUrls = currentChannels.filter { it.isPinned }.map { it.streamUrl }.toSet()
        val favoriteUrls = currentChannels.filter { it.isFavorite }.map { it.streamUrl }.toSet()

        val newChannels = m3uParser.parseFromUrl(m3uUrl)

        if (newChannels.isNotEmpty()) {
            val updatedChannels = newChannels.map { channel ->
                channel.copy(
                    isPinned = channel.streamUrl in pinnedUrls,
                    isFavorite = channel.streamUrl in favoriteUrls
                )
            }

            channelDao.clearAllChannels()
            channelDao.insertChannels(updatedChannels)
        }
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        channelDao.updateFavorite(id, isFavorite)
    }

    suspend fun updatePinned(id: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        channelDao.updatePinned(id, isPinned)
    }

    suspend fun getPinnedCount(): Int = withContext(Dispatchers.IO) {
        channelDao.getPinnedCount()
    }

    suspend fun updateLastWatched(id: Long) = withContext(Dispatchers.IO) {
        channelDao.updateLastWatched(id, System.currentTimeMillis())
    }

    suspend fun getChannelCount(): Int = withContext(Dispatchers.IO) {
        channelDao.getCount()
    }

    suspend fun addCustomChannel(channel: Channel) = withContext(Dispatchers.IO) {
        channelDao.insertChannel(channel)
    }
}