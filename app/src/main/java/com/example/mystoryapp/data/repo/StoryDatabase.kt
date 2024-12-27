package com.example.mystoryapp.data.repo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mystoryapp.data.dao.DaoStory
import com.example.mystoryapp.data.dao.RemoteKeys
import com.example.mystoryapp.data.dao.RemoteKeysDao
import com.example.mystoryapp.data.response.ListStoryItemLocal


@Database(
    entities = [ListStoryItemLocal::class, RemoteKeys::class],
    version = 2,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {


    abstract fun storyDao(): DaoStory
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java, "story_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
      }
}