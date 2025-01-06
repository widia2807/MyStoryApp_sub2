package com.example.mystoryapp.ui.main.main1

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mystoryapp.data.repo.StoryManager
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.response.DetailStoryResponse
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.userpref.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class MainViewModel(
    private val userManager: UserManager,
    private val storyManager: StoryManager
) : ViewModel() {

    private val _needRefresh = MutableLiveData<Boolean>()
    val needRefresh: LiveData<Boolean> = _needRefresh

    fun getSession(): LiveData<UserModel> = userManager.retrieveUserSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            try {
                userManager.clearSession()
                Timber.d("User logged out successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error during logout")
                _isErr.value = "Logout failed: ${e.message}"
            }
        }
    }
    fun refreshStories() {
        viewModelScope.launch(SupervisorJob() + Dispatchers.IO) {
            try {
                Log.d("MainViewModel", "Calling refresh stories")
                storyManager.refreshStories()
                Log.d("MainViewModel", "Story refresh completed")
                _needRefresh.postValue(true)
                storyPager.collectLatest {
                    Log.d("MainViewModel", "New paging data received")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error refreshing stories", e)
            }
        }
    }


    val storyPager: Flow<PagingData<ListStoryItem>> = storyManager.getStoriesPaging()
        .cachedIn(viewModelScope)


    suspend fun fetchStoryDetail(storyId: String): DetailStoryResponse {
        return try {
            storyManager.fetchStoryDetails(storyId)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching story detail for ID: $storyId")
            throw e
        }
    }

    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: LiveData<List<ListStoryItem>> = _listStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    private val _isErr = MutableLiveData<String>()
    val isErr: LiveData<String> = _isErr

    init {
        Timber.d("MainViewModel initialized")
    }


    private fun updateLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    private fun handleError(error: Throwable) {
        Timber.e(error, "Error in MainViewModel")
        _isErr.value = error.message ?: "An unknown error occurred"
    }
}