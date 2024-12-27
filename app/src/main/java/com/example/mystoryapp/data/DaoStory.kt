package com.example.mystoryapp.data

import androidx.paging.PagingSource
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mystoryapp.data.response.ListStoryItemLocal

@DaoStory
interface DaoStory {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<ListStoryItemLocal>)

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, ListStoryItemLocal>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}