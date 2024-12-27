package com.example.mystoryapp.ui.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.repo.StoryManager
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.ui.auth.NetworkResult
import kotlinx.coroutines.launch

class MapsViewModel(private val storyRepository: StoryManager) : ViewModel() {
    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: LiveData<List<ListStoryItem>> = _listStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        getStories()
    }

    fun getStories() {
        viewModelScope.launch {
            try {
                val response = storyRepository.fetchAllStories()
                if (response.listStory != null) {
                    _listStories.value = response.listStory
                    Log.d("MapsViewModel", "Stories fetched successfully")
                } else {
                    _error.value = "No stories available"
                    Log.e("MapsViewModel", "Story list is null")
                }
            } catch (e: IllegalStateException) {
                _error.value = "Authentication error: ${e.message}"
                Log.e("MapsViewModel", "Auth error: ${e.message}")
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("MapsViewModel", "Error fetching stories: ${e.message}")
            }
        }
    }

    // Alternative implementation using Flow if you prefer using the getStories Flow method
    fun getStoriesWithFlow() {
        viewModelScope.launch {
            storyRepository.getStories("").collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _listStories.value = result.data
                        _isLoading.value = false
                    }
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        _error.value = result.message
                        _isLoading.value = false
                    }
                }
            }
        }
    }
}