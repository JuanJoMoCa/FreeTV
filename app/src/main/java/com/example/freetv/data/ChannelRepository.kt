package com.example.freetv.data

class ChannelRepository() {
    
    private val api get() = ApiClient.apiService

    suspend fun getChannels(): Result<List<Channel>> {
        return try {
            val response = api.getChannels()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchChannels(query: String): Result<List<Channel>> {
        return try {
            val response = api.searchChannels(query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
