package com.example.freetv.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.freetv.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    val customLists: StateFlow<List<CustomListEntity>> = userDataDao.getAllCustomLists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

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

    private val _timeRemaining = MutableStateFlow<Long?>(null)
    val timeRemaining: StateFlow<Long?> = _timeRemaining.asStateFlow()

    private val _isTimerActive = MutableStateFlow(false)
    val isTimerActive: StateFlow<Boolean> = _isTimerActive.asStateFlow()

    private val _timerMenuExpanded = MutableStateFlow(false)
    val timerMenuExpanded: StateFlow<Boolean> = _timerMenuExpanded.asStateFlow()

    private val _timerFinishedEvent = MutableSharedFlow<Unit>()
    val timerFinishedEvent: SharedFlow<Unit> = _timerFinishedEvent.asSharedFlow()

    private var timerJob: Job? = null

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
    }

    fun syncWithRemote() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.syncChannels()
            } catch (_: Exception) {
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
            onChannelPlayed(channels.value[index])
        }
    }

    fun nextChannel(): String? {
        if (channels.value.isEmpty()) return null
        return null
    }

    fun previousChannel(): String? {
        if (channels.value.isEmpty()) return null
        return null
    }

    fun getCurrentChannelName(): String {
        return "Reproduciendo..."
    }

    fun addCustomChannel(nombre: String, url: String) {
        viewModelScope.launch {
            val newChannel = Channel(
                nombre = nombre,
                categoria = "Personalizado",
                logoUrl = "",
                streamUrl = url,
                descripcion = "Canal añadido manualmente"
            )
            repository.addCustomChannel(newChannel)
        }
    }

    fun startSleepTimer(minutes: Int) {
        timerJob?.cancel()
        _isTimerActive.value = true
        val durationMillis = minutes * 60 * 1000L
        _timeRemaining.value = durationMillis

        timerJob = viewModelScope.launch {
            var remaining = durationMillis
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _timeRemaining.value = remaining
            }
            onTimerFinished()
        }
    }

    fun cancelSleepTimer() {
        timerJob?.cancel()
        _isTimerActive.value = false
        _timeRemaining.value = null
    }

    private suspend fun onTimerFinished() {
        _isTimerActive.value = false
        _timeRemaining.value = null
        _timerFinishedEvent.emit(Unit)
    }

    fun setTimerMenuExpanded(expanded: Boolean) {
        _timerMenuExpanded.value = expanded
    }

    sealed class ListCreationState {
        object Idle : ListCreationState()
        object Loading : ListCreationState()
        object Success : ListCreationState()
        data class Error(val message: String) : ListCreationState()
    }

    private val _listCreationState = MutableStateFlow<ListCreationState>(ListCreationState.Idle)
    val listCreationState: StateFlow<ListCreationState> = _listCreationState.asStateFlow()

    fun resetListCreationState() {
        _listCreationState.value = ListCreationState.Idle
    }

    fun createCustomList(name: String, selectedChannelUrls: List<String>) {
        if (name.isBlank()) {
            _listCreationState.value = ListCreationState.Error("Campos vacíos")
            return
        }

        viewModelScope.launch {
            _listCreationState.value = ListCreationState.Loading
            try {
                withContext(Dispatchers.IO) {
                    val listId = userDataDao.insertCustomList(CustomListEntity(name = name.trim()))

                    if (selectedChannelUrls.isNotEmpty()) {
                        val relations = selectedChannelUrls.map { url ->
                            CustomListChannel(listId = listId, streamUrl = url)
                        }
                        userDataDao.insertChannelsToCustomList(relations)
                    }
                }
                _listCreationState.value = ListCreationState.Success
            } catch (e: Exception) {
                _listCreationState.value = ListCreationState.Error("Error al guardar los datos")
            }
        }
    }
}