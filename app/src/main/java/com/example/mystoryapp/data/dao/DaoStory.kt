package com.example.mystoryapp.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mystoryapp.data.response.ListStoryItemLocal

@Dao
interface DaoStory {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<ListStoryItemLocal>)

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, ListStoryItemLocal>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}