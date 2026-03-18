package com.example.freetv.data

import retrofit2.http.GET
import retrofit2.http.Query

interface TvApi {
    @GET("api/canales")
    suspend fun getChannels(): List<Channel>

    @GET("api/canales/buscar")
    suspend fun searchChannels(@Query("nombre") query: String): List<Channel>
}
