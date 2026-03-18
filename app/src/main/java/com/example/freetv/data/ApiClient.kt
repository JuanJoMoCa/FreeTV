package com.example.freetv.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var currentIp: String = "192.168.0.103" // Default home IP
    private var isUsbMode: Boolean = false

    fun setIp(newIp: String) {
        currentIp = newIp
        isUsbMode = false
        rebuildService()
    }

    fun setUsbMode(enabled: Boolean) {
        isUsbMode = enabled
        rebuildService()
    }

    fun getBaseUrl(): String {
        return if (isUsbMode) "http://localhost:8080/" else "http://$currentIp:8080/"
    }

    fun isUsbModeActive(): Boolean = isUsbMode

    private var _apiService: TvApi? = null
    val apiService: TvApi
        get() {
            if (_apiService == null) {
                rebuildService()
            }
            return _apiService!!
        }

    private fun rebuildService() {
        _apiService = Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TvApi::class.java)
    }
}
