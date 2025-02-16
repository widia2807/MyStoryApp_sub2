package com.example.mystoryapp.data.di

import android.content.Context
import com.example.mystoryapp.data.repo.StoryDatabase
import com.example.mystoryapp.data.repo.StoryManager
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.data.userpref.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object DependencyProvider {
    fun createUserRepository(context: Context, apiService: ApiService): UserManager {
        val preferences = UserPreference.getInstance(context.dataStore)
        return UserManager.createInstance(apiService, preferences)
    }

    fun createStoryRepository(context: Context, database: StoryDatabase): StoryManager {
        val userPreferences = UserPreference.getInstance(context.dataStore)
        val userSession = runBlocking { userPreferences.getSession().first() }
        val apiClient = ApiConfig.getApiService()
        return StoryManager.createInstance(apiClient, userPreferences, database)
    }


}