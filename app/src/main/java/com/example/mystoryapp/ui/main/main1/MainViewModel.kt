package com.example.mystoryapp.ui.main.main1

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    private val userManager: UserManager,
    private val storyManager: StoryManager
) : ViewModel() {

    fun getSession(): LiveData<UserModel> = userManager.retrieveUserSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userManager.clearSession()
        }
    }

    fun getStoryPager(): Flow<PagingData<ListStoryItem>> {
        return storyManager.getStoriesPaging()
    }


    suspend fun fetchStoryDetail(storyId: String): DetailStoryResponse {
        return storyManager.fetchStoryDetails(storyId)
    }

    private val _listStories = MutableLiveData<List<ListStoryItem>>()
    val listStories: LiveData<List<ListStoryItem>> = _listStories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isErr = MutableLiveData<String>()
    val isErr: LiveData<String> = _isErr
}