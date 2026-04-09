package com.example.freetv.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.freetv.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedTvViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ChannelRepository(database.channelDao())
    private val settingDao = database.settingDao()
    private val userDataDao = database.userDataDao()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("Todas")
    
    // Atomic status flows
    val favoriteUrls: StateFlow<Set<String>> = userDataDao.getAllFavorites()
        .map { list -> list.map { it.streamUrl }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val historyMap: StateFlow<Map<String, Long>> = userDataDao.getHistory()
        .map { list -> list.associate { it.streamUrl to it.lastWatched } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val channels: StateFlow<List<Channel>> = combine(
        repository.getAllChannels(), 
        _searchQuery,
        _selectedCategory,
        favoriteUrls
    ) { list, query, category, favUrls ->
        list.map { it.copy(isFavorite = it.streamUrl in favUrls) }
            .filter { channel ->
                val matchesQuery = query.isBlank() || channel.nombre.contains(query, ignoreCase = true)
                val matchesCategory = category == "Todas" || channel.categoria == category
                matchesQuery && matchesCategory
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val settings: StateFlow<Map<String, String>> = settingDao.getAllSettings()
        .map { list -> list.associate { it.key to it.value } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Favorites and Recents are now derived directly from combined state
    val favorites: StateFlow<List<Channel>> = combine(repository.getAllChannels(), favoriteUrls) { list, favUrls ->
        list.filter { it.streamUrl in favUrls }.map { it.copy(isFavorite = true) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recents: StateFlow<List<Channel>> = combine(repository.getAllChannels(), historyMap) { list, hMap ->
        list.filter { it.streamUrl in hMap.keys }
            .sortedByDescending { hMap[it.streamUrl] ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentChannelIndex = MutableStateFlow<Int>(-1)
    val currentChannelIndex: StateFlow<Int> = _currentChannelIndex.asStateFlow()

    init {
        observeDatabase()
        syncWithRemote()
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.getCategories().collect { list ->
                _categories.value = listOf("Todas") + list
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                settingDao.insertSetting(UserSetting(key, value))
            }
        }
    }

    fun searchChannels(query: String) {
        _searchQuery.value = query
    }

    fun loadChannels() {
        _searchQuery.value = ""
        _selectedCategory.value = "Todas"
        // Local database is reactive via Flow, no need to manually re-load
    }

    fun syncWithRemote() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.syncChannels()
            } catch (e: Exception) {
                // Verificamos de forma segura si hay datos en la BD local (CACHE)
                val count = repository.getChannelCount()
                if (count > 0) {
                    _error.value = "Sincronización fallida. Cargando canales guardados."
                } else {
                    _error.value = "Error de conexión: No se pudo obtener el catálogo."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val isCurrentlyFavorite = favoriteUrls.value.contains(channel.streamUrl)
                if (isCurrentlyFavorite) {
                    userDataDao.removeFavorite(channel.streamUrl)
                } else {
                    userDataDao.addFavorite(FavoriteEntity(channel.streamUrl))
                }
            }
        }
    }

    fun onChannelPlayed(channel: Channel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userDataDao.addHistory(HistoryEntity(channel.streamUrl, System.currentTimeMillis()))
            }
        }
    }

    fun selectChannelByUrl(url: String) {
        val index = channels.value.indexOfFirst { it.streamUrl == url }
        if (index != -1) {
            _currentChannelIndex.value = index
            onChannelPlayed(channels.value[index])
        }
    }

    fun nextChannel(): String? {
        if (channels.value.isEmpty()) return null
        val nextIndex = (_currentChannelIndex.value + 1) % channels.value.size
        _currentChannelIndex.value = nextIndex
        val channel = channels.value[nextIndex]
        onChannelPlayed(channel)
        return channel.streamUrl
    }

    fun previousChannel(): String? {
        if (channels.value.isEmpty()) return null
        val prevIndex = if (_currentChannelIndex.value <= 0) channels.value.size - 1 else _currentChannelIndex.value - 1
        _currentChannelIndex.value = prevIndex
        val channel = channels.value[prevIndex]
        onChannelPlayed(channel)
        return channel.streamUrl
    }
    
    fun getCurrentChannelName(): String {
        val index = _currentChannelIndex.value
        return if (index in channels.value.indices) channels.value[index].nombre else "Reproduciendo..."
    }
}
