package com.example.mystoryapp.data.retrofit

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.mystoryapp.data.dao.RemoteKeys
import com.example.mystoryapp.data.repo.StoryDatabase
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.response.ListStoryItemLocal
import com.example.mystoryapp.data.userpref.UserPreference
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val api: ApiService,
    private val preferences: UserPreference
) : RemoteMediator<Int, ListStoryItemLocal>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItemLocal>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        try {
            val token = preferences.getSession().first().token ?: ""
            val responseData = api.getStories("Bearer $token", page, state.config.pageSize)
            val response = responseData.listStory?.filterNotNull() ?: emptyList()
            val endOfPaginationReached = response.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = response.map { item ->
                    RemoteKeys(id = item.id ?: "", prevKey = prevKey, nextKey = nextKey)
                }
                database.remoteKeysDao().insertAll(keys)
                val convertApiToDao = toLocalModel(response)
                database.storyDao().insertStory(convertApiToDao)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private fun toLocalModel(items: List<ListStoryItem>): List<ListStoryItemLocal> {
        return items.map { item ->
            ListStoryItemLocal(
                id = item.id ?: "",
                name = item.name ?: "",
                description = item.description ?: "",
                photoUrl = item.photoUrl ?: "",
                createdAt = item.createdAt ?: "",
                lat = item.lat?.toString() ?: "",
                lon = item.lon?.toString() ?: ""
            )
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItemLocal>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { data ->
                database.remoteKeysDao().getRemoteKeysId(data.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItemLocal>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { data ->
                database.remoteKeysDao().getRemoteKeysId(data.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItemLocal>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }
}