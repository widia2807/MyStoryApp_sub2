package com.example.mystoryapp.data.repo

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.mystoryapp.data.response.DetailStoryResponse
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.response.ListStoryItemLocal
import com.example.mystoryapp.data.response.StoryResponse
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.retrofit.StoryRemoteMediator
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.ui.auth.NetworkResult
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException



open class StoryManager private constructor(
    private val api: ApiService,
    private val preferences: UserPreference,
    private val database: StoryDatabase
) {

    fun getStories(token: String): Flow<NetworkResult<List<ListStoryItem?>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = api.getStories("Bearer $token")
            val stories = response.listStory ?: emptyList()
            emit(NetworkResult.Success(stories))
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: "An HTTP error occurred"))
        } catch (e: IOException) {
            emit(NetworkResult.Error("Network error occurred"))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "An unknown error occurred"))
        }
    }

    suspend fun refreshStories() {
        Log.d("StoryManager", "Starting story refresh")
        val remoteKeys = database.remoteKeysDao().getRemoteKeysId("0")
        if (remoteKeys != null) {
            database.remoteKeysDao().deleteRemoteKeys()
            database.storyDao().deleteAll()
        }
    }

    suspend fun fetchStoriesWithLocation(): StoryResponse {
        val authToken = preferences.getSession().firstOrNull()?.token
            ?: throw IllegalStateException("Authentication token is missing")
        return api.getStories(token = "Bearer $authToken", location = 1)
    }

    suspend fun fetchAllStories(): StoryResponse {
        val authToken = preferences.getSession().firstOrNull()?.token
        if (authToken.isNullOrEmpty()) {
            throw IllegalStateException("Authentication token is missing")
        }
        return api.getStories(token = "Bearer $authToken")
    }

    suspend fun fetchStoryDetails(storyId: String): DetailStoryResponse {
        val authToken = preferences.getSession().firstOrNull()?.token
            ?: throw IllegalStateException("Authentication token is missing")
        return api.getStoryDetail(token = "Bearer $authToken", id = storyId)
    }

    fun getPaginatedStories(): Flow<PagingData<ListStoryItemLocal>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { database.storyDao().getAllStory() }
        ).flow
    }

    suspend fun getStoriesLocation(): StoryResponse? {
        return try {
            val session = preferences.getSession().firstOrNull()
            if (session?.token != null) {
                api.getStoriesLocation("Bearer ${session.token}")
            } else {
                Log.e("StoryRepo", "Token is null")
                null
            }
        } catch (e: Exception) {
            Log.e("StoryRepo", "Error getting stories location: ${e.message}")
            null
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getStoriesPaging(): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            remoteMediator = StoryRemoteMediator(
                database = database,
                api = api,
                preferences = preferences
            ),
            pagingSourceFactory = {
                database.storyDao().getAllStory()
            }
        ).flow.map { pagingData ->
            pagingData.map { localItem ->
                ListStoryItem(
                    id = localItem.id,
                    name = localItem.name,
                    description = localItem.description,
                    photoUrl = localItem.photoUrl,
                    createdAt = localItem.createdAt,
                    lat = localItem.lat.toDoubleOrNull(),
                    lon = localItem.lon.toDoubleOrNull()
                )
            }
        }
    }
    companion object {
        @Volatile
        private var INSTANCE: StoryManager? = null

        fun createInstance(api: ApiService, preferences: UserPreference, database: StoryDatabase): StoryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryManager(api, preferences, database).also { INSTANCE = it }
            }
        }
    }

    @Module
    @InstallIn(ViewModelComponent::class)
    object MapsModule {

        @Provides
        fun provideStoryManager(
            api: ApiService,
            preferences: UserPreference,
            database: StoryDatabase
        ): StoryManager {
            return StoryManager(api, preferences, database)
        }
    }
}
