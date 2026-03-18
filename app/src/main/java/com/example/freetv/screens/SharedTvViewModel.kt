package com.example.freetv.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freetv.data.Channel
import com.example.freetv.data.ChannelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedTvViewModel(private val repository: ChannelRepository = ChannelRepository()) : ViewModel() {

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentChannelIndex = MutableStateFlow<Int>(-1)
    val currentChannelIndex: StateFlow<Int> = _currentChannelIndex.asStateFlow()

    init {
        loadChannels()
    }

    fun loadChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getChannels()
                .onSuccess { list ->
                    _channels.value = list
                }
                .onFailure { e ->
                    _error.value = "Error al cargar canales: ${e.message}"
                }
            _isLoading.value = false
        }
    }

    fun searchChannels(query: String) {
        viewModelScope.launch {
            repository.searchChannels(query)
                .onSuccess { list ->
                    _channels.value = list
                }
                .onFailure { e ->
                    _error.value = "Error al buscar: ${e.message}"
                }
        }
    }

    fun selectChannelByUrl(url: String) {
        val index = _channels.value.indexOfFirst { it.streamUrl == url }
        if (index != -1) {
            _currentChannelIndex.value = index
        }
    }

    fun nextChannel(): String? {
        if (_channels.value.isEmpty()) return null
        val nextIndex = (_currentChannelIndex.value + 1) % _channels.value.size
        _currentChannelIndex.value = nextIndex
        return _channels.value[nextIndex].streamUrl
    }

    fun previousChannel(): String? {
        if (_channels.value.isEmpty()) return null
        val prevIndex = if (_currentChannelIndex.value <= 0) _channels.value.size - 1 else _currentChannelIndex.value - 1
        _currentChannelIndex.value = prevIndex
        return _channels.value[prevIndex].streamUrl
    }
    
    fun getCurrentChannelName(): String {
        val index = _currentChannelIndex.value
        return if (index in _channels.value.indices) _channels.value[index].nombre else "Reproduciendo..."
    }
}
